package com.quantrian.popularmoviesapp.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.quantrian.popularmoviesapp.MovieDetailActivity;
import com.quantrian.popularmoviesapp.models.Movie;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Vinnie on 12/11/2017.
 */

public class FetchMovies extends AsyncTask<String, Void, ArrayList<Movie> > {
    private static final String TAG = "FetchMoviesTask";
    private Context context;
    private TaskCompleteListener<ArrayList<Movie>> listener;

    public FetchMovies(Context context, TaskCompleteListener<ArrayList<Movie>> listener){
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected ArrayList<Movie> doInBackground(String... params) {
        if (params.length==0)
            return null;

        String parameter = params[0];
        URL requestMoviesURL = NetworkUtilities.buildUrl(parameter);

        try{
            String jsonMovieResponse = NetworkUtilities.getResponseFromHttpUrl(requestMoviesURL);
            //Log.d("JSON_RESPONSE, "onItemClick: "+jsonMovieResponse.substring(0,50));
            ArrayList<Movie> movies = OpenMoviesJsonUtilities.getMovieListFromJson(context,jsonMovieResponse);
            return movies;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Update the ArrayList<Movie> with the values we received from the API call.
    @Override
    protected void onPostExecute(final ArrayList<Movie> movieData){
        if (movieData!=null){
            super.onPostExecute(movieData);
            listener.onTaskComplete(movieData);
        }
    }
}