package com.app.sneezyapplication;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

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


import com.app.sneezyapplication.binding.MultiBind;
import com.app.sneezyapplication.binding.SneezeBind;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.SneezeRepository;
import com.app.sneezyapplication.databinding.FragmentHomeBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


import io.realm.RealmList;
import io.realm.RealmQuery;


import java.util.Calendar;
import static com.app.sneezyapplication.MainActivity.repo;

public class HomeFragment extends Fragment {

    private ForecastObj forecastObj;

    Integer todaysSneezes;

    private SneezeBind mSneeze;
    private MultiBind mMulti;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Using binding the DataBindingUtil needs to be used with inflation. The Views(view.) will remain the same and you can use as per usual.
        FragmentHomeBinding mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        View view = mBinding.getRoot();

        final Button sneezeButton = view.findViewById(R.id.sneezeButton);
        final Button minusButton = view.findViewById(R.id.minusButton);
        final Button plusButton = view.findViewById(R.id.plusButton);

        //BINDING
        //SneezeBind
        SneezeBind sneezer = new SneezeBind();
        mSneeze = sneezer;
        mBinding.setSneeze(mSneeze);
        //Multi Bind
        MultiBind multiMaker = new MultiBind();
        mMulti = multiMaker;
        mMulti.getMulti(1);
        mBinding.setMulti(mMulti);

        sneezeButton.setOnClickListener((View v) -> {
            /*START LOCATION CODE*/
            MainActivity mainAct = (MainActivity) getActivity();
            if (mainAct.checkLocationPermission()) {
                mainAct.fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                //Log.e("app", String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
                                MainActivity.location = location;
                            }
                        }).addOnFailureListener(e -> {
                    Log.e("location", e.getLocalizedMessage());
                });
            }
            Integer multiNumber;
            int i;
            multiNumber = multiMaker.getMultiNum();
            if (multiNumber > 1) {
                for (i = 0; i < multiNumber; i++) {
                    handleSneeze();
                }
                mBinding.setSneeze(mSneeze);
                mMulti.getMulti(1);
                mBinding.setMulti(mMulti);
            }
            else {
                handleSneeze();
                mBinding.setSneeze(mSneeze);
            }

        });

        minusButton.setOnClickListener((View v) -> {
            mMulti.getMulti(3);
            mBinding.setMulti(mMulti);
            mBinding.setSneeze(mSneeze);
        });

        plusButton.setOnClickListener((View v) -> {
            mMulti.getMulti(2);
            mBinding.setMulti(mMulti);
            mBinding.setSneeze(mSneeze);
        });

        return view;
    }

    DateFormat dayFormat = new SimpleDateFormat("EEE MMM dd yyyy");

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initialise static variables for updating view
        packageName = getActivity().getBaseContext().getPackageName();
        viewForUpdateView = getView();
        resources = getResources();
        //update forecast location text field

        forecastObj = MainActivity.getForecastObj();
        updatePollenCountLocationTxt();
