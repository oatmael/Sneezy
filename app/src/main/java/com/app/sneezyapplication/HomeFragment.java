package com.app.sneezyapplication;

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

import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.SneezeItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    DateFormat dayFormat = new SimpleDateFormat("EEE MMM dd yyyy");

    private void handleSneeze(){
        MainActivity mainAct = (MainActivity)getActivity();

        Date curTime = new Date();

        BsonDocument searchForCurrentDateQuery = new BsonDocument()
                .append(SneezeItem.Fields.DATE, new BsonString(dayFormat.format(curTime)))
                .append(SneezeItem.Fields.OWNER_ID, new BsonString(mainAct.getUserID()));

        mainAct.getItems().sync().count(searchForCurrentDateQuery).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Long numDocs = task.getResult();
                if (numDocs != 0){
                    updateCurrentSneeze(mainAct);
                } else {
                    createNewSneeze(mainAct);
                }
            } else {
                Log.e("app", "Failed to count documents with exception: ", task.getException());
            }
        });



    }

    private void createNewSneeze(MainActivity mainAct){

        Date curTime = new Date();

        List<SneezeData> sd = new ArrayList<SneezeData>();
        sd.add(new SneezeData(curTime, ""));

        mainAct.getItems().sync().insertOne(new SneezeItem(new ObjectId(), mainAct.getUserID(), dayFormat.format(curTime), sd))
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

    private void updateCurrentSneeze(MainActivity mainAct){
        //Toast.makeText(getActivity(), "Sneeze Item found", Toast.LENGTH_LONG).show();
        String location = "";
        Date curTime = new Date();

        BsonDocument filterDoc = new BsonDocument()
                .append(SneezeItem.Fields.OWNER_ID, new BsonString(mainAct.getUserID()))
                .append(SneezeItem.Fields.DATE, new BsonString(dayFormat.format(curTime)));

        BsonDocument queryDoc = new BsonDocument()
                //.append(SneezeItem.Fields.OWNER_ID, new BsonString(mainAct.getUserID()))
                .append("$addToSet",
                        new BsonDocument(SneezeItem.Fields.SNEEZES,
                                new BsonDocument()
                                        .append(SneezeItem.Fields.LOCATION, new BsonString(location))
                                        .append(SneezeItem.Fields.DATE, new BsonString(curTime.toString()))));

        mainAct.getItems().sync().updateOne(filterDoc, queryDoc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        long numMatched = task.getResult().getMatchedCount();
                        long numModified = task.getResult().getModifiedCount();
                        Log.d("app", String.format("successfully matched %d and modified %d documents",
                                numMatched, numModified));
                    } else {
                        Log.e("app", "failed to update document with: ", task.getException());
                    }
                });
    }
}
