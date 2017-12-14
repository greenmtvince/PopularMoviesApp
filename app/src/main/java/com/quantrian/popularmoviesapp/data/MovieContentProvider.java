package com.quantrian.popularmoviesapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Vinnie on 12/13/2017.
 */

public class MovieContentProvider extends ContentProvider {
    public static final int FAVORITES = 100; //Favorites directory
    public static final int FAVORITES_WITH_ID = 101; //Individual Favorite Movie

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.AUTHORITY,MovieContract.PATH_FAVORITES,FAVORITES);
        uriMatcher.addURI(MovieContract.AUTHORITY,MovieContract.PATH_FAVORITES+"/#",FAVORITES);

        return uriMatcher;
    }

    private MovieDbHelper mMovieDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMovieDbHelper = new MovieDbHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);

        Cursor retCursor;
        switch (match){
            case FAVORITES:
                retCursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVORITES_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection ="_id=?";
                String[] mSelectionArgs = new String[]{id};

                retCursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " +uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(),uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri;
        switch (match){
            case FAVORITES:
                long id = db.insert(MovieContract.MovieEntry.TABLE_NAME,null,contentValues);
                if (id>0){
                    returnUri= ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);

                } else {
                    throw new SQLException("Failed to insert row into " +uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " +uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
