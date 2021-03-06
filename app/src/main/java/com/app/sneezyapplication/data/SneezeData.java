package com.app.sneezyapplication.data;

import android.location.Location;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            double lat = 0;
            double lng = 0;
            try {
                lat = Double.parseDouble(tokens[0]);
                lng = Double.parseDouble(tokens[1]);
            } catch (Exception e) {
                Log.e("location", e.getLocalizedMessage());
            }
            location.setLongitude(lng);
            location.setLatitude(lat);
        } else {
            return null;
        }

        return location;
    }

    public Date dateAsAndroidDate(){
        try {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
