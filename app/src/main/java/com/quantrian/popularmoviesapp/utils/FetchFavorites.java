package com.quantrian.popularmoviesapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.quantrian.popularmoviesapp.data.MovieContract;
import com.quantrian.popularmoviesapp.models.Movie;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Vinnie on 12/16/2017.
 */

public class FetchFavorites extends AsyncTask<Void, Void, Cursor> {
    private Context mContext;
    private TaskCompleteListener<ArrayList<Movie>> mListener;

    public FetchFavorites(Context context, TaskCompleteListener listener){
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Cursor doInBackground(Void... voids) {
        Log.d("ASYNCLOAD", "Starting loadInBackground");
        try{
            return mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    MovieContract.MovieEntry.COLUMN_RATING);
        } catch (Exception e){
            Log.e(TAG, "Failed to Asynchronously Load Data.");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute (Cursor result){
        if (mListener !=null){
            super.onPostExecute(result);
            mListener.onTaskComplete(getAllFavorites(result));
        }
    }
    private ArrayList<Movie> getAllFavorites(Cursor c){
        ArrayList<Movie> favMovies = new ArrayList<>();

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                String title = c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
                String id = c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID));
                String url = c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_URL));
                String synopsis = c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS));
                Float popularity = c.getFloat(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY));
                Float rating = c.getFloat(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING));
                String date = c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_DATE));

                Movie m = new Movie(title,url,synopsis,popularity.toString(),rating.toString(),date,id);
                m.favorite = true;
                favMovies.add(m);
                c.moveToNext();
            }
        }
        return  favMovies;
    }
}