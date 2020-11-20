package com.app.sneezyapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.app.sneezyapplication.data.GraphData;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import static com.app.sneezyapplication.Application.*;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SettingsFragment.RestartListener {
    //TODO NEED TO ADD AN INTERFACE CLASS TO HANDLE DATA BETWEEN PAGES
    private DrawerLayout drawer;

    public FusedLocationProviderClient fusedLocationClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState() == true) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

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
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        graphData = new GraphData();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(_location -> {
                        if (_location != null) {
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
        AppRater.app_launched(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //realm.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //realm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }



    private void logout() {
        if (user != null) {
            user.logOutAsync(r -> {
                Log.i("REALM", "Logged out");
            });
        }
        realm.close();
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


        switch (item.getItemId()) {
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
            case R.id.nav_changelog:
                transaction.replace(R.id.fragment_container,
                        new ChangelogFragment()).commit();
                break;
            case R.id.nav_policy:
                transaction.replace(R.id.fragment_container,
                        new PolicyFragment()).commit();
                break;
            case R.id.nav_logout:
                logoutPopup();
                break;
            case R.id.nav_rate_us:

                rateApp();
                break;
            case R.id.nav_report_bug:
                sendEmail();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void sendEmail() {
        Log.i("Send email", "");
        String[] TO = {"sneezyapp@gmail.com"};
        /*        String[] CC = {"xyz@gmail.com"};*/
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        /*        emailIntent.putExtra(Intent.EXTRA_CC, CC);*/
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report: ");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Bug Report: Write your description of the bug, as well as the steps to make the bug occur. Screenshots help a lot. Thank you.");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /*Simple rate us Method which will direct the user to the Google Market rating page I believe. Used with the sidebar button.*/ /*Now works with Emulator. Opens Chrome website.*/
    public void rateApp()
    {

        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, "com.app.sneezyapplication")));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }


    public void logoutPopup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
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
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            NavigationView navigationView = findViewById(R.id.nav_view);
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof HomeFragment) {
                navigationView.setCheckedItem(R.id.nav_home);
            }
            if (f instanceof MapsFragment) {
                navigationView.setCheckedItem(R.id.nav_maps);
            }
            if (f instanceof GraphsFragment) {
                navigationView.setCheckedItem(R.id.nav_graph);
            }
            if (f instanceof StatsFragment) {
                navigationView.setCheckedItem(R.id.nav_stats);
            }
            if (f instanceof SettingsFragment) {
                navigationView.setCheckedItem(R.id.nav_settings);
            }
            if (f instanceof AboutFragment) {
                navigationView.setCheckedItem(R.id.nav_about);
            }
            if (f instanceof ChangelogFragment) {
                navigationView.setCheckedItem(R.id.nav_changelog);
            }
            if (f instanceof PolicyFragment) {
                navigationView.setCheckedItem(R.id.nav_policy);
            }
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

}//MainActivity End

