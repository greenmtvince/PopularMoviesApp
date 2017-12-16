package com.quantrian.popularmoviesapp.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.quantrian.popularmoviesapp.MovieDetailActivity;
import com.quantrian.popularmoviesapp.models.Review;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Vinnie on 12/10/2017.
 */

public class FetchReviews extends AsyncTask<String, Void, ArrayList<Review> > {
    private static final String TAG = "FetchReviewsTask";
    private Context context;
    private TaskCompleteListener<ArrayList<Review>> listener;

    public FetchReviews(Context context, TaskCompleteListener<ArrayList<Review>> listener){
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected ArrayList<Review> doInBackground(String... params) {
        if (params.length==0)
            return null;

        String parameter = params[0];
        URL requestMoviesURL = NetworkUtilities.buildUrl(parameter);

        try{
            String jsonMovieResponse = NetworkUtilities.getResponseFromHttpUrl(requestMoviesURL);
            //Log.d("JSON_RESPONSE, "onItemClick: "+jsonMovieResponse.substring(0,50));
            ArrayList<Review> reviews = OpenMoviesJsonUtilities.getReviewListFromJson(context,jsonMovieResponse);
            return reviews;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Update the ArrayList<Movie> with the values we received from the API call.
    @Override
    protected void onPostExecute(final ArrayList<Review> reviewData){
        if (reviewData!=null){
            super.onPostExecute(reviewData);
            listener.onTaskComplete(reviewData);
        }
    }
}