package com.app.sneezyapplication;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.databinding.FragmentHomeBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.RealmList;
import io.realm.RealmQuery;

import static com.app.sneezyapplication.MainActivity.repo;


public class HomeFragment extends Fragment {


    Integer todaysSneezes;
/*    Integer todaysSneezesForText = getTodaysSneezes();*/
    int multiSneezes;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Using binding the DataBindingUtil needs to be used with inflation. The Views(view.) will remain the same and you can use as per usual.
        FragmentHomeBinding mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        View view = mBinding.getRoot();

        final Button sneezeButton = view.findViewById(R.id.sneezeButton);
        final Button minusButton = view.findViewById(R.id.minusButton);
        final Button plusButton = view.findViewById(R.id.plusButton);
        TextView todaysSneezesText = view.findViewById(R.id.sneezesTodayText);
        TextView multiText = view.findViewById(R.id.timesText);



        sneezeButton.setOnClickListener(v -> {
            /*START LOCATION CODE*/
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

        });

        return view;
    }

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
    }

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

    private int getTodaysSneezes(){
        if (repo.todayUserSneezeItems() == null) { //TODO REMOVE WHEN GOOGLE LOGIN IS WORKING CORRECTLY
            todaysSneezes = 0;
        }
        else {
            todaysSneezes = repo.todayUserSneezeItems()
                    .getSneezes()
                    .size();
        }
        return todaysSneezes;
    }



}
