package com.app.sneezyapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    SharedPreferences colourModePref;
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

}
