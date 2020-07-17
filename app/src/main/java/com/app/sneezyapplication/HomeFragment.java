package com.app.sneezyapplication;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.SneezeItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            SneezeItem sneeze = new SneezeItem();

            sneeze.setDate(dayFormat.format(new Date()));
            sneeze.setOwner_id(MainActivity.user.getId());

            SneezeData sd = new SneezeData();
            sd.setDate(new Date().toString());
            sd.setLocation(getLocation((MainActivity)getActivity()));

            RealmList<SneezeData> sdl = new RealmList<>();
            sdl.add(sd);
            sneeze.setSneezes(sdl);

            MainActivity.realm.insert(sneeze);
        });
    }

    private void updateCurrentSneeze(){

    }

    private String getLocation(MainActivity mainAct){
        String[] returnLocation = new String[2];

        if (mainAct.checkLocationPermission()) {
            mainAct.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null){
                            Log.e("app", String.valueOf(location.getLatitude()) + String.valueOf(location.getLongitude()));
                            returnLocation[0] = String.valueOf(location.getLatitude());
                            returnLocation[1] = String.valueOf(location.getLongitude());
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("location", e.getLocalizedMessage());
                        returnLocation[0] = "";
                        returnLocation[1] = "";
                    });
        } else {
            returnLocation[0] = "";
            returnLocation[1] = "";
        }


        return returnLocation[0] + "," + returnLocation[1];
    }
}
