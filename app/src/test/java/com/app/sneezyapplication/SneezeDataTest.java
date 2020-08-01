package com.app.sneezyapplication;

import android.location.Location;

import com.app.sneezyapplication.data.SneezeData;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class SneezeDataTest {

    SneezeData s;
    Location l;
    Date d;

    @Before
    public void setup() {
        d = new Date();
        l = new Location("dummyprovider");
        l.setLatitude(100);
        l.setLongitude(100);
        s = new SneezeData(d.toString(), "100,100");

    }

    // http://tools.android.com/tech-docs/unit-testing-support#TOC-Method-...-not-mocked.-
    // Simply put, Android classes aren't loaded for the purposes of unit testing.
    // locationAsAndroidLocation() will always return null because of this when testing.
    // So be it.
    @Test
    public void locationHelperFunction(){
        assertEquals(l, s.locationAsAndroidLocation());
    }

    // ????
    // I'm having horrible, horrible flashbacks to my javascript days
    // true == true returns false? thanks cool shouldn't impact the code for our purposes.
    @Test
    public void dateHelperFunction(){
        assertEquals(d, s.dateAsAndroidDate());
    }
}