//        upDatePollenForecastView(getView(), getResources(), getActivity().getBaseContext().getPackageName(), forecastObj);
        Button setLocationBtn = getView().findViewById(R.id.changeLocation);
        setLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocationPopup();
            }
        });
    }//onViewCreated

    private void handleSneeze() {
        RealmQuery<SneezeItem> searchForCurrentDateQuery = MainActivity.realm.where(SneezeItem.class)
                .equalTo(SneezeItem.Fields.DATE, dayFormat.format(new Date()))
                .equalTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId());

        if (searchForCurrentDateQuery.count() != 0) {
            updateCurrentSneeze();
        } else {
            createNewSneeze();
        }
    }

    private void createNewSneeze() {
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

    private void updateCurrentSneeze() {
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

    private String getLocation() {
        String lat = "";
        String lng = "";
        if (MainActivity.location != null) {
            lat = String.valueOf(MainActivity.location.getLatitude());
            lng = String.valueOf(MainActivity.location.getLongitude());

            Log.e("app", MainActivity.location.getLatitude() + "," + MainActivity.location.getLongitude());
        }
        return lat + "," + lng;
    }


    private void setLocationPopup() {
//        Toast.makeText(getActivity(),"Location popup called",Toast.LENGTH_SHORT).show();

        final String TAG = "setLocationPopup";
        Log.i(TAG, "launched set location popup");

        int cityNo = forecastObj.getSelectedCityNo();
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        final View popupView = getLayoutInflater().inflate(R.layout.popup_set_location, null);

        //check the radiobutton which corresponds to the city that has been set in forecastObj.selectedCity
        final RadioGroup radioGroupCities = (RadioGroup) popupView.findViewById(R.id.radioGCities);
        ((RadioButton) radioGroupCities.getChildAt(cityNo)).setChecked(true);
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
                String selectedCity = selectedCityRadio.getText().toString().replace("_radio", "");
//                Toast.makeText(getActivity(),"Save Clicked Selected City: "+forecastObj.getCityIndex(selectedCity),Toast.LENGTH_LONG).show();
                //get the index of the city that was selected and save it to the forecastObj
                if (forecastObj.getCityIndex(selectedCity) != -1) {
                    forecastObj.setSelectedCityNo(forecastObj.getCityIndex(selectedCity));
                    MainActivity.setForecastObj(forecastObj);// *NON-STATIC CONTEXT
                    Log.i("ForecastObj", "Location successfully set to " + forecastObj.getCityName(forecastObj.getSelectedCityNo()));
                    //update forecastObj in MainActivity
                    Log.i("ForecastObj", "Location " + selectedCity + "successfully saved to shared prefs");


                    //update pollen forecast
                    new MainActivity.getForecastAsync().execute(forecastObj.getUrl());
                    //update home frag values
                    updatePollenCountLocationTxt();
//                    upDatePollenForecastView(getView(), getResources(), getActivity().getBaseContext().getPackageName(), forecastObj);
                } else {
                    Toast.makeText(getActivity(), "Developer Error: selectedCity value does not exist", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }//end of onclick
        });//onClickListener END
    }// setLocationPopup END

    public void updatePollenCountLocationTxt() {
        TextView pollenLocationTxt = getView().findViewById(R.id.pollenCountLocationTxt);
        pollenLocationTxt.setText(forecastObj.getCityName(forecastObj.getSelectedCityNo()) + ", " + forecastObj.getStateName(forecastObj.getSelectedCityNo()));
    }

    private int getTodaysSneezes() {
        if (repo.todayUserSneezeItems() == null) { //TODO REMOVE WHEN GOOGLE LOGIN IS WORKING CORRECTLY
            todaysSneezes = 0;
        } else {
            todaysSneezes = repo.todayUserSneezeItems()
                    .getSneezes()
                    .size();
        }
        return todaysSneezes;

    }



    public static void upDatePollenForecastView(View view, Resources resources, String packageName, ForecastObj forecastObj) {
        final int numDays = 4;
        final String[] weekDays = new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        //get views to edit and reference resources
        ConstraintLayout constraintLayout = view.findViewById(R.id.homeConstraintLayout);
//        ArrayList<String> forecastDays =new ArrayList<String>(forecastObj.getDaysList());
//        ImageView backgroundImg;

//        relativeLayout.findViewById(R.id.forecastImage1);
//        View homeLayout = (R.layout.fragment_home);
        TextView dayNameTxt;
        View dayImgView;
        String txtName;
        String imgName;
        int txtID;
        int imgID;
        //background variables
        int dayForecastValue;
//        forecast_block_

        final String[] drawableColours = new String[]{"green", "yellow", "orange", "red_orange", "red"};
        final ArrayList<Integer> IndexValueNums = forecastObj.getIndexValues();

        int drawableID;
        String drawableName;
        Drawable background;
        //day variables
        int counter = 0;
        Calendar calendar = Calendar.getInstance();
        int currentDayNo = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String dayOfWeek;

        for (int i = 0; i < numDays; i++) {
            //get background colour for forecast
            try {
                drawableName = ("forecast_block_" + drawableColours[IndexValueNums.get(i)]);
                drawableID = resources.getIdentifier(drawableName, "drawable", packageName);
                background = resources.getDrawable(drawableID);
//            background = resources.getDrawable(R.drawable.forecast_block_red_orange);
            } catch (Exception ex) {
                background = resources.getDrawable(R.drawable.forecast_block_green);
                Log.e("ForecastObj", "An Exception was thrown\nDrawable Not found\n" + ex);

            }
            //get and edit background
            try {
                imgName = "forecastImage" + (i + 1);
                imgID = resources.getIdentifier(imgName, "id", packageName);
                dayImgView = constraintLayout.findViewById(imgID);
                dayImgView.setBackground(background);
            } catch (Exception ex) {
                Log.e("ForecastObj", "An Exception was thrown\nDay Img cant be found\n" + ex);
            }
            //get Current day from weekDays array
            if (currentDayNo + counter > 6) {
                currentDayNo = 0;
                counter = 0;
            }
            dayOfWeek = weekDays[currentDayNo + counter];
            //get and edit text View
            txtName = "forecastTextBlock" + (i + 1);
            txtID = resources.getIdentifier(txtName, "id", packageName);
            dayNameTxt = constraintLayout.findViewById(txtID);
            dayNameTxt.setText(dayOfWeek);

            counter++;
        }//for END
    }//upDatePollenForecastView END

    static String packageName;
    static Resources resources;
    static View viewForUpdateView;

    //called by main activity to update forecast values for
    public static void upDatePollenForecastViewOnPostExecute(ForecastObj forecastObj) {
        upDatePollenForecastView(viewForUpdateView, resources, packageName, forecastObj);
    }//upDatePollenForecastViewOnPostExecute END
}//HomeFragment END




