package com.quantrian.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class MainActivity extends AppCompatActivity {
    private static final String MOVIELIST_KEY = "movielist";
    private String sortBy;
    private ArrayList<Movie> movieList;
    private RecyclerView mRecyclerView;

    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.main_recycler_view);
        //movieList = new ArrayList<>();

        //Make the layout adaptable to landscape or portrait.
        //I should do an additional check for larger screen formats and allocate more columns.
        int spanCount;
        if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
            spanCount=2;
        else
            spanCount=3;
        //Quick call to shared preferences to determine whether we're sorting by highest rated or most popular
        setupSharedPreferences();

        //Initialize the SQLite Database
        MovieDbHelper dbHelper = new MovieDbHelper(this);
        mDb = dbHelper.getWritableDatabase();


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),spanCount);
        mRecyclerView.setLayoutManager(layoutManager);

        //This saves a network call if we're just flipping orientation.
        //Basically it checks if we have a saved instance state which is the movie list and if
        //we do it loads from that.  Otherwise we make a network call.
        if (savedInstanceState!=null) {
            movieList = savedInstanceState.getParcelableArrayList(MOVIELIST_KEY);
            if (movieList != null) {
                setAdapter();
            }
        }else if (sortBy.equals(getString(R.string.pref_sort_value_favorites))){
            movieList = getAllFavorites();
            setAdapter();
        } else {
            loadMovieData(this);
        }
    }

    //Puts our ArrayList<Movie> movieList into a parcelableArray
    @Override
    protected void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        if (movieList!=null)
            outstate.putParcelableArrayList(MOVIELIST_KEY,movieList);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (sortBy.equals(getString(R.string.pref_sort_value_favorites))) {
            movieList = getAllFavorites();
            setAdapter();
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

    public class FetchMovieTaskCompleteListener implements MovieDetail.AsyncTaskCompleteListener<ArrayList<Movie>> {
        @Override
        public void onTaskComplete(ArrayList<Movie> result){
            movieList = result;
            setAdapter();
        }
    }

    private ArrayList<Movie> getAllFavorites(){
        Cursor c = mDb.query(MovieContract.MovieEntry.TABLE_NAME,null, null, null, null,null,MovieContract.MovieEntry.COLUMN_POPULARITY);
        //c.getCount()
        //c.moveToFirst();
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
        /*for (int i=0; i<c.getCount();i++){
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
        }*/
        return  favMovies;
    }
}
