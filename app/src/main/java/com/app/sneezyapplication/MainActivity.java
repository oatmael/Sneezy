package com.app.sneezyapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.app.sneezyapplication.data.SneezeRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SettingsFragment.RestartListener {
//TODO NEED TO ADD AN INTERFACE CLASS TO HANDLE DATA BETWEEN PAGES
    private DrawerLayout drawer;

    public static App app;
    public static User user;
    public static Realm realm;
    public static Location location;

    public FusedLocationProviderClient fusedLocationClient;

    public static SneezeRepository repo;

    private String userID;
    public String getUserID() {
        return userID;
    }

    SharedPref sharedPref;

    public static ForecastObj forecastObj;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()==true) {
            setTheme(R.style.darkTheme);
        }
        else setTheme(R.style.AppTheme);

        //set up forecastObj
        int locationPref = sharedPref.loadLocationPreference();
        if(locationPref != -1){
            forecastObj = new ForecastObj(locationPref);
        }
        else{forecastObj = new ForecastObj();
        }
        new getForecastAsync().execute(forecastObj.getUrl());

        super.onCreate(savedInstanceState);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(_location -> {
                        if (_location != null){
                            location = _location;
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("location", e.getLocalizedMessage());
                    });
        }
    }



    public void restartApp() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }



    @Override
    protected void onStart() {
        super.onStart();
        connectToDB();
        login();
        AppRater.app_launched(this);
    }

    @Override
    protected void onStop() {
        sharedPref.saveLocationPreference(forecastObj.getSelectedCityNo());
        super.onStop();
        realm.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void connectToDB(){
        String appID = getResources().getString(R.string.stitch_client_app_id);
        if (app == null)
            app = new App(new AppConfiguration.Builder(appID).build());
    }

    private void login(){
        try {
            user = app.currentUser();
        } catch (IllegalStateException e) {
            Log.e("NoLogin", e.getMessage());
        }

        if (user != null) {
            userID = user.getId();

            setupLocalData();

            return;
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 111);

        }
    }

    private void logout(){
        if (user != null) {
            user.logOutAsync(r -> {
                Log.i("REALM", "Logged out");
            });
        }
        realm.close();
    }

    private void setupLocalData(){
        String partitionValue = "partition";
        SyncConfiguration config = new SyncConfiguration.Builder(user, partitionValue)
                .waitForInitialRemoteData()
                .build();

        Realm.getInstanceAsync(config, new Realm.Callback() {
            @Override
            @ParametersAreNonnullByDefault
            public void onSuccess(Realm _realm) {
                realm = _realm;
                repo.updateRecords();
                Log.v("REALM", "Successfully instantiated realm!");
            }

            @Override
            public void onError(Throwable exception) {
                Log.e("REALM", exception.getMessage());
            }
        });

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
            case R.id.nav_logout:
                logoutPopup();
                break;
            case R.id.nav_rate_us:
                /*rateUs();*/

                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*Simple rate us Method which will direct the user to the Google Market rating page I believe. Used with the sidebar button.*/ /*WILL NOT WORK ON THE A.S EMULATOR as the emulator doesent have the*/
    /*market place. A real phone is required to test this properly*/
    public void rateUs() {
        String APP_RATE_NAME = "SneezyApplication"; /*TODO ADD ACTUAL GOOGLE MARKET PACKAGE NAME HERE WHEN ACQUIRED*/
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_RATE_NAME)));
    }



    public void logoutPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                logout();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 111);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(60000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationCallback mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location _location : locationResult.getLocations()) {
                        if (_location != null) {
                            location = _location;
                        }
                    }
                }
            };
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }//onRequestPermission End

    public static ForecastObj getForecastObj(){
        return forecastObj;
    }// getForecastObj End

    public static void setForecastObj(ForecastObj forecastObj){
        MainActivity.forecastObj = forecastObj;
    }// setForecastObj End

    static class getForecastAsync extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Toast.makeText(MainActivity.this, "Retrieving forecast",Toast.LENGTH_LONG).show();
            Log.d("ForecastObj","Retrieving forecast");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            Toast.makeText(MainActivity(), "The connection result: " + result, Toast.LENGTH_LONG).show();
            Log.d("ForecastObj","The connection result: " + result);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result;
            final String TAG = "doInBackground";

            try {
                String url =strings[0];
                //parse the page html to the document
                Document pageHtml = Jsoup.connect(url).get();
                //Check document is not empty
                if (pageHtml != null) {
                    Elements listElement = pageHtml.select("ul.pollen_graph");
                    final String forecasts = ""+ listElement.select("li").text();
                    result = "successful\n Values:"+forecasts;//**
                    //split the pollen forecast values into array (potential values: Low, Moderate, High, Very High, Extreme)
                    String delim = "\\W+";
                    String[] days = forecasts.split(delim);
                    ArrayList<String> daysList = new ArrayList<>();
                    Collections.addAll(daysList, days);
                    forecastObj.setDaysList(daysList);//update daysList in forecastObj
                }
                else {
                    result = "could not retrieve page from site\nUrl: "+url;
                }
            }//end of try
            catch (Exception ex) {
                ex.printStackTrace();
//                Log.e(TAG,"An Exception was thrown\n" +ex);
                Log.e("ForecastObj","An Exception was thrown\n" +ex);
                result = "An Exception was thrown\n"+ex;
            }//end of catch
            return result;//returns result to onPostExecute
        }//end of doInBackground
    }// getForecastAsync End

}//MainActivity End

