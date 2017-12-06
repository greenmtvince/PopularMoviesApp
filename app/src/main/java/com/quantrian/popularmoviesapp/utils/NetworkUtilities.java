package com.quantrian.popularmoviesapp.utils;

import android.net.Uri;
import android.util.Log;

import com.quantrian.popularmoviesapp.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
//Code comes modified from the Udacity UD851 Sunshine Example
/**
 * These utilities will be used to communicate with the network.
 */

public class NetworkUtilities {
    final static String TMDB_BASE_URL ="https://api.themoviedb.org/3/movie";
    final static String PARAM_API_KEY = "api_key";

    final static String API_KEY= BuildConfig.API_KEY;


    public static URL buildUrl(String sortBy) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                //Our URL only differs by the value of sortBy
                .appendPath(sortBy)
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();
        //Log.d("MYURL", "buildUrl: "+builtUri);
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    //Code comes directly from the Udacity UD851 Sunshine Example
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();

            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}