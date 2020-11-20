package com.app.sneezyapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.app.sneezyapplication.binding.MultiBind;
import com.app.sneezyapplication.binding.SneezeBind;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.SneezeRepository;
import com.app.sneezyapplication.databinding.FragmentHomeBinding;
import com.app.sneezyapplication.forecast.ForecastResultHandler;
import com.app.sneezyapplication.forecast.ForecastResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.app.sneezyapplication.Application.*;

public class HomeFragment extends Fragment {
    Integer todaysSneezes;

    private SneezeBind mSneeze;
    private MultiBind mMulti;

    private SwipeRefreshLayout mRefreshLayout;
    //Forecast Variables
    private ForecastResultHandler mForecastResultHandler;
    private ForecastResult mForecastResult;
    private OnForecastUpdateCompleteListener mForecastListener;
    static final String F_TAG ="Forecast Home Frag";
    //Graph Variables
    private Cartesian finalGraph;
    private AnyChartView finalChartView;
    private Column weeklySneezesColumn;

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
                                Application.location = location;
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
            else if(multiNumber < 0) {
                int num;
                num = convertToPositive(multiNumber);
                removeSneezes(num);

                mBinding.setSneeze(mSneeze);
                mMulti.getMulti(1);
                mBinding.setMulti(mMulti);
            }
            else {
                handleSneeze();
                mBinding.setSneeze(mSneeze);
            }
            //update graph data
            weeklySneezesColumn.data(graphData.getWeeklyUserData());
        });

        minusButton.setOnClickListener((View v) -> {
            mMulti.getMulti(3);
            mBinding.setMulti(mMulti);
            mBinding.setSneeze(mSneeze);

            // Sneeze repo testing
            List<SneezeItem> test = repo.getSneezeItems(27, 8, 2020, 18, 9, 2020, SneezeRepository.Scope.COMBINED);
            //List<SneezeItem> test2 = repo.getSneezeItems(27, 8, 2020, 18, 9, 2020, SneezeRepository.Scope.COMBINED, true);
            Log.i("test", String.valueOf(test.size()));
            //Log.i("test", test2.toString());
        });

        plusButton.setOnClickListener((View v) -> {
            mMulti.getMulti(2);
            mBinding.setMulti(mMulti);
            mBinding.setSneeze(mSneeze);
        });

        //creates graph
        createHomeGraph(view);

        return view;
    }

    DateFormat dayFormat = new SimpleDateFormat("EEE MMM dd yyyy", Locale.US);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OnForecastUpdateCompleteListener mForecastUpdateCompleteListener = new ForecastUpdateListener();
        this.registerOnUpdateCompleteListener(mForecastUpdateCompleteListener);
        this.setupForecast();
        mRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        mRefreshLayout.setOnRefreshListener(this::refreshHome);
        styleRefreshLayout();
        getView().findViewById(R.id.changeLocation).setOnClickListener(v -> openSetLocationPopup());
        getView().findViewById(R.id.indexLayout).setOnClickListener(v -> openIndexPopup());
        getView().findViewById(R.id.btnRefreshForecast).setOnClickListener(v -> updateForecast());

    }//onViewCreated

    @Override
    public void onResume() {
        super.onResume();
        setupForecast();
    }

    public int convertToPositive(int number) {

        number = number * -1;
        return number;
    }

    public void removeSneezes(int amount) {
        RealmResults<SneezeItem> currentSneezes = repo.getSneezeItems(new Date(), SneezeRepository.Scope.USER);

        for (int i = 0; i < amount; i++){
            if (currentSneezes.get(0).getSneezes().size() <= 0) break;
            realm.executeTransaction(r -> {
                currentSneezes.get(0).getSneezes().deleteLastFromRealm();
            });
        }
    }

    private void handleSneeze() {
        RealmQuery<SneezeItem> searchForCurrentDateQuery = realm.where(SneezeItem.class)
                .equalTo(SneezeItem.Fields.DATE, dayFormat.format(new Date()))
                .equalTo(SneezeItem.Fields.OWNER_ID, user.getId());

        if (searchForCurrentDateQuery.count() != 0) {
            updateCurrentSneeze();
        } else {
            createNewSneeze();
        }
    }

    private void createNewSneeze() {
        realm.executeTransaction(r -> {
            SneezeData sd = new SneezeData(
                    new Date().toString(),
                    getLocation());
            RealmList<SneezeData> sdl = new RealmList<>();
            sdl.add(sd);

            SneezeItem sneeze = new SneezeItem(
                    dayFormat.format(new Date()), user.getId(), sdl);

            realm.insert(sneeze);
        });
    }

    private void updateCurrentSneeze() {
        realm.executeTransaction(r -> {
            SneezeItem sneeze = realm.where(SneezeItem.class)
                    .equalTo(SneezeItem.Fields.DATE, dayFormat.format(new Date()))
                    .equalTo(SneezeItem.Fields.OWNER_ID, user.getId())
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
        if (location != null) {
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());

            Log.e("app", location.getLatitude() + "," + location.getLongitude());
        }
        return lat + "," + lng;
    }

    private void refreshHome(){
        updateForecast();
        mRefreshLayout.setRefreshing(false);//must call setRefreshing(false) at end of the method
    }


    private void openSetLocationPopup() {
        upDatePollenForecastView();
        final String TAG = "Forecast - setLocationPopup";
        Log.d(TAG, "launched set location popup");
        int cityNo = mForecastResult.getSelectedCityNo();
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        final View popupView = getLayoutInflater().inflate(R.layout.popup_set_location, null);
        //check the radiobutton which corresponds to the city that has been set in forecastResult.selectedCity
        final RadioGroup radioGroupCities = popupView.findViewById(R.id.radioGCities);
        ((RadioButton) radioGroupCities.getChildAt(cityNo)).setChecked(true);
        //show the dialog
        mBuilder.setView(popupView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        AtomicBoolean updateRequired = new AtomicBoolean(false);
        //save button will set the value selectedCityNo to access the city name and url
        Button saveBtn = popupView.findViewById(R.id.locationSaveBtn);
        Button cancelBtn = popupView.findViewById(R.id.locationCancelBtn);
        //save onclick
        saveBtn.setOnClickListener(v -> {
            upDatePollenForecastView();
            //get the selected radio button to save it to the selectedLocationNo
            int selectedCityId = radioGroupCities.getCheckedRadioButtonId();
            RadioButton selectedCityRadio = popupView.findViewById(selectedCityId);
            String selectedCity = selectedCityRadio.getText().toString().replace("_radio", "");
            //get the index of the city that was selected and save it to the forecastResult
            if (mForecastResult.getCityIndex(selectedCity) != -1 && cityNo != mForecastResult.getCityIndex(selectedCity)) {
                makeNewForecast(mForecastResult.getCityIndex(selectedCity));
                Log.d(TAG, "Location successfully set to " + mForecastResult.getCityName(mForecastResult.getSelectedCityNo()));
            }
            else if(mForecastResult.getCityIndex(selectedCity) != -1 && cityNo == mForecastResult.getCityIndex(selectedCity)){
                Log.i("forecastResult", "Same city was selected, no update will be made");
            }
            else {
                Log.e(TAG, "Developer Error: selectedCity value does not exist");
            }
            dialog.dismiss();
//            updateRequired.set(true);
        });//save onClickListener END
        cancelBtn.setOnClickListener(v ->{
            dialog.dismiss();
        });
    }// setLocationPopup END

    private void openIndexPopup(){
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        final View popupView = getLayoutInflater().inflate(R.layout.popup_index_details, null);
        //build dialog
        mBuilder.setView(popupView);
        final AlertDialog dialog = mBuilder.create();
        //set drawable for imageButton based on theme
        Drawable img;
        if(sharedPref.loadNightModeState()){
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
        closeBtn.setOnClickListener(v -> dialog.dismiss());//closeBtn onClick END
        //open www.weatherzone.com
        weatherzoneLinkBtn.setOnClickListener(v -> {
            String url = "https://www.weatherzone.com.au";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });//weatherzoneLinkBtn onClick END
    }//openIndexPopup END

    public void upDatePollenForecastView() {
        mForecastResult = mForecastResultHandler.getForecastResult();
        String packageName = getActivity().getBaseContext().getPackageName();
        View view = getView();
        Resources resources = getResources();
        // if location has been fetched
        if(mForecastResult.getIndexValues().size() == 4){
            //update location textview
            TextView pollenLocationTxt = view.findViewById(R.id.pollenCountLocationTxt);
            pollenLocationTxt.setText(mForecastResult.getCityName(mForecastResult.getSelectedCityNo()) + ", " + mForecastResult.getStateName(mForecastResult.getSelectedCityNo()));
            //update date textView
            TextView pollenForecastDateTxt = view.findViewById(R.id.txtForecastUpdateDate);
            pollenForecastDateTxt.setText("Updated: "+ mForecastResult.getUpdateDateAsString());
            final int numDays = 4;
            ConstraintLayout constraintLayout = view.findViewById(R.id.homeConstraintLayout);

            //background colour variables
            final String[] coloursNames = new String[]{"lowColour", "moderateColour", "highColour", "vHighColour", "extremeColour"};
            final ArrayList<Integer> indexValueNums = mForecastResult.getIndexValues();

            //day name variables
            SimpleDateFormat dayStr = new SimpleDateFormat("EEE", Locale.US);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mForecastResult.getUpdateDateInMillis());

            //loop through forecast textViews/Background and set the corresponding colour/day
            for (int i = 0; i < numDays; i++) {
                int colorValue;
                //get background colour value for forecast
                try {
                    int colorID = resources.getIdentifier(coloursNames[indexValueNums.get(i)], "color", packageName);
                    colorValue = ResourcesCompat.getColor(resources, colorID,null);
//                    String hexColor = String.format("#%06X", (0xFFFFFF &colorValue));
                } catch (Exception ex) {
                    colorValue = ResourcesCompat.getColor(resources, R.color.black,null);
                    Log.e("ForecastResult", "An Exception was thrown\nColor Not found\n" + ex);
                }
                //get background element and set value on UI
                try {
                    String imgName = "forecastImage" + (i + 1);
                    int imgID = resources.getIdentifier(imgName, "id", packageName);
                    ImageView dayImgView = constraintLayout.findViewById(imgID);
                    int[][] states = new int[][] {
                            new int[] {} // enabled
                    };
                    int[] color = new int[] {
                            colorValue
                    };
                    dayImgView.setBackgroundTintList(new ColorStateList(states, color));
                }
                catch (Exception ex) {
                    Log.e("ForecastResult", "An Exception was thrown\nDay Img cant be found\n" + ex);
                }

                //get and edit text View
                String txtName = "forecastTextBlock" + (i + 1);
                int txtID = resources.getIdentifier(txtName, "id", packageName);
                TextView dayNameTxt = constraintLayout.findViewById(txtID);
                dayNameTxt.setText(dayStr.format(c.getTime()).toUpperCase());
                c.add(Calendar.DATE, 1);
            }//for END
        }//if ForecastResult Fetched END
    }//upDatePollenForecastView END

    private void styleRefreshLayout(){
        TypedValue bgValue = new TypedValue();
        TypedValue fgValue = new TypedValue();
        int bgColor;
        int fgColor;
        if (getContext().getTheme().resolveAttribute(R.attr.sectionColor, bgValue, true)  && getContext().getTheme().resolveAttribute(R.attr.mainText, fgValue, true) ){
            fgColor = fgValue.data;
            bgColor = bgValue.data;
        }
        else{
            bgColor = Color.WHITE;
            fgColor = Color.BLACK;
        }
        mRefreshLayout.setProgressBackgroundColorSchemeColor(bgColor);
        mRefreshLayout.setColorSchemeColors(fgColor);
    }

    // sets the event listener passed on runtime
    public void registerOnUpdateCompleteListener(OnForecastUpdateCompleteListener mForecastListener){
        this.mForecastListener = mForecastListener;
    }

    //Async ForecastResultHandler Tasks
    //initialises forecast instance
    public void setupForecast(){
        new Thread(() -> {
            mForecastResultHandler = new ForecastResultHandler(getContext());
            mForecastResult = mForecastResultHandler.getForecastResult();

            if (mForecastListener != null) {
                Log.d(F_TAG, "SetupForecast Task complete invoking callback method");
                getActivity().runOnUiThread(() -> mForecastListener.onForecastUpdateComplete());
            }
            else {
                Log.e(F_TAG, "mForecastListener is null");
            }
        }).start();
    }

    //called when force updating a forecast
    public void updateForecast(){
        new Thread(() -> {
            //only true if the location has been changed as seen on makeNewForecast()
            mForecastResultHandler.fetchForecastFromWeb(false);

            if (mForecastListener != null) {
                Log.d(F_TAG, "UpdateForecast Task complete invoking callback method");
                // invoke the callback method
                getActivity().runOnUiThread(() -> mForecastListener.onForecastUpdateComplete());
            }
            else {
                Log.e(F_TAG, "mForecastListener is null");
            }
        }).start();
    }

    //called when location is changed
    public void makeNewForecast(int locationIndex){
        new Thread(() -> {
            mForecastResultHandler.changeLocation(locationIndex);
            mForecastResultHandler.fetchForecastFromWeb(true);

            if (mForecastListener != null) {
                Log.d(F_TAG, "Task complete calling callback method");
                // invoke the callback method
                getActivity().runOnUiThread(() -> mForecastListener.onForecastUpdateComplete());
            }
            else {
                Log.e(F_TAG, "mForecastListener is null");
            }
        }).start();
    }

    class ForecastUpdateListener implements OnForecastUpdateCompleteListener{
        @Override
        public void onForecastUpdateComplete() {
            Log.d(F_TAG,"onUpdateComplete invoked");
            upDatePollenForecastView();
        }
    }//ForecastHandler end

    void createHomeGraph(View view){
        finalChartView = view.findViewById(R.id.any_chart_view);
        finalChartView.setProgressBar(view.findViewById(R.id.progress_bar));

        finalGraph = AnyChart.column();

        weeklySneezesColumn = finalGraph.column(graphData.getWeeklyUserData());

        weeklySneezesColumn.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(1d)
                .format("{%Value}{groupsSeparator: }");

        finalGraph.animation(true);
        //cartesian.title("DailySneezes");

        finalGraph.yScale()
                .minimum(0d)
                .ticks().interval(1);

        finalGraph.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        finalGraph.tooltip().positionMode(TooltipPositionMode.POINT);
        finalGraph.interactivity().hoverMode(HoverMode.BY_X);

        //cartesian.xAxis(0).title("Your Week");
        //cartesian.yAxis(0).title("Sneezes");

        //Appearance Options
        TypedArray c1;
        TypedArray c2;

        if (sharedPref.loadNightModeState()){
            c1 = getContext().getTheme().obtainStyledAttributes(
                    R.style.darkTheme,
                    new int[] { R.attr.sectionColor });
            c2 = getContext().getTheme().obtainStyledAttributes(
                    R.style.darkTheme,
                    new int[] { R.attr.colorAccent });
        } else {
            c1 = getContext().getTheme().obtainStyledAttributes(
                    R.style.AppTheme,
                    new int[] { R.attr.sectionColor });
            c2 = getContext().getTheme().obtainStyledAttributes(
                    R.style.AppTheme,
                    new int[] { R.attr.colorAccent });
        }

        // Get color hex code (eg, #fff) and format string to match API conventions
        //ColorPrimary
        int intColor1 = c1.getColor(0 /* index */, 0 /* defaultVal */);
        String colorPrimary = "#" +Integer.toHexString(intColor1).substring(2);
        //ColorAccent
        int intColor2 = c2.getColor(0 /* index */, 0 /* defaultVal */);
        String colorAccent = "#" +Integer.toHexString(intColor2).substring(2);

        finalGraph.background().fill(colorPrimary);
        weeklySneezesColumn.color(colorAccent);
        //sets background during load
        finalChartView.setBackgroundColor(colorPrimary);

        finalChartView.setChart(finalGraph);
        // Recycle
        c1.recycle();
        c2.recycle();
    }//CreateGraph END
}//HomeFragment END


interface OnForecastUpdateCompleteListener{
    void onForecastUpdateComplete();
}