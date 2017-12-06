package com.quantrian.popularmoviesapp;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by Vinnie on 11/30/2017.
 */

public class Movie implements Parcelable {
    public String originalTitle;
    public String moviePosterImageURL;
    public String synopsis;
    public Float popularityScore;
    public Float rating;
    public String releaseDate;

    //Initialize each instance of Movie with data from the API
    public Movie(String title, String imgURL, String synopsis, String popularity, String rating, String date){
        this.originalTitle = title;
        this.moviePosterImageURL = imgURL;
        this.synopsis = synopsis;
        this.releaseDate = date;
        this.popularityScore = convertToFloat(popularity);
        this.rating = convertToFloat(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //Convert to a Parcel
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(originalTitle);
        parcel.writeString(moviePosterImageURL);
        parcel.writeString(synopsis);
        parcel.writeFloat(rating);
        parcel.writeFloat(popularityScore);
        parcel.writeString(releaseDate);
    }

    //Build from a Parcel
    protected Movie(Parcel in){
        originalTitle = in.readString();
        moviePosterImageURL = in.readString();
        synopsis = in.readString();
        rating = in.readFloat();
        popularityScore = in.readFloat();
        releaseDate = in.readString();
    }

    //I don't quite get the purpose of the CREATOR
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    //Conversion utility
    private Float convertToFloat(String floater){
        Float outFloat;
        try {
            outFloat = Float.parseFloat(floater);
        } catch (Exception e){
            e.printStackTrace();
            outFloat = 0.0f;
        }
        return outFloat;
    }

}
