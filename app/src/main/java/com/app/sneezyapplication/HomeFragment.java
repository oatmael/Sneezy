package com.app.sneezyapplication;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmQuery;


public class HomeFragment extends Fragment {

    private ForecastObj forecastObj;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
//        forecastObj = new ForecastObj();// *testing*
        final Button sneezeButton = view.findViewById(R.id.sneezeButton);
        sneezeButton.setOnClickListener(v -> {
            MainActivity mainAct = (MainActivity)getActivity();
            if (mainAct.checkLocationPermission()) {
                mainAct.fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null){
                                //Log.e("app", String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
                                MainActivity.location = location;
                            }
                        }).addOnFailureListener(e -> {
                    Log.e("location", e.getLocalizedMessage());
                });
            }
            handleSneeze();
        });
        return view;
    }//onCreateView

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //update forecast location string
        forecastObj = MainActivity.getForecastObj();
        updatePollenCountLocationTxt();
        
        Button setLocationBtn = getView().findViewById(R.id.changeLocation);
        setLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocationPopup();
            }
        });
//        Toast.makeText(getActivity(), "City index loaded from shared prefs: "+forecastObj.getCityName(forecastObj.getSelectedCityNo()), Toast.LENGTH_LONG).show();
    }//onViewCreated

    DateFormat dayFormat = new SimpleDateFormat("EEE MMM dd yyyy");

    private void handleSneeze(){
        RealmQuery<SneezeItem> searchForCurrentDateQuery = MainActivity.realm.where(SneezeItem.class)
                .equalTo(SneezeItem.Fields.DATE, dayFormat.format(new Date()))
                .equalTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId());

        if (searchForCurrentDateQuery.count() != 0){
            updateCurrentSneeze();
        } else {
            createNewSneeze();
        }
    }//handleSneeze()

    private void createNewSneeze(){
        MainActivity.realm.executeTransaction(r -> {
            SneezeData sd = new SneezeData(
                    new Date().toString(),
                    getLocation());
            RealmList<SneezeData> sdl = new RealmList<>();
            sdl.add(sd);

            SneezeItem sneeze = new SneezeItem(
                    dayFormat.format(new Date()), MainActivity.user.getId(), sdl);

            MainActivity.realm.insert(sneeze);
        });
    }

    private void updateCurrentSneeze(){
        MainActivity.realm.executeTransaction(r -> {
            SneezeItem sneeze = MainActivity.realm.where(SneezeItem.class)
                    .equalTo(SneezeItem.Fields.DATE, dayFormat.format(new Date()))
                    .equalTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId())
                    .findFirst();

            sneeze.getSneezes().add(
                    new SneezeData(
                            new Date().toString(),
                            getLocation()
                    ));
        });
    }

    private String getLocation(){
        String lat = "";
        String lng = "";
        if (MainActivity.location != null) {
            lat = String.valueOf(MainActivity.location.getLatitude());
            lng = String.valueOf(MainActivity.location.getLongitude());

            Log.e("app", String.valueOf(MainActivity.location.getLatitude()) + "," + String.valueOf(MainActivity.location.getLongitude()));
        }
        return lat + "," + lng;
    }


    private void setLocationPopup(){
//        Toast.makeText(getActivity(),"Locaiton popup called",Toast.LENGTH_SHORT).show();

        final String TAG = "setLocationPopup";
        Log.i(TAG, "launched set location popup");

        int cityNo = forecastObj.getSelectedCityNo();
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        final View popupView = getLayoutInflater().inflate(R.layout.popup_set_location, null);

        //check the radiobutton which corresponds to the city that has been set in forecastObj.selectedCity
        final RadioGroup radioGroupCities = (RadioGroup) popupView.findViewById(R.id.radioGCities);
        ((RadioButton)radioGroupCities.getChildAt(cityNo)).setChecked(true);
//        Toast.makeText(getActivity(),"selectedNo:" +cityNo,Toast.LENGTH_LONG);

        //show the dialog
        mBuilder.setView(popupView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        //save button will set the value selectedCityNo to access the city name and url
        Button saveBtn = popupView.findViewById(R.id.locationSaveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the selected radio button to save it to the selectedLocationNo
                int selectedCityId = radioGroupCities.getCheckedRadioButtonId();
                RadioButton selectedCityRadio = (RadioButton) popupView.findViewById(selectedCityId);
                String selectedCity = selectedCityRadio.getText().toString().replace("_radio","");
//                Toast.makeText(getActivity(),"Save Clicked Selected City: "+forecastObj.getCityIndex(selectedCity),Toast.LENGTH_LONG).show();
                //get the index of the city that was selected and save it to the forecastObj
                if(forecastObj.getCityIndex(selectedCity)!=-1){
                    forecastObj.setSelectedCityNo(forecastObj.getCityIndex(selectedCity));
                    MainActivity.setForecastObj(forecastObj);// *NON-STATIC CONTEXT
                    Log.i("ForecastObj","Location successfully set to "+forecastObj.getCityName(forecastObj.getSelectedCityNo()));
                    //update forecastObj in MainActivity
                    Log.i("ForecastObj","Location "+selectedCity+"successfully saved to shared prefs");


                    //update pollen forecast
                    new MainActivity.getForecastAsync().execute(forecastObj.getUrl());
                    //update home frag values
                    updatePollenCountLocationTxt();
                }
                else {
                    Toast.makeText(getActivity(),"Developer Error: selectedCity value does not exist",Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }//end of onclick
        });//end of save onClickListener
    }//end of setLocationPopup

    private void updatePollenCountLocationTxt(){
        TextView pollenLocationTxt = getView().findViewById(R.id.pollenCountLocationTxt);
        pollenLocationTxt.setText(forecastObj.getCityName(forecastObj.getSelectedCityNo())+", "+forecastObj.getStateName(forecastObj.getSelectedCityNo()));
    }

    //TODO add method to update pollen forecast values in view
    //-update date sting values of view based on current date date
    //-add colour attributes for each value [Extreme, Very High, High, Moderate, Low]
    //TODO add popup to for more detail about pollen index

}
