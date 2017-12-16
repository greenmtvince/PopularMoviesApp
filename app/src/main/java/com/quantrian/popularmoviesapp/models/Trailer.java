package com.quantrian.popularmoviesapp.models;

import android.net.Uri;

/**
 * Created by Vinnie on 12/9/2017.
 */

public class Trailer {
    private static final String DOMAIN = "img.youtube.com";
    private static final String FILE = "default.jpg";
    private static final String PATH_MODIFIER = "vi";
    private static final Uri BASE_URI = Uri.parse("https://"+DOMAIN);

    public String name;
    public String source;
    public Uri trailerThumbUri;


    public Trailer(String name, String source){
        this.name=name;
        this.source=source;
        trailerThumbUri = BASE_URI.buildUpon().appendPath(PATH_MODIFIER).appendPath(source).appendEncodedPath(FILE).build();
    }
}
