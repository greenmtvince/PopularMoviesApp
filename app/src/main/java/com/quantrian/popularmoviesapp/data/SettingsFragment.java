package com.quantrian.popularmoviesapp.data;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.quantrian.popularmoviesapp.R;

/**
 * Created by Vinnie on 12/4/2017.
 */

//Code modified from the Udacity UD851 Lesson 6 Exercies
public class SettingsFragment extends PreferenceFragmentCompat
        {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_movies);
    }


}
