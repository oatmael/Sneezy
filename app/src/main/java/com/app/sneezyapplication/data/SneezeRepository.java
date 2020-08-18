package com.app.sneezyapplication.data;

import com.app.sneezyapplication.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.RealmList;
import io.realm.RealmResults;

import static com.app.sneezyapplication.MainActivity.repo;

public class SneezeRepository {

    private List<ListChangeListener> listeners = new ArrayList<>();

    private List<SneezeItem> allSneezeItems;
    private List<SneezeItem> allUserSneezeItems;
    private List<SneezeItem> monthlyUserSneezeItems;
    private List<SneezeItem> weeklyUserSneezeItems;
    private SneezeItem todayUserSneezeItem;

    private Integer todaysSneeze;

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
        return weeklyUserSneezeItems;
    }
    public SneezeItem todayUserSneezeItems() {
        updateUserTodaySneeze();
        return todayUserSneezeItem;
    }

    public List<SneezeItem> getWeeklyDummyData(){
        ArrayList<SneezeItem> dummyList = new ArrayList<>();

        Random r = new Random();
        int rSneeze;
        int maxSneezes = 6;
        RealmList<SneezeData> sl;
        SneezeItem si;
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        for (int d = 0; d < 7; d++) {
            sl = new RealmList<SneezeData>();
            rSneeze = r.nextInt(maxSneezes);
            for (int i = 0; i < rSneeze; i++) {
                SneezeData sd = new SneezeData(new Date().toString(), "111,111");
                sl.add(sd);
            }
            si = new SneezeItem(c.getTime().toString(), "ownerID01", sl);
            dummyList.add(si);
            c.add(Calendar.DATE, -1);
        }
        return dummyList;
    }

    public SneezeRepository(){
        allSneezeItems = new ArrayList<>();
        allUserSneezeItems = new ArrayList<>();
        monthlyUserSneezeItems = new ArrayList<>();
        weeklyUserSneezeItems = new ArrayList<>();
        todayUserSneezeItem = new SneezeItem();

    }

    public void updateRecords() {
        updateAllSneezes();
        updateUserSneezes();
        updateUserMonthlySneezes();
        updateUserWeeklySneezes();
        updateUserTodaySneeze();

        for (ListChangeListener listener : listeners){
            listener.onListChange();
        }
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
        monthlyUserSneezeItems = new ArrayList<>();

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
        weeklyUserSneezeItems = new ArrayList<>();

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

    public void addListener(ListChangeListener listener) {
        listeners.add(listener);
    }
    public interface ListChangeListener {
        void onListChange();
    }
}

