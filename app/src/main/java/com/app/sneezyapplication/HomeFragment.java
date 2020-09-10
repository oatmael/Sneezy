package com.app.sneezyapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.app.sneezyapplication.binding.MultiBind;
import com.app.sneezyapplication.binding.SneezeBind;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.databinding.FragmentHomeBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import io.realm.RealmList;
import io.realm.RealmQuery;


import java.util.Calendar;

import static com.app.sneezyapplication.MainActivity.repo;

public class HomeFragment extends Fragment {

    private ForecastObj forecastObj;

    Integer todaysSneezes;

    private SneezeBind mSneeze;
    private MultiBind mMulti;


    private static String packageName;
    private static Resources resources;
    private static View viewForUpdateView;

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

        repo.addListener(() -> {
            mBinding.setSneeze(mSneeze);
        });

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
            } else {
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
        //update forecast location text field and day/colour values for forecast
        forecastObj = MainActivity.getForecastObj();
        Button setLocationBtn = getView().findViewById(R.id.changeLocation);
        setLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocationPopup();
            }
        });
        LinearLayout indexLayout = getView().findViewById(R.id.indexLayout);
        indexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openIndexPopup();
            }
        });
    }//onViewCreated

    @Override
    public void onResume() {
        super.onResume();
        upDatePollenForecastView(viewForUpdateView, resources, packageName, forecastObj);
    }

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
        final String TAG = "setLocationPopup";
        Log.i(TAG, "launched set location popup");

        int cityNo = forecastObj.getSelectedCityNo();
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        final View popupView = getLayoutInflater().inflate(R.layout.popup_set_location, null);

        //check the radiobutton which corresponds to the city that has been set in forecastObj.selectedCity
        final RadioGroup radioGroupCities = popupView.findViewById(R.id.radioGCities);
        ((RadioButton) radioGroupCities.getChildAt(cityNo)).setChecked(true);

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
                RadioButton selectedCityRadio = popupView.findViewById(selectedCityId);
                String selectedCity = selectedCityRadio.getText().toString().replace("_radio", "");
//                Toast.makeText(getActivity(),"Save Clicked Selected City: "+forecastObj.getCityIndex(selectedCity),Toast.LENGTH_LONG).show();
                //get the index of the city that was selected and save it to the forecastObj
                if (forecastObj.getCityIndex(selectedCity) != -1) {
                    forecastObj.setSelectedCityNo(forecastObj.getCityIndex(selectedCity));
                    MainActivity.setForecastObj(forecastObj);// *NON-STATIC CONTEXT
                    Log.i("ForecastObj", "Location successfully set to " + forecastObj.getCityName(forecastObj.getSelectedCityNo()));
                    //update forecastObj in MainActivity
                    Log.i("ForecastObj", "Location " + selectedCity + "successfully saved to shared prefs");

                    //update pollen forecast from source
                    new MainActivity.getForecastAsync().execute(forecastObj.getUrl());
                    forecastObj = MainActivity.getForecastObj();
                    //update home frag values
                } else {
                    Toast.makeText(getActivity(), "Developer Error: selectedCity value does not exist", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }//end of onclick
        });//onClickListener END
    }// setLocationPopup END

    private void openIndexPopup(){
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        final View popupView = getLayoutInflater().inflate(R.layout.popup_index_details, null);
        //build dialog
        mBuilder.setView(popupView);
        final AlertDialog dialog = mBuilder.create();
        //set drawable for imageButton based on theme
//        SharedPref sharedPref = MainActivity.sharedPref;
        Drawable img;
        if(MainActivity.sharedPref.loadNightModeState()){
            img = ResourcesCompat.getDrawable(getResources(), R.drawable.weatherzone_logo_full_dark, null);
        }
        else{
            img = ResourcesCompat.getDrawable(getResources(), R.drawable.weatherzone_logo_full_light, null);
        }
        ImageButton weatherzoneLinkBtn = popupView.findViewById(R.id.weatherzoneImg);
        weatherzoneLinkBtn.setBackground(img);
        //show the dialog
        dialog.show();
        //Button to close dialog
        Button closeBtn = popupView.findViewById(R.id.btn_index_popup_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });//closeBtn onClick END
        //open www.weatherzone.com
        weatherzoneLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.weatherzone.com.au";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });//weatherzoneLinkBtn onClick END
    }//openIndexPopup END

    private static boolean locationFetched = false;
    public static void upDatePollenForecastView(View view, Resources resources, String packageName, ForecastObj forecastObj) {
        //if location has been fetched
        int size = forecastObj.getIndexValues().size();
        if(forecastObj.getIndexValues().size() == 4){
            //update location textview
            TextView pollenLocationTxt = view.findViewById(R.id.pollenCountLocationTxt);
            pollenLocationTxt.setText(forecastObj.getCityName(forecastObj.getSelectedCityNo()) + ", " + forecastObj.getStateName(forecastObj.getSelectedCityNo()));

            final int numDays = 4;
            final String[] weekDays = new String[]{"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
            //declare and get views to edit
            //check forecast has been initialized
            ConstraintLayout constraintLayout = view.findViewById(R.id.homeConstraintLayout);
            TextView dayNameTxt;
            ImageView dayImgView;
            String txtName;
            String imgName;
            int txtID;
            int imgID;
            //background colour variables
            final String[] coloursNames = new String[]{"lowColour", "moderateColour", "highColour", "vHighColour", "extremeColour"};
            final ArrayList<Integer> indexValueNums = forecastObj.getIndexValues();
            int colorID;
            int colorValue;

            //day name variables
            int counter = 0;
            Calendar calendar = Calendar.getInstance();
            int currentDayNo = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            String dayOfWeek;

            for (int i = 0; i < numDays; i++) {
                //get background colour for forecast
                try {
                    colorID = resources.getIdentifier(coloursNames[indexValueNums.get(i)], "color", packageName);
                    colorValue = ResourcesCompat.getColor(resources, colorID,null);
//                    String hexColor = String.format("#%06X", (0xFFFFFF &colorValue));
                } catch (Exception ex) {
                    colorValue = ResourcesCompat.getColor(resources, R.color.black,null);
                    Log.e("ForecastObj", "An Exception was thrown\nColor Not found\n" + ex);
                }
                //get and edit background
                try {
                    imgName = "forecastImage" + (i + 1);
                    imgID = resources.getIdentifier(imgName, "id", packageName);
                    dayImgView = constraintLayout.findViewById(imgID);
                    int[][] states = new int[][] {
                            new int[] {} // enabled
                    };
                    int[] color = new int[] {
                            colorValue
                    };
                    dayImgView.setBackgroundTintList(new ColorStateList(states, color));
                }
                catch (Exception ex) {
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
        }//if END
    }//upDatePollenForecastView END

    //called by main activity to update forecast values after a forecast has been retrieved
    public static void upDatePollenForecastViewOnPostExecute(ForecastObj forecastObj) {
        locationFetched = true;
        upDatePollenForecastView(viewForUpdateView, resources, packageName, forecastObj);
    }//upDatePollenForecastViewOnPostExecute END
}//HomeFragment END




