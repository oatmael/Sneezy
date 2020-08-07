package com.example.sneezyapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //TODO NEED TO ADD AN INTERFACE CLASS TO HANDLE DATA BETWEEN PAGES
    private DrawerLayout drawer;

    //Variables forecast functionality & to save location preferences to local memory
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LOCATION_INDEX= "locationIndex";
    static int locationIndex = -1;//stores value from shared preferences
    private static ForecastObj forecastObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocationPreference();
        forecastObj = getForecastObj();

        new getForecastAsync().execute(forecastObj.getUrl());
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
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }


    }
//TODO Put get supportFragmentManager, all into one method and reuse. (With animations)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_maps:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapsFragment()).commit();
                break;
            case R.id.nav_graph:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new GraphsFragment()).commit();
                break;
            case R.id.nav_stats:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
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



    //METHODS
    //TODO call this method when the hamburger menu is clicked
    public void saveLocationPreference(){
        //TODO check if locationIndex == forecastObj.getSelectedCityNo()
        //not true update locationIndex

        Log.d("ForecastObj", "saveLocationPreference Called");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LOCATION_INDEX, locationIndex);
    }
    private void loadLocationPreference(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        locationIndex = sharedPreferences.getInt(LOCATION_INDEX, -1);
    }


    //called by home fragment to retrieve and edit the forecast object
    public static ForecastObj getForecastObj(){
        //check if a forecastObj Has been created
        if (forecastObj==null){
            //check if a locationIndex has been saved to preferences
            if(locationIndex!=-1){
                //create a forecastObj with the stored location preferences
                forecastObj = new ForecastObj(locationIndex);
            }
            else{
                forecastObj = new ForecastObj();
            }
        }
        return forecastObj;
    }//end of getForecastObj


    public static void setForecastObj(ForecastObj forecastObj){
        MainActivity.forecastObj = forecastObj;
        //also updates the location index
//        locationIndex = forecastObj.getSelectedCityNo();
    }

    //Retrieves the forecast from the page url passed to it
    class getForecastAsync extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Retrieving forecast",Toast.LENGTH_LONG).show();
            Log.d("ForecastObj","Retrieving forecast");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, "The connection result: " + result, Toast.LENGTH_LONG).show();
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
    }//end of getForecastAsync

}//END OF MAIN ACTIVITY
