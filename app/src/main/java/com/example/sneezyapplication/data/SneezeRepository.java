package com.example.sneezyapplication.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.sneezyapplication.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;

import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SneezeRepository {

    private List<SneezeItem> allSneezeItems;
    private List<SneezeItem> allUserSneezeItems;

    public List<SneezeItem> getAllSneezeItems() {
        return allSneezeItems;
    }
    public List<SneezeItem> getAllUserSneezeItems() {
        return allUserSneezeItems;
    }

    public SneezeRepository(){
        allSneezeItems = new ArrayList<>();
        allUserSneezeItems = new ArrayList<>();
    }

    public void updateRecords(MainActivity activity) {

        BsonDocument allRecordsFilter = new BsonDocument()
                .append("$not", new BsonDocument()
                        .append(SneezeItem.Fields.OWNER_ID, new BsonString(activity.getUserID())));

        // Potentially omit owner_id from result for more security?
        BsonDocument allRecordsProjectionFilter = new BsonDocument()
                .append(SneezeItem.Fields.OWNER_ID, new BsonString("0"));

        BsonDocument userRecordsFilter = new BsonDocument()
                .append(SneezeItem.Fields.OWNER_ID, new BsonString(activity.getUserID()));



        RemoteFindIterable allRecordsResults = activity.getItems()
                .find(allRecordsFilter)
                //.projection(allRecordsProjectionFilter);
                .projection(new BsonDocument())
                .sort(new BsonDocument());

        Task<List<SneezeItem>> allRecordsTask = allRecordsResults.into(this.allSneezeItems);
        allRecordsTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                // Do something on update?
            }
        });

        RemoteFindIterable userRecordsResults = activity.getItems()
                .find(userRecordsFilter)
                .projection(new BsonDocument())
                .sort(new BsonDocument());;

        Task<List<SneezeItem>> userRecordsTask = userRecordsResults.into(this.allUserSneezeItems);
        userRecordsTask.addOnCompleteListener(task -> {
           if (task.isSuccessful()){
                // Do something on update?
           }
        });
    }


}
