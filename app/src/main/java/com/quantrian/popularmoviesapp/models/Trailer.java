package com.quantrian.popularmoviesapp.models;

/**
 * Created by Vinnie on 12/9/2017.
 */

public class Trailer {
    public String name;
    public String source;
    public String trailerThumbURL;

    public Trailer(String name, String source){
        this.name=name;
        this.source=source;
        trailerThumbURL = "http://img.youtube.com/vi/"+source+"/default.jpg";
    }
}
