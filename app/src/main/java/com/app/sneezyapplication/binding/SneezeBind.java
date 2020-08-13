package com.app.sneezyapplication.binding;

import static com.app.sneezyapplication.MainActivity.repo;

public class SneezeBind {


    public Integer todaysSneeze;
    public String todaysSneezeCountText;


    public SneezeBind() {

    }

    public String getTodaysSneezes() {
        /*if (repo.todayUserSneezeItems() == null) { //TODO REMOVE WHEN GOOGLE LOGIN IS WORKING CORRECTLY
            todaysSneezeCountText = "You haven't sneezed today";
        } else {*/
        todaysSneeze = repo.todayUserSneezeItems()
                .getSneezes()
                .size();
        if (todaysSneeze == null) {    //Maybe Remove the null check once the google login is working correctly. Null check is already in the getTodaysSneezes method
            todaysSneezeCountText = ("You haven't sneezed today");
        } else if (todaysSneeze == 1) {
            todaysSneezeCountText = ("You've sneezed once today");
        } else if (todaysSneeze < 1) {
            todaysSneezeCountText = ("You haven't sneezed today");
        } else {
            todaysSneezeCountText = ("You've sneezed " + todaysSneeze + " times today");
        }
        return todaysSneezeCountText;
    }
}


