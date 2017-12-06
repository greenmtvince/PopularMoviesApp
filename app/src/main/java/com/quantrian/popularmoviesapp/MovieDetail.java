package com.quantrian.popularmoviesapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class MovieDetail extends AppCompatActivity {
    private TextView tv_MovieTitle;
    private ImageView iv_MoviePoster;
    private ProgressBar pb_MovieRating;
    private TextView tv_MovieRating;
    private TextView tv_releaseDate;
    private TextView tv_synopsis;

    //Very straightforward.  Take the data and populate the fields.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        //Add Navigation back to MainActivity
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Movie movie = getIntent().getParcelableExtra("MOVIE");
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
    }
}
