package com.quantrian.popularmoviesapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quantrian.popularmoviesapp.adapters.PosterAdapter;
import com.quantrian.popularmoviesapp.adapters.ReviewAdapter;
import com.quantrian.popularmoviesapp.adapters.TrailerAdapter;
import com.quantrian.popularmoviesapp.models.Movie;
import com.quantrian.popularmoviesapp.models.Review;
import com.quantrian.popularmoviesapp.models.Trailer;
import com.quantrian.popularmoviesapp.utils.FetchReviews;
import com.quantrian.popularmoviesapp.utils.FetchTrailers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieDetail extends AppCompatActivity {
    private String movieID;
    private TextView tv_MovieTitle;
    private ImageView iv_MoviePoster;
    private ProgressBar pb_MovieRating;
    private TextView tv_MovieRating;
    private TextView tv_releaseDate;
    private TextView tv_synopsis;
    private RecyclerView mRecyclerView;
    private RecyclerView rv_reviews;
    private ArrayList<Trailer> trailerList;
    private ArrayList<Review> reviewList;

    //Very straightforward.  Take the data and populate the fields.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        trailerList = new ArrayList<>();
        reviewList = new ArrayList<>();

        //Add Navigation back to MainActivity
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Movie movie = getIntent().getParcelableExtra("MOVIE");
        movieID = movie.id;
        mRecyclerView = findViewById(R.id.rv_trailers);
        rv_reviews = findViewById(R.id.rv_reviews);
        tv_MovieTitle = (TextView) findViewById(R.id.MovieTitle);
        iv_MoviePoster = findViewById(R.id.imageViewPoster);
        pb_MovieRating = findViewById(R.id.pb_user_rating);
        tv_MovieRating = findViewById(R.id.tv_user_rating);
        tv_releaseDate = findViewById(R.id.tv_release_date);
        tv_synopsis = findViewById(R.id.tv_synopsis);

        tv_MovieTitle.setText(movie.originalTitle);
        Picasso.with(this).load(movie.moviePosterImageURL).into(iv_MoviePoster);
        pb_MovieRating.setProgress(Math.round(movie.rating*10));
        tv_MovieRating.setText(movie.rating.toString());
        tv_releaseDate.setText(movie.releaseDate);
        tv_synopsis.setText(movie.synopsis);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.LayoutManager reviewLayoutMgr = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        rv_reviews.setLayoutManager(reviewLayoutMgr);
        loadData(this);

    }

    private void loadData(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            //new MainActivity.FetchMovies().execute(sortBy);
            //loadFakeTrailerData();
            new FetchTrailers(this, new FetchTrailerTaskCompleteListener()).execute(movieID+"/trailers");
            new FetchReviews(this, new FetchReviewTaskCompleteListener()).execute(movieID+"/reviews");

            Toast.makeText(this, "I got here!",Toast.LENGTH_LONG).show();

        }else {
            Toast.makeText(this, "Not Connected to the internet",
                    Toast.LENGTH_LONG).show();
        }
    }

    //This sets the Adapter for the RecyclerView
    private void setAdapter(){
        TrailerAdapter trailerAdapter = new TrailerAdapter(getApplicationContext(),trailerList);
        trailerAdapter.setOnItemClickListener(new PosterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Trailer trailer = trailerList.get(position);
                watchYoutubeVideo(getApplicationContext(), trailer.source);
            }
        });
        ReviewAdapter reviewAdapter = new ReviewAdapter(getApplicationContext(),reviewList);
        rv_reviews.setAdapter(reviewAdapter);

        mRecyclerView.setAdapter(trailerAdapter);
    }

    //From Stackoverflow answer:
    //https://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
    private static void watchYoutubeVideo(Context context, String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    public class FetchReviewTaskCompleteListener implements AsyncTaskCompleteListener<ArrayList<Review>>{

        @Override
        public void onTaskComplete(ArrayList<Review> result) {
            reviewList = result;
            setAdapter();
        }
    }

    public class FetchTrailerTaskCompleteListener implements AsyncTaskCompleteListener<ArrayList<Trailer>>{
        @Override
        public void onTaskComplete(ArrayList<Trailer> result){
            trailerList = result;
            setAdapter();
        }
    }

    public interface AsyncTaskCompleteListener<T>{
        public void onTaskComplete(T result);
    }
}
