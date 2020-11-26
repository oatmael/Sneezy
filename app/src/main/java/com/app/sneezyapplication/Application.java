package com.app.sneezyapplication;

import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.app.sneezyapplication.data.GraphData;
import com.app.sneezyapplication.data.SneezeRepository;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;

public class Application extends android.app.Application {

    public static App app;
    public static User user;
    public static Realm realm;
    public static Location location;

    public static SneezeRepository repo;
    public static GraphData graphData;

    public static String userID;

    public static SharedPref sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

        String appID = getString(R.string.stitch_client_app_id);
        realm = Realm.getDefaultInstance();
        app = new App(new AppConfiguration.Builder(appID).build());
        try {
            user = app.currentUser();
        } catch (IllegalStateException e) {
            Log.e("NoLogin", e.getMessage());
        }

        repo = new SneezeRepository();

        Intent i;
        if (user != null) {
            repo.connectToDB();
            i = new Intent(this, MainActivity.class);
        }
        else
            i = new Intent(this, LoginActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
