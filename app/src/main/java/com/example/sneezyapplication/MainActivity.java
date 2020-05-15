package com.example.sneezyapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sneezyapplication.data.SneezeItem;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.internal.common.BsonUtils;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
//TODO NEED TO ADD AN INTERFACE CLASS TO HANDLE DATA BETWEEN PAGES
    private DrawerLayout drawer;

    public static StitchAppClient client;

    private RemoteMongoCollection<SneezeItem> items;
    public RemoteMongoCollection<SneezeItem> getItems() {
        return items;
    }

    private String userID;
    public String getUserID() {
        return userID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/arial.ttf");*/

        // initiate the Stitch Client, depending on the app lifecycle might move this.
        client = Stitch.getDefaultAppClient();
        final RemoteMongoClient mongoClient = client.getServiceClient(
                RemoteMongoClient.factory, "mongodb-atlas");

        items = mongoClient
                .getDatabase(SneezeItem.SNEEZE_DATABASE)
                .getCollection(SneezeItem.SNEEZE_COLLECTION, SneezeItem.class)
                .withCodecRegistry(CodecRegistries.fromRegistries(
                        BsonUtils.DEFAULT_CODEC_REGISTRY,
                        CodecRegistries.fromCodecs(SneezeItem.codec)));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);


        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Handles if the phone is flipped if the saved instance state is empty(when app first launches) it launches to the home fragment and not to an empty activity.
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        login();
    }

    private void login(){
        if (client.getAuth().getUser() != null && client.getAuth().getUser().isLoggedIn()) {
            userID = client.getAuth().getUser().getId();
            return;
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 111);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //getting support frag vars/ also setting custom animations/ actual fragment changes done in switch below
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //set animations, animations can be changed in the res/anim folder
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right);
        //back stack used so when the back button is pressed, the app wont close, instead will go back to prev page
        transaction.addToBackStack(null);


        switch(item.getItemId()) {
            case R.id.nav_home:
                transaction.replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_maps:
                transaction.replace(R.id.fragment_container,
                        new MapsFragment()).commit();
                break;
            case R.id.nav_graph:
                transaction.replace(R.id.fragment_container,
                        new GraphsFragment()).commit();
                break;
            case R.id.nav_stats:
                transaction.replace(R.id.fragment_container,
                        new StatsFragment()).commit();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settingssss", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "WHAT ABOUT IT", Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
