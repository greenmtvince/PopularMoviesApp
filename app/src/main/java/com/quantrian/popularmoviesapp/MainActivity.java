package com.quantrian.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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

import com.quantrian.popularmoviesapp.data.SettingsActivity;
import com.quantrian.popularmoviesapp.utils.NetworkUtilities;
import com.quantrian.popularmoviesapp.utils.OpenMoviesJsonUtilities;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String MOVIELIST_KEY = "movielist";
    private String sortBy;
    private ArrayList<Movie> movieList;
    private RecyclerView mRecyclerView;

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
        //Quick call to shared preferences to determine whether we're sorting by highest rated or most popular
        setupSharedPreferences();


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),spanCount);
        mRecyclerView.setLayoutManager(layoutManager);

        //This saves a network call if we're just flipping orientation.
        //Basically it checks if we have a saved instance state which is the movie list and if
        //we do it loads from that.  Otherwise we make a network call.
        if (savedInstanceState!=null) {
            movieList = savedInstanceState.getParcelableArrayList(MOVIELIST_KEY);
            if (movieList!=null)
                setAdapter();
        } else
            loadMovieData(this);
    }

    //Puts our ArrayList<Movie> movieList into a parcelableArray
    @Override
    protected void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        if (movieList!=null)
            outstate.putParcelableArrayList(MOVIELIST_KEY,movieList);
    }

    //Checks to see if we're connected to the internet and if so calls the AsyncTask
    //Otherwise displays a Toast that the app isn't connected.
    private void loadMovieData(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected)
            new FetchMovies().execute(sortBy);
        else {
            Toast.makeText(this, "Not Connected to the internet",
                    Toast.LENGTH_LONG).show();
        }
    }

    //Async task that makes the network call.
    //This should go to it's own file for maintainability.  Will keep in MainActivity
    // for Project 1 Submission
    //TODO Refactor FetchMovies to it's own Java file.
    private class FetchMovies extends AsyncTask<String, Void, ArrayList<Movie> >{

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            if (params.length==0)
                return null;

            String parameter = params[0];
            URL requestMoviesURL = NetworkUtilities.buildUrl(parameter);

            try{
                String jsonMovieResponse = NetworkUtilities.getResponseFromHttpUrl(requestMoviesURL);
                //Log.d("JSON_RESPONSE", "onItemClick: "+jsonMovieResponse.substring(0,50));
                ArrayList<Movie> movieData = OpenMoviesJsonUtilities.getMovieListFromJson(MainActivity.this,jsonMovieResponse);
                return movieData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        //Update the ArrayList<Movie> with the values we received from the API call.
        @Override
        protected void onPostExecute(final ArrayList<Movie> movieData){
            if (movieData!=null){
                movieList=movieData;
                setAdapter();
            }
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

    //Function for testing the build with with static local data
    private ArrayList<Movie> makeMovies(){
        ArrayList<Movie> movieSet = new ArrayList<>();
        movieSet.add(new Movie("Thor: Ragnarok", getResources().getString(R.string.base_img_url)+"oSLd5GYGsiGgzDPKTwQh7wamO8t.jpg",
                "Thor is imprisoned on the other side of the universe and finds himself in a race against time to get back to Asgard to stop Ragnarok, the prophecy of destruction to his homeworld and the end of Asgardian civilization, at the hands of an all-powerful new threat, the ruthless Hela.",
                "743.969937","7.5", "2017-10-25"));
        movieSet.add(new Movie("It", getResources().getString(R.string.base_img_url)+"/9E2y5Q7WlCVNEhP5GiVTjhEhx1o.jpg",
                "In a small town in Maine, seven children known as The Losers Club come face to face with life problems, bullies and a monster that takes the shape of a clown called Pennywise.",
                "874.849543","7.3", "2017-09-05"));
        return movieSet;
    }

}
