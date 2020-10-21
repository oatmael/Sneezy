package com.app.sneezyapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPref {

    SharedPreferences sneezyPreferences;
    public SharedPref(Context context) {
        sneezyPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);

    }

    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = sneezyPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.commit();
    }
    //true: night mode on - false: night mode off
    public Boolean loadNightModeState() {
        Boolean state = sneezyPreferences.getBoolean("NightMode", false);
        return state;
    }

    //public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LOCATION_INDEX_KEY= "locationIndex";
    public static final String MAP_PRESENTATION_KEY= "mapPresentation";
    public static final String MAP_TIME_RANGE_KEY= "mapTimeRange";
    public static final String MAP_USER_SCOPE_KEY= "mapUserScope";


    public void saveLocationPreference(int locationIndex){
        //TODO before calling* unless can be called from popup save btn
        //check IF locationIndex != forecastObj.getSelectedCityNo()
        //-IF FALSE update locationIndex
        //-ELSE do nothing
        SharedPreferences.Editor editor = sneezyPreferences.edit();
        editor.putInt(LOCATION_INDEX_KEY, locationIndex);
        editor.commit();
        Log.d("SharedPref","locationIndex " + locationIndex + " successfully saved to shared preferences");
    }

    public int loadLocationPreference(){
        int locationIndex = sneezyPreferences.getInt(LOCATION_INDEX_KEY, -1);
        return locationIndex;
    }


    public void saveMapPreferences(Enum timeRange, Enum userScope,Enum presentation){
        SharedPreferences.Editor editor = sneezyPreferences.edit();
        editor.putString(MAP_TIME_RANGE_KEY, timeRange.name());
        editor.putString(MAP_USER_SCOPE_KEY, userScope.name());
        editor.putString(MAP_PRESENTATION_KEY, presentation.name());
        editor.commit();
    }

    public String[] loadMapPreferences(){
        String[] mapPreferences= new String[3]; //0:TimeRange || 1:UserScope || 2:Presentation
        mapPreferences[0] = sneezyPreferences.getString(MAP_TIME_RANGE_KEY, "MONTH");
        mapPreferences[1] = sneezyPreferences.getString(MAP_USER_SCOPE_KEY, "ALL");
        mapPreferences[2] = sneezyPreferences.getString(MAP_PRESENTATION_KEY, "MARKER");
        return mapPreferences;
    }
}
