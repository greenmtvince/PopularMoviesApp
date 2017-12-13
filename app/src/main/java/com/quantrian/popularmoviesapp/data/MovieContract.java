package com.quantrian.popularmoviesapp.data;

import android.provider.BaseColumns;

/**
 * Created by Vinnie on 12/12/2017.
 */

public class MovieContract  {
    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_DATE = "releaseDate";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_URL = "posterURL";
        public static final String COLUMN_POPULARITY = "popularity";
    }
}
