package com.app.sneezyapplication.data;

import android.location.Location;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass(name = "Sneeze_sneezes", embedded = true)
public class SneezeData extends RealmObject {

    private String date;
    private String location;

    // Standard getters & setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public SneezeData(String date, String location) {
        this.date = date;
        this.location = location;
    }
    public SneezeData(){
        this.date = new Date().toString();
        this.location = "null,null";
    }

    public Location locationAsAndroidLocation(){
        Location location = new Location("dummyprovider");

        //Dumb error checking, should rethink this later
        String[] tokens = this.location.split(",");
        if (!tokens[0].equals("") || !tokens[1].equals("")){
            Long lat = 0L;
            Long lng = 0L;
            try {
                lat = Long.parseLong(tokens[0]);
                lng = Long.parseLong(tokens[1]);
            } catch (Exception e) {
                Log.e("location", e.getLocalizedMessage());
            }
            location.setLongitude(lng);
            location.setLatitude(lat);
        }

        return location;
    }

    public Date dateAsAndroidDate(){
        try {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
