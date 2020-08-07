package com.example.sneezyapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.security.PublicKey;


public class HomeFragment extends Fragment {

    static final String CLASS_TAG ="HomeFragment";



//    int selectedCityIndex;
    private ForecastObj forecastObj;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }// END OF onCreateView
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forecastObj = MainActivity.getForecastObj();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getActivity(),"HOME FRAG ON CREATE",Toast.LENGTH_LONG);
        Button setLocationBtn;
        setLocationBtn = getView().findViewById(R.id.forecastBtnLocation);
        //initialise forecastObj values

        Toast.makeText(getActivity(),""+forecastObj.getDaysList(),Toast.LENGTH_LONG).show();
        TextView txtForecast = getView().findViewById(R.id.txtHomeForecast);
        txtForecast.setText(forecastObj.getDaysList().toString());
        //TODO call function to display forecast results and location currently selected to home screen
        setLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocationPopup();
            }
        });
    }



    private void setLocationPopup(){

        final String TAG = "setLocationPopup";
        Log.i(TAG, "launched set location popup");

        int cityNo = forecastObj.getSelectedCityNo();
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        final View popupView = getLayoutInflater().inflate(R.layout.popup_set_location, null);

        //check the radiobutton which corresponds to the city that has been set in forecastObj.selectedCity
        final RadioGroup radioGroupCities = (RadioGroup) popupView.findViewById(R.id.radioGCities);
        ((RadioButton)radioGroupCities.getChildAt(cityNo)).setChecked(true);
        Toast.makeText(getActivity(),"selectedNo:" +cityNo,Toast.LENGTH_LONG);

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
                Toast.makeText(getActivity(),"Save Clicked Selected City: "+forecastObj.getCityIndex(selectedCity),Toast.LENGTH_LONG).show();
//                TextView testView = mView.findViewById(R.id.setLocationHeader);
                //get the index of the city that was selected and save it to the forecastObj
                if(forecastObj.getCityIndex(selectedCity)!=-1){
                    forecastObj.setSelectedCityNo(forecastObj.getCityIndex(selectedCity));

                    Log.i("ForecastObj","Location successfully set to "+forecastObj.getCity(forecastObj.getCityIndex(selectedCity)));
                    //update forecastObj in MainActivity
                    MainActivity.setForecastObj(forecastObj);
//                    Log.d(TAG,"Location successfully set to "+forecastObj.getCity(forecastObj.getCityIndex(selectedCity)));
                    Log.i("ForecastObj","Location successfully saved to "+forecastObj.getCity(forecastObj.getCityIndex(selectedCity)));

                    dialog.dismiss();
                }
                else {
                    Toast.makeText(getActivity(),"Backend Error: Selected city was -1",Toast.LENGTH_LONG);
                    dialog.dismiss();
                }
            }//end of onclick
        });//end of onClickListener
    }//end of setLocationPopup

}
