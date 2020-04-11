package com.example.bskapp.com.example.bskapp.dataapi;

/**
 * Represents an item in a ToDo list
 */
public class Location {


    @com.google.gson.annotations.SerializedName("location")
    private String mLocation;


    public Location() {

    }

    @Override
    public String toString() {
        return getLocation();
    }


    public Location(String location) {
        this.setLocation(location);
    }

    public String getLocation() {
        return mLocation;
    }

    public final void setLocation(String location) {
        mLocation = location;    }



    @Override
    public boolean equals(Object o) {
        return o instanceof Location ;
    }
}