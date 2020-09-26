package com.app.sneezyapplication.data;

import com.app.sneezyapplication.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    public RealmResults getSneezeItems(Date date, Scope scope, boolean outlierCull){
        // Did you know? Java date.getYear returns an offset of the year - 1900. Why? We may never know.
        return getSneezeItems(date.getDate(), date.getMonth() + 1, date.getYear() + 1900, scope, outlierCull);
    }
    public RealmResults getSneezeItems(Date date, Date date2, Scope scope, boolean outlierCull){
        return getSneezeItems(date.getDate(), date.getMonth() + 1, date.getYear() + 1900, date2.getDate(), date2.getMonth() + 1, date2.getYear() + 1900, scope, outlierCull);
    }

    public RealmResults getSneezeItems(int day, int month, int year, Scope scope, boolean outlierCull){
        Calendar date = Calendar.getInstance();
        date.set(year, month - 1, day); // months are zero based in calendar? cool thanks I guess

        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd yyyy", Locale.US);

        RealmResults<SneezeItem> results = null;

        switch (scope){
            case USER:
                results = MainActivity.realm.where(SneezeItem.class)
                        .equalTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId())
                        .and()
                        .equalTo(SneezeItem.Fields.DATE, df.format(date.getTime()))
                        .findAll();
                break;
            case NOT_USER:
                results = MainActivity.realm.where(SneezeItem.class)
                        .notEqualTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId())
                        .and()
                        .equalTo(SneezeItem.Fields.DATE, df.format(date.getTime()))
                        .findAll();
                break;
            case COMBINED:
                results = MainActivity.realm.where(SneezeItem.class)
                        .equalTo(SneezeItem.Fields.DATE, df.format(date.getTime()))
                        .findAll();
                break;
            default:
                break;
        }

        return results;
    }

    public RealmResults<SneezeItem> getSneezeItems(int day, int month, int year, int day2, int month2, int year2, Scope scope, boolean outlierCull){
        Calendar date = Calendar.getInstance();
        date.set(year, month - 1, day);

        Calendar date2 = Calendar.getInstance();
        date2.set(year2, month2 - 1, day2);

        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd yyyy", Locale.US);

        RealmResults<SneezeItem> results = null;

        // We do something awful
        // Generate all dates between range in a list
        // .in(list[])
        // yikes

        List<String> dateRange = new ArrayList<>();

        date2.add(Calendar.DATE, 1);
        while (date.compareTo(date2) != 0) {
            dateRange.add(df.format(date.getTime()));
            date.add(Calendar.DATE, 1);
        }

        String[] dateRangeStr = new String[dateRange.size()];
        dateRangeStr = dateRange.toArray(dateRangeStr);

        switch (scope){
            case USER:
                results = MainActivity.realm.where(SneezeItem.class)
                        .equalTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId())
                        .and()
                        .in(SneezeItem.Fields.DATE, dateRangeStr)
                        .findAll();

                break;
            case NOT_USER:
                results = MainActivity.realm.where(SneezeItem.class)
                        .notEqualTo(SneezeItem.Fields.OWNER_ID, MainActivity.user.getId())
                        .and()
                        .in(SneezeItem.Fields.DATE, dateRangeStr)
                        .findAll();
                break;
            case COMBINED:
                results = MainActivity.realm.where(SneezeItem.class)
                        .in(SneezeItem.Fields.DATE, dateRangeStr)
                        .findAll();
                break;
            default:
                break;
        }

        return results;
    }

    public float sneezeItemAverge(RealmResults<SneezeItem> sneezeItems){
        float avg = 0;

        return avg;
    }

    public enum Scope {
        USER,
        NOT_USER,
        COMBINED
    }

    public void addListener(ListChangeListener listener) {
        listeners.add(listener);
    }
    public interface ListChangeListener {
        void onListChange();
    }



}
