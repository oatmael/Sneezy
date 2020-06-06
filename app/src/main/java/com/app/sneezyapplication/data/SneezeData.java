package com.app.sneezyapplication.data;

import java.util.Date;

public class SneezeData {

    private final Date date;
    private final String location;

    public SneezeData(Date date, String location) {
    this.date = date;
    this.location = location;
}

    public Date getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }
}
