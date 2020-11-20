package com.app.sneezyapplication;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    SharedPref sharedPref;

    RestartListener mCallBack;

    Button restartButton;

    public interface RestartListener {
        public void restartApp();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallBack = (RestartListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
            + " must implement RestartListener");
        }
    }


    public void onRestartClick() {
        mCallBack.restartApp();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        /*DARK MODE CODE*/
        final Switch darkModeSwitcher = (Switch)view.findViewById(R.id.darkModeSwitch);

        sharedPref = new SharedPref(getActivity());
        if (sharedPref.loadNightModeState()==true) {
            darkModeSwitcher.setChecked(true);
        }
        darkModeSwitcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                sharedPref.setNightModeState(true);
            }
            else {
                sharedPref.setNightModeState(false);
            }
        });

        restartButton = (Button) view.findViewById(R.id.appRestartButton);

        restartButton.setOnClickListener((View v) -> {
            onRestartClick();
        });


        /*End Dark Mode Code.////////////////////////////////*/
        return view;
    }





}
