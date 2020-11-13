package com.app.sneezyapplication.data;

import com.app.sneezyapplication.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.realm.RealmList;
import io.realm.RealmResults;

import static com.app.sneezyapplication.MainActivity.realm;
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

    public RealmResults getSneezeItems(Date date, Scope scope){
        // Did you know? Java date.getYear returns an offset of the year - 1900. Why? We may never know.
        return getSneezeItems(date.getDate(), date.getMonth() + 1, date.getYear() + 1900, scope);
    }
    public RealmResults getSneezeItems(Date date, Date date2, Scope scope){
        return getSneezeItems(date.getDate(), date.getMonth() + 1, date.getYear() + 1900, date2.getDate(), date2.getMonth() + 1, date2.getYear() + 1900, scope);
    }

    public RealmResults getSneezeItems(int day, int month, int year, Scope scope){
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

    public RealmResults<SneezeItem> getSneezeItems(int day, int month, int year, int day2, int month2, int year2, Scope scope){
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

    public void removeSneezes(int amount) {
        RealmResults<SneezeItem> currentSneezes = getSneezeItems(new Date(), Scope.USER);

        for (int i = 0; i < amount; i++){
            if (currentSneezes.get(0).getSneezes().size() <= 0) break;
            realm.executeTransaction(r -> {
                currentSneezes.get(0).getSneezes().deleteLastFromRealm();
            });
        }
    }

    public static float sneezeItemAverage(RealmResults<SneezeItem> sneezeItems, boolean fillEmpty){
        float avg = 0;
        int tally = 0;

        for (SneezeItem s : sneezeItems) {
            tally += s.getSneezes().size();
        }

        int diff = 0;
        if (fillEmpty){
            Calendar c1 = Calendar.getInstance();
            Date d1 = sneezeItems.get(0).getSneezes().get(0).dateAsAndroidDate();
            c1.set(d1.getYear(), d1.getMonth() - 1, d1.getDate());
            Calendar c2 = Calendar.getInstance();
            Date d2 = new Date();
            c2.set(d2.getYear(), d2.getMonth() - 1, d2.getDate());

            diff = (int)((c2.getTime().getTime() - c1.getTime().getTime()) / (1000 * 60 * 60 * 24)) - sneezeItems.size();
        }

        avg = tally / (sneezeItems.size() + diff);

        return avg;
    }

    public static int[] sneezeItemDays(RealmResults<SneezeItem> sneezeItems){
        // starting at monday
        int[] days = {0,0,0,0,0,0,0};

        for (SneezeItem s : sneezeItems){
            if      (s.getDate().contains("Mon")) { days[0] += s.getSneezes().size(); }
            else if (s.getDate().contains("Tue")) { days[1] += s.getSneezes().size(); }
            else if (s.getDate().contains("Wed")) { days[2] += s.getSneezes().size(); }
            else if (s.getDate().contains("Thu")) { days[3] += s.getSneezes().size(); }
            else if (s.getDate().contains("Fri")) { days[4] += s.getSneezes().size(); }
            else if (s.getDate().contains("Sat")) { days[5] += s.getSneezes().size(); }
            else if (s.getDate().contains("Sun")) { days[6] += s.getSneezes().size(); }
        }

        return days;
    }

    public static List<SneezeItem> outlierCull(RealmResults<SneezeItem> sneezeItems){
        List<SneezeItem> set = sneezeItems;

        int[] testSet = new int[set.size()];
        for (int i = 0; i > set.size(); i++){
            testSet[i] = set.get(i).getSneezes().size();
        }
        Arrays.sort(testSet);

        // potential 0 index bugs
        int median = (int) Math.floor(set.size() / 2);
        int Q1 = 0 + (int) Math.floor(median / 2);
        int Q3 = median + (int) Math.floor(median / 2);
        int IQR = testSet[Q3] - testSet[Q1];

        for (int i = 0; i > set.size(); i++) {
            if (set.get(i).getSneezes().size() > (IQR * 1.5)){
                set.remove(i);
            }
        }

        return set;
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
