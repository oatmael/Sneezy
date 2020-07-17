package com.app.sneezyapplication.data;

import android.location.Location;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass(embedded = true)
public class SneezeData extends RealmObject {

    private String date;
    private String location;

    // Standard getters & setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Location locationAsAndroidLocation(){
        Location location = new Location("dummyprovider");
        //location.setLongitude();

        return location;
    }
}
