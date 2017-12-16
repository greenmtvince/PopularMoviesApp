package com.quantrian.popularmoviesapp.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.quantrian.popularmoviesapp.MovieDetailActivity;
import com.quantrian.popularmoviesapp.models.Trailer;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Vinnie on 12/11/2017.
 */

public class FetchTrailers extends AsyncTask<String, Void, ArrayList<Trailer> > {
    private static final String TAG = "FetchReviewsTask";
    private Context context;
    private TaskCompleteListener<ArrayList<Trailer>> listener;

    public FetchTrailers(Context context, TaskCompleteListener<ArrayList<Trailer>> listener){
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected ArrayList<Trailer> doInBackground(String... params) {
        if (params.length==0)
            return null;

        String parameter = params[0];
        URL requestMoviesURL = NetworkUtilities.buildUrl(parameter);

        try{
            String jsonMovieResponse = NetworkUtilities.getResponseFromHttpUrl(requestMoviesURL);
            //Log.d("JSON_RESPONSE, "onItemClick: "+jsonMovieResponse.substring(0,50));
            ArrayList<Trailer> trailers = OpenMoviesJsonUtilities.getTrailerListFromJson(context,jsonMovieResponse);
            return trailers;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Update the ArrayList<Movie> with the values we received from the API call.
    @Override
    protected void onPostExecute(final ArrayList<Trailer> trailerData){
        if (trailerData!=null){
            super.onPostExecute(trailerData);
            listener.onTaskComplete(trailerData);
        }
    }
}