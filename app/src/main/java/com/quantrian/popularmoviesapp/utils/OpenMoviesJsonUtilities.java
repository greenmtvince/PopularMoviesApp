package com.quantrian.popularmoviesapp.utils;

import android.content.Context;

import com.quantrian.popularmoviesapp.Movie;
import com.quantrian.popularmoviesapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vinnie on 12/4/2017.
 */

//Code comes modified from the Udacity UD851 Sunshine Example
public class OpenMoviesJsonUtilities {
    public static ArrayList<Movie> getMovieListFromJson(Context context, String tmdbJsonStr)
        throws JSONException {
        ArrayList<Movie> movieList = new ArrayList<>();

        //Turn our JSON response body into a JSON Object
        JSONObject moviesJson = new JSONObject(tmdbJsonStr);

        //results contains an array of movie objects, so we convert this to a JSONArray
        JSONArray moviesArray = moviesJson.getJSONArray("results");

        //For each item in the array, we convert to a JSONObject and then pass the values of that object
        //to a Movie object.  This could be done much more concisely with the GSON library.  I opted for
        //the JSONObject to be more explicit in my code about what was happening and because these libraries
        //weren't introduced in the course.
        for (int i =0;i<moviesArray.length();i++){
            JSONObject movieJson = moviesArray.getJSONObject(i);
            String title = movieJson.getString("title");
            String imgURL = context.getResources().getString(R.string.base_img_url)+movieJson.getString("poster_path");
            String overview = movieJson.getString("overview");
            String popularity = movieJson.getString("popularity");
            String rating = movieJson.getString("vote_average");
            String date = movieJson.getString("release_date");

            Movie movie = new Movie(title,imgURL,overview,popularity,rating,date);

            //Once we have a movie object, add it to our ArrayList<Movie>
            movieList.add(movie);
        }
        return movieList;
    }

}
