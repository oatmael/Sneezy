package com.app.sneezyapplication.data;

import com.app.sneezyapplication.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmResults;

public class SneezeRepository {

    private List<SneezeItem> allSneezeItems;
    private List<SneezeItem> allUserSneezeItems;
    private List<SneezeItem> monthlyUserSneezeItems;

    public List<SneezeItem> getAllSneezeItems() {
        updateAllSneezes();
        return allSneezeItems;
    }
    public List<SneezeItem> getAllUserSneezeItems() {
        updateUserSneezes();
        return allUserSneezeItems;
    }
    public List<SneezeItem> getMonthlyUserSneezeItems() {
        updateUserMonthlySneezes();
        return monthlyUserSneezeItems;
    }

    public SneezeRepository(){
        allSneezeItems = new ArrayList<>();
        allUserSneezeItems = new ArrayList<>();
        monthlyUserSneezeItems = new ArrayList<>();
    }

    public void updateRecords() {
        updateAllSneezes();
        updateUserSneezes();
        updateUserMonthlySneezes();

    }

    private void updateAllSneezes(){
        RealmResults<SneezeItem> allSneezes = MainActivity.realm.where(SneezeItem.class)
                .notEqualTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId())
                .findAll();

        allSneezeItems = allSneezes;

    }

    private void updateUserSneezes(){
        RealmResults<SneezeItem> userSneezes = MainActivity.realm.where(SneezeItem.class)
                .equalTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId())
                .findAll();

        allUserSneezeItems = userSneezes;

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
