package com.example.sneezyapplication.data;

import com.example.sneezyapplication.MainActivity;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;

import org.bson.BsonDocument;
import org.bson.BsonString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SneezeRepository {

    private List<SneezeItem> allSneezeItems;
    private List<SneezeItem> allUserSneezeItems;
    private List<SneezeItem> monthlyUserSneezeItems;

    public List<SneezeItem> getAllSneezeItems() {
        return allSneezeItems;
    }
    public List<SneezeItem> getAllUserSneezeItems() {
        return allUserSneezeItems;
    }
    public List<SneezeItem> getMonthlyUserSneezeItems() { return monthlyUserSneezeItems; }

    public SneezeRepository(){
        allSneezeItems = new ArrayList<>();
        allUserSneezeItems = new ArrayList<>();
        monthlyUserSneezeItems = new ArrayList<>();
    }

    public void updateRecords(MainActivity activity) {

        updateAllSneezes(activity);
        updateUserSneezes(activity);
        updateUserMonthlySneezes();

    }

    private void updateAllSneezes(MainActivity activity){
        BsonDocument allRecordsFilter = new BsonDocument()
                .append(SneezeItem.Fields.OWNER_ID, new BsonDocument()
                        .append("$ne", new BsonString(activity.getUserID())));

        // Potentially omit owner_id from result for more security?
        BsonDocument allRecordsProjectionFilter = new BsonDocument()
                .append(SneezeItem.Fields.OWNER_ID, new BsonString("0"));

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
    }

    private void updateUserSneezes(MainActivity activity){
        BsonDocument userRecordsFilter = new BsonDocument()
                .append(SneezeItem.Fields.OWNER_ID, new BsonString(activity.getUserID()));

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

    private void updateUserMonthlySneezes(){
        DateFormat monthFormat = new SimpleDateFormat("MMM");
        Date date = new Date();
        String month = monthFormat.format(date);

        for (SneezeItem s : allUserSneezeItems){
            if (s.getDate().contains(month)){
                monthlyUserSneezeItems.add(s);
            }
        }
    }
    private void updateUserWeeklySneezes(){

    }


}
