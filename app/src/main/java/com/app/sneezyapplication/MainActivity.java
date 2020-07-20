package com.app.sneezyapplication;

import android.content.Intent;
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
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeRepository;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.internal.common.BsonUtils;
import com.mongodb.stitch.core.services.mongodb.remote.ChangeEvent;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ChangeEventListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.DefaultSyncConflictResolvers;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ErrorListener;

import org.bson.BsonValue;
import org.bson.codecs.configuration.CodecRegistries;

import java.util.Set;

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

    public SneezeRepository repo;

    private String userID;
    public String getUserID() {
        return userID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/arial.ttf");*/

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

        repo = new SneezeRepository();

        connectToDB();
        login();
    }

    private void connectToDB(){
        // initiate the Stitch Client, depending on the app lifecycle might move this.

        /*client = Stitch.getDefaultAppClient();
        final RemoteMongoClient mongoClient = client.getServiceClient(
                RemoteMongoClient.factory, "mongodb-atlas");

        items = mongoClient
                .getDatabase(SneezeItem.SNEEZE_DATABASE)
                .getCollection(SneezeItem.SNEEZE_COLLECTION, SneezeItem.class)
                .withCodecRegistry(CodecRegistries.fromRegistries(
                        BsonUtils.DEFAULT_CODEC_REGISTRY,
                        CodecRegistries.fromCodecs(SneezeItem.codec)));*/

        String appID = getResources().getString(R.string.stitch_client_app_id);
        client = Stitch.getDefaultAppClient();

    }

    private void login(){
        if (client.getAuth().getUser() != null && client.getAuth().getUser().isLoggedIn()) {
            userID = client.getAuth().getUser().getId();

            setupLocalData();

            return;
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 111);
        }
    }

    private void setupLocalData(){
        final RemoteMongoClient mongoClient = client.getServiceClient(
                RemoteMongoClient.factory, "mongodb-atlas");
        items = mongoClient
                .getDatabase(SneezeItem.SNEEZE_DATABASE)
                .getCollection(SneezeItem.SNEEZE_COLLECTION, SneezeItem.class)
                .withCodecRegistry(CodecRegistries.fromRegistries(
                        BsonUtils.DEFAULT_CODEC_REGISTRY,
                        CodecRegistries.fromCodecs(SneezeItem.codec)));

        items.sync().configure(
                DefaultSyncConflictResolvers.remoteWins(),
                new CollectionUpdateListener(),
                new CollectionErrorListener()
        );
    }


    private class CollectionErrorListener implements ErrorListener {
        @Override
        public void onError(BsonValue documentId, Exception error) {
            Log.e("Stitch", error.getLocalizedMessage());

            Set<BsonValue> docsToBeFixed = items.sync().getPausedDocumentIds().getResult();
            for (BsonValue doc_id : docsToBeFixed){
                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                // Other error resolve logic if necessary
                items.sync().resumeSyncForDocument(doc_id);
            }

            // Refresh views here if necessary
        }
    }

    private class CollectionUpdateListener implements ChangeEventListener<SneezeItem> {
        @Override
        public void onEvent(BsonValue documentId, ChangeEvent<SneezeItem> event) {
            if (!event.hasUncommittedWrites()) {
                // Do something on update

                repo.updateRecords(MainActivity.this);
            }

            // Refresh views here if necessary
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
                transaction.replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                break;
            case R.id.nav_about:
                transaction.replace(R.id.fragment_container,
                        new AboutFragment()).commit();
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

