package com.quantrian.popularmoviesapp;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantrian.popularmoviesapp.adapters.PosterAdapter;
import com.quantrian.popularmoviesapp.adapters.ReviewAdapter;
import com.quantrian.popularmoviesapp.adapters.TrailerAdapter;
import com.quantrian.popularmoviesapp.data.MovieContract;
import com.quantrian.popularmoviesapp.models.Movie;
import com.quantrian.popularmoviesapp.models.Review;
import com.quantrian.popularmoviesapp.models.Trailer;
import com.quantrian.popularmoviesapp.utils.FetchReviews;
import com.quantrian.popularmoviesapp.utils.FetchTrailers;
import com.quantrian.popularmoviesapp.utils.TaskCompleteListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {
    private static final String SCROLL_POSITION = "scroll_position";

    private Movie movie;
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
    private Button btn;
    private ScrollView mScroll;
    private int[] mPosition; //Holds the position of the scrollview

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

        movie = getIntent().getParcelableExtra("MOVIE");

        if(findMovie(movie.id)>0)
            movie.favorite=true;

        //Saw the suggestion to use Butterknife here.  I'll have to try that out for in the future
        //because it looks pretty handy for larger projects!
        mScroll = findViewById(R.id.scrollViewDetail);
        mRecyclerView = findViewById(R.id.rv_trailers);
        rv_reviews = findViewById(R.id.rv_reviews);
        tv_MovieTitle = findViewById(R.id.MovieTitle);
        iv_MoviePoster = findViewById(R.id.imageViewPoster);
        pb_MovieRating = findViewById(R.id.pb_user_rating);
        tv_MovieRating = findViewById(R.id.tv_user_rating);
        tv_releaseDate = findViewById(R.id.tv_release_date);
        tv_synopsis = findViewById(R.id.tv_synopsis);
        btn = findViewById(R.id.btn_favorite);

        if (movie.favorite)
            btn.setText("Unfavorite");

        tv_MovieTitle.setText(movie.originalTitle);
        Picasso.with(this)
                .load(movie.moviePosterImageURL)
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.broken_android)
                .into(iv_MoviePoster);
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
            new FetchTrailers(this, new FetchTrailerTaskCompleteListener()).execute(movie.id+"/trailers");
            new FetchReviews(this, new FetchReviewTaskCompleteListener()).execute(movie.id+"/reviews");
        } else {
            Toast.makeText(this, R.string.toast_no_network,
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

    public void addToFavorites(View view){
        movie.favorite = !movie.favorite;

        String btnMsg;

        Long idOut;
        if (movie.favorite) {
            btnMsg = "UnFavorite";
            String bob = addNewMovie(movie);
            //Toast.makeText(this, "You inserted at: " +bob+" MovieID= "+movie.id,Toast.LENGTH_SHORT).show();
        } else {
            btnMsg = "Favorite";
            //idOut = findMovie(movie.id);
            removeMovie(movie.id);
            //Toast.makeText(this, "You removed MovieID= "+movie.id,Toast.LENGTH_SHORT).show();
        }
        btn.setText(btnMsg);
    }

    private String addNewMovie(Movie newMovie){
        ContentValues cv = new ContentValues();

        cv.put(MovieContract.MovieEntry.COLUMN_ID, newMovie.id);
        cv.put(MovieContract.MovieEntry.COLUMN_DATE,newMovie.releaseDate);
        cv.put(MovieContract.MovieEntry.COLUMN_FAVORITE, newMovie.favorite);
        cv.put(MovieContract.MovieEntry.COLUMN_POPULARITY, newMovie.popularityScore);
        cv.put(MovieContract.MovieEntry.COLUMN_RATING, newMovie.rating);
        cv.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, newMovie.synopsis);
        cv.put(MovieContract.MovieEntry.COLUMN_TITLE, newMovie.originalTitle);
        cv.put(MovieContract.MovieEntry.COLUMN_URL, newMovie.moviePosterImageURL);

        Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, cv);

        if(uri !=null){
            return uri.toString();
        } else
            return "Didn't Work!";


    }

    private long findMovie(String movieID){
        Uri myUri = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieID).build();

        Cursor c = getContentResolver().query(
                myUri,
                null,
                null,
                null,
                null);
            if (c.getCount()>0) {
                c.moveToFirst();
                return c.getLong(c.getColumnIndex(MovieContract.MovieEntry._ID));
            }
            else
                return -1;
    }

    private void removeMovie(String movieId){
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(movieId).build();
        getContentResolver().delete(uri, null,null);
    }

    public class FetchReviewTaskCompleteListener implements TaskCompleteListener<ArrayList<Review>>{
        @Override
        public void onTaskComplete(ArrayList<Review> result) {
            reviewList = result;
            setAdapter();
            setScrollPosition();
        }
    }

    public class FetchTrailerTaskCompleteListener implements TaskCompleteListener<ArrayList<Trailer>> {
        @Override
        public void onTaskComplete(ArrayList<Trailer> result){
            trailerList = result;
            setAdapter();
            setScrollPosition();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        mPosition = new int[]{mScroll.getScrollX(),mScroll.getScrollY()};
        outstate.putIntArray(SCROLL_POSITION,mPosition);
    }

    //This is called after the Async Tasks have completed rather than in onRestoreInstanceState
    //because onRestoreInstanceState fires before the recyclerViews have completed loading and thus
    //doesn't scroll because there's nowhere to scroll yet.
    //
    //Though, it does mean each time the screen is rotated, I'm making an extraneous network call.
    //TODO convert Trailer and Review objects to Parcelable and pass them to the bundle
    // during rotation to avoid extra network calls.  Possibly move set scroll position back to
    //onRestoreInstanceState.
    private void setScrollPosition(){
        if (mPosition != null){
            mScroll.post(new Runnable() {
                @Override
                public void run() {
                    mScroll.scrollTo(mPosition[0],mPosition[1]);
                }
            });
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        mPosition = savedInstanceState.getIntArray(SCROLL_POSITION);

    }

}
