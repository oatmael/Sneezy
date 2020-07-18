package com.app.sneezyapplication;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.RealmList;
import io.realm.RealmQuery;


public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        final Button button = view.findViewById(R.id.sneezeButton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                handleSneeze();
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
                    new Date().toString(), getLocation((MainActivity)getActivity()));
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
                            getLocation((MainActivity)getActivity())
                    ));
        });
    }

    private String getLocation(MainActivity mainAct){
        final AtomicReference<String> latitude = new AtomicReference<>();
        final AtomicReference<String> longitude = new AtomicReference<>();

        if (mainAct.checkLocationPermission()) {
            mainAct.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null){
                            //Log.e("app", String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
                            latitude.set(String.valueOf(location.getLatitude()));
                            longitude.set(String.valueOf(location.getLongitude()));
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("location", e.getLocalizedMessage());
                        latitude.set("");
                        longitude.set("");
                    });
        } else {
            latitude.set("");
            longitude.set("");
        }

        Log.e("app", latitude.get() + "," + longitude.get());

        return latitude.get() + "," + longitude.get();
    }
}
