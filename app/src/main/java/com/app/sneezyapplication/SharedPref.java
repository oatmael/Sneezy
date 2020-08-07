package com.app.sneezyapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class SharedPref {

    SharedPreferences colourModePref;//may need to refactor name to something like sharedPrefs
    public SharedPref(Context context) {
        colourModePref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = colourModePref.edit();
        editor.putBoolean("NightMode", state);
        editor.commit();
    }

    public Boolean loadNightModeState() {
        Boolean state = colourModePref.getBoolean("NightMode", false);
        return state;
    }



//    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LOCATION_INDEX_KEY= "locationIndex";


    public void saveLocationPreference(int locationIndex){
        //TODO before calling* unless can be called from popup save btn
        //check if locationIndex == forecastObj.getSelectedCityNo()
        //not true update locationIndex
        SharedPreferences.Editor editor = colourModePref.edit();
        editor.putInt(LOCATION_INDEX_KEY, locationIndex);
        editor.commit();
        Log.d("SharedPref","locationIndex " + locationIndex + " successfully saved to shared preferences");
    }

    public int loadLocationPreference(){
        int locationIndex = colourModePref.getInt(LOCATION_INDEX_KEY, -1);
        return locationIndex;
    }


}
