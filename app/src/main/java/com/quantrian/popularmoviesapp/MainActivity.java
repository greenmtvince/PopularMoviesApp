package com.quantrian.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.quantrian.popularmoviesapp.adapters.PosterAdapter;
import com.quantrian.popularmoviesapp.data.MovieContract;
import com.quantrian.popularmoviesapp.models.Movie;
import com.quantrian.popularmoviesapp.data.SettingsActivity;
import com.quantrian.popularmoviesapp.utils.FetchFavorites;
import com.quantrian.popularmoviesapp.utils.FetchMovies;
import com.quantrian.popularmoviesapp.utils.GridAutofitLayoutManager;
import com.quantrian.popularmoviesapp.utils.TaskCompleteListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String MOVIELIST_KEY = "movielist";
    private static final String SAVED_LAYOUT_MANAGER = "layout_manager";
    private String sortBy;
    private ArrayList<Movie> movieList;
    private RecyclerView mRecyclerView;

    private static final String TAG = MainActivity.class.getSimpleName();
    private Parcelable layoutManagerSavedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.main_recycler_view);

        //Quick call to shared preferences to determine whether we're sorting by highest rated,
        // most popular, or favorites.
        setupSharedPreferences();

        //Initialize our RecyclerView using an Autofit manager to determine columnspan
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(this, getResources().getInteger(R.integer.columnWidth));
        mRecyclerView.setLayoutManager(layoutManager);

        //This saves a network call if we're just flipping orientation.
        //Basically it checks if we have a saved instance state which is the movie list and if
        //we do it loads from that.
        // OR If we're retrieving favorites, we just pull it from the local store
        // Otherwise we make a network call.
        if (savedInstanceState!=null) {
            movieList = savedInstanceState.getParcelableArrayList(MOVIELIST_KEY);
            if (movieList != null) {
                setAdapter();
            }
        }else if (sortBy.equals(getString(R.string.pref_sort_value_favorites))){
            //Get our favorite movies from the content provider
            //getSupportLoaderManager().initLoader(0,null, this);
            new FetchFavorites(this, new FetchMovieTaskCompleteListener()).execute();
        } else {
            loadMovieData(this);
        }
    }

    //Puts our ArrayList<Movie> movieList into a parcelableArray
    //Also stores our layout manager to save the scroll state
    @Override
    protected void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        if (movieList!=null)
            outstate.putParcelableArrayList(MOVIELIST_KEY,movieList);
        outstate.putParcelable(SAVED_LAYOUT_MANAGER, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle state){
        if (state !=null){
            layoutManagerSavedState = state.getParcelable(SAVED_LAYOUT_MANAGER);
        }
        super.onRestoreInstanceState(state);
    }

    //Handles if the user navigates to this screen by back button rather than
    //the menu bar icon.  We check to see if the preferences have changed, and either reload
    //the favorites to reflect any changes to the local store, OR reload the list from the network
    @Override
    public void onResume(){
        super.onResume();

        setupSharedPreferences();
        if (sortBy.equals(getString(R.string.pref_sort_value_favorites))) {
            //getSupportLoaderManager().restartLoader(0,null, this);
            new FetchFavorites(this, new FetchMovieTaskCompleteListener()).execute();
        } else {
            loadMovieData(this);
        }
    }

    //Checks to see if we're connected to the internet and if so calls the AsyncTask
    //Otherwise displays a Toast that the app isn't connected.
    //TODO Implement a broadcast reciever to detect connectivity status changes
    //https://www.androidhive.info/2012/07/android-detect-internet-connection-status/
    private void loadMovieData(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected){
            new FetchMovies(this, new FetchMovieTaskCompleteListener()).execute(sortBy);
        } else {
            Toast.makeText(this, "Not Connected to the internet",
                    Toast.LENGTH_LONG).show();
        }
    }

    //This sets the Adapter for the RecyclerView
    private void setAdapter(){
        PosterAdapter posterAdapter = new PosterAdapter(getApplicationContext(),movieList);
        posterAdapter.setOnItemClickListener(new PosterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Movie movie = movieList.get(position);
                Intent i=new Intent(getApplicationContext(),MovieDetailActivity.class);
                i.putExtra("MOVIE",movie);
                startActivity(i);
            }
        });
        mRecyclerView.setAdapter(posterAdapter);
        if (layoutManagerSavedState!=null){

            mRecyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }
    }

    //Creates a menu option for the settings activity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    //Launch the settings activity!
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //This calls the sharedPreference Manager so we can load the user's previously defined preferences
    //for sorting.
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortBy = sharedPreferences.getString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_value_popular));
    }

    public class FetchMovieTaskCompleteListener implements TaskCompleteListener<ArrayList<Movie>> {
        @Override
        public void onTaskComplete(ArrayList<Movie> result){
            movieList = result;
            setAdapter();
        }
    }
}
