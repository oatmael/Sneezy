package com.app.sneezyapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/*CLASS FOR AN AUTOMATIC RATE US DIALOG POPUP*/
/*This dialog will pop up after a specific amount of days OR a specific amount of app launches. Asking the user to rate the app.*/
public class AppRater {

    private final static String APP_TITLE = "Sneezy";
    private final static String APP_PNAME = "SneezeApplication"; /*REPLACE WITH ACTUAL GOOGLE PACKAGE NAME*/

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 3;

    private static SharedPref sharedPref;

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRatePopup(mContext, editor);
            }
        }
        editor.commit();
    }

    public static void showRatePopup(Context mContext, final SharedPreferences.Editor editor) {
        sharedPref = new SharedPref(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AlertDialogCustom)); /*Replace mcontext if not working.*/
        builder.setCancelable(true);
        builder.setTitle("Rate " + APP_TITLE);

        builder.setMessage("If you enjoy using " + APP_TITLE + ", please take some time to rate it. Thank you for your support!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME));
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Maybe Later.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editor != null) {
                    Long date_firstLaunch;
                    date_firstLaunch = System.currentTimeMillis();
                    editor.putLong("date_firstlaunch", date_firstLaunch);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No, don't show again.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        if (sharedPref.loadNightModeState()==true) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.darkBackground);
        }
        else {
            dialog.getWindow().setBackgroundDrawableResource(R.color.lightBackground);
        }
    }
    }




