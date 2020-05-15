package com.example.sneezyapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.sneezyapplication.data.SneezeData;
import com.example.sneezyapplication.data.SneezeItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private void handleSneeze(){
        MainActivity mainAct = (MainActivity)getActivity();

        DateFormat dayFormat = new SimpleDateFormat("EEE MMM dd");
        Date curTime = new Date();

        List<SneezeData> sd = new ArrayList<SneezeData>();
        sd.add(new SneezeData(curTime, ""));

        mainAct.getItems().insertOne(new SneezeItem(new ObjectId(), mainAct.getUserID(), curTime, sd))
                .addOnSuccessListener(new OnSuccessListener<RemoteInsertOneResult>() {
                    @Override
                    public void onSuccess(RemoteInsertOneResult remoteInsertOneResult) {
                        Toast.makeText(getActivity(), "Successfully added to DB", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add to DB", Toast.LENGTH_LONG).show();
                        Log.e("MONGODB ERR", e.toString());
                    }
                });

    }
}
