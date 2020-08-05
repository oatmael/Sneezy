package com.app.sneezyapplication.data;

import com.app.sneezyapplication.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.RealmResults;

public class SneezeRepository {

    private List<SneezeItem> allSneezeItems;
    private List<SneezeItem> allUserSneezeItems;
    private List<SneezeItem> monthlyUserSneezeItems;
    private List<SneezeItem> weeklyUserSneezeItems;
    private SneezeItem todayUserSneezeItem;

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
    public List<SneezeItem> getWeeklyUserSneezeItems() {
        updateUserWeeklySneezes();
        return monthlyUserSneezeItems;
    }
    public SneezeItem todayUserSneezeItems() {
        updateUserTodaySneeze();
        return todayUserSneezeItem;
    }

    public SneezeRepository(){
        allSneezeItems = new ArrayList<>();
        allUserSneezeItems = new ArrayList<>();
        monthlyUserSneezeItems = new ArrayList<>();
        weeklyUserSneezeItems = new ArrayList<>();
    }

    public void updateRecords() {
        updateAllSneezes();
        updateUserSneezes();
        updateUserMonthlySneezes();
        updateUserWeeklySneezes();
        updateUserTodaySneeze();

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

    // TODO: CHANGE THIS TO REALMRESULTS QUERY. BAD.
    private void updateUserTodaySneeze() {
        Calendar current = Calendar.getInstance();
        Calendar test = Calendar.getInstance();

        current.setTime(new Date());

        for (SneezeItem s : allUserSneezeItems){
            test.setTime(s.dateAsAndroidDate());

            if (test.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)
                    && test.get(Calendar.MONTH) == current.get(Calendar.MONTH)
                    && test.get(Calendar.YEAR) == current.get(Calendar.YEAR)){
                todayUserSneezeItem = s;
                break;
            }
        }
    }

    private void updateUserMonthlySneezes(){
        Calendar current = Calendar.getInstance();
        Calendar test = Calendar.getInstance();

        current.setTime(new Date());

        for (SneezeItem s : allUserSneezeItems){
            test.setTime(s.dateAsAndroidDate());

            if (test.get(Calendar.MONTH) == current.get(Calendar.MONTH)
                    && test.get(Calendar.YEAR) == current.get(Calendar.YEAR)){
                monthlyUserSneezeItems.add(s);
            }
        }
    }

    private void updateUserWeeklySneezes(){
        Calendar current = Calendar.getInstance();
        Calendar test = Calendar.getInstance();

        current.setTime(new Date());

        for (SneezeItem s : allUserSneezeItems){
            test.setTime(s.dateAsAndroidDate());

            if (test.get(Calendar.WEEK_OF_YEAR) == current.get(Calendar.WEEK_OF_YEAR)
                    && test.get(Calendar.YEAR) == current.get(Calendar.YEAR)){
                weeklyUserSneezeItems.add(s);
            }
        }
    }



}
