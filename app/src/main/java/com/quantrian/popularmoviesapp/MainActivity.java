package com.quantrian.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.quantrian.popularmoviesapp.data.MovieDbHelper;
import com.quantrian.popularmoviesapp.models.Movie;
import com.quantrian.popularmoviesapp.data.SettingsActivity;
import com.quantrian.popularmoviesapp.utils.FetchMovies;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    private static final String MOVIELIST_KEY = "movielist";
    private static final String SAVED_LAYOUT_MANAGER = "layout_manager";
    private String sortBy;
    private ArrayList<Movie> movieList;
    private RecyclerView mRecyclerView;

    //private SQLiteDatabase mDb;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Parcelable layoutManagerSavedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.main_recycler_view);

        //Make the layout adaptable to landscape or portrait.
        //I should do an additional check for larger screen formats and allocate more columns.
        int spanCount;
        if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
            spanCount=2;
        else
            spanCount=3;

        //Quick call to shared preferences to determine whether we're sorting by highest rated,
        // most popular, or favorites.
        setupSharedPreferences();

        //Initialize the SQLite Database
        //MovieDbHelper dbHelper = new MovieDbHelper(this);
        //mDb = dbHelper.getWritableDatabase();

        //Initialize our RecyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),spanCount);

        //if (savedInstanceState==null)
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
            getSupportLoaderManager().initLoader(0,null, this);
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

    //
    @Override
    protected void onRestoreInstanceState(Bundle state){
        if (state !=null){
            layoutManagerSavedState = state.getParcelable(SAVED_LAYOUT_MANAGER);
            //mRecyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
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
            getSupportLoaderManager().restartLoader(0,null, this);
        } else {
            loadMovieData(this);
        }
    }

    //Checks to see if we're connected to the internet and if so calls the AsyncTask
    //Otherwise displays a Toast that the app isn't connected.
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
                Intent i=new Intent(getApplicationContext(),MovieDetail.class);
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
        //Log.d("PREFFOES", "setupSharedPreferences: "+sortBy);
    }

    //This code is modified from ud851-Lesson09 to-do-list
    //Should I be concerned about the IDE flag for memory leak?
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return  new AsyncTaskLoader<Cursor>(this) {
            Cursor mTaskData=null;

            //Code from ud851-Lesson09-To_do-List
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                Log.d("ASYNCLOAD", "Starting loadInBackground");
                try{
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
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

            public void deliverResult(Cursor data){
                Integer result = data.getCount();
                Log.d("ASYNCLOAD", "The cursor has "+result.toString()+" items in it");
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        movieList = getAllFavorites(data);
        setAdapter();
    }

    //TODO Need to resolve this?
    //I feel like I should add some code here, but I'm not sure what.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class FetchMovieTaskCompleteListener implements MovieDetail.AsyncTaskCompleteListener<ArrayList<Movie>> {
        @Override
        public void onTaskComplete(ArrayList<Movie> result){
            movieList = result;
            setAdapter();
        }
    }

    //Gets all the users favorite movies for use in the adapter.
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
