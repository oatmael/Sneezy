package com.app.sneezyapplication.data;

import android.icu.text.SimpleDateFormat;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.app.sneezyapplication.MainActivity.repo;

public class GraphData {

    private List<DataEntry> weeklyUserData;
    private List<DataEntry> monthlyUserData;

    public List<DataEntry> getWeeklyUserData() {
        updateWeeklyUserData();
        return weeklyUserData;
    }

    public List<DataEntry> getMonthlyUserData(Calendar calendarMonth) {
        updateMonthlyUserData(calendarMonth);
        return monthlyUserData;
    }

    private void updateWeeklyUserData(){
        //gets graphable data with arguments for 7 days and todays date, this will get the last week of sneezes for the graph
        weeklyUserData = getDailyGraphableData(repo.getAllUserSneezeItems(), 7, new Date(), "dayOfWeek");
    }

    private void updateMonthlyUserData(Calendar month){
        Calendar c = Calendar.getInstance();
        c.setTime(month.getTime());
        c.set(Calendar.DAY_OF_MONTH, 0);
        int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        c.add(Calendar.DATE, daysInMonth );
        monthlyUserData = getDailyGraphableData(repo.getAllUserSneezeItems(), daysInMonth, c.getTime(), "dayOfMonth");
    }



    public GraphData(){
        weeklyUserData = new ArrayList<>();
    }

    private List<DataEntry> getDailyGraphableData(List<SneezeItem> _sneezeList, int days, Date fromDate, String labelFormat) {

        String date = fromDate.toString();
        int sneezes = 0;
        List<SneezeItem> sneezeList = _sneezeList;
        List<DataEntry> data = new ArrayList<>();
        //gets selected calender date to work back from
        Calendar c = Calendar.getInstance();
        c.setTime(fromDate);


        for (int i = 0; i < days; i++) {
            sneezes = 0;

            for (SneezeItem s : sneezeList) {
                date = s.getDate();

                if (compareDate(date, c.getTime().toString())) {
                    for (SneezeData d : s.getSneezes()) {
                        sneezes++;
                    }
                    //removes SneezeItems once accounted for
                    //sneezeList.remove(s);
                }
            }
            //adds at index 0 for chronological order
            //displays only first 3 chars of date string
            if (labelFormat == "dayOfWeek") {
                data.add(0, new ValueDataEntry(new SimpleDateFormat("EE", Locale.ENGLISH).format(c.getTime()), sneezes));
            }
            else if (labelFormat == "dayOfMonth"){
                data.add(0, new ValueDataEntry(new SimpleDateFormat("dd", Locale.ENGLISH).format(c.getTime()), sneezes));
            }
            //take one from calender date to check previous day
            c.add(Calendar.DATE, -1);
        }

        return data;
    }

    private Boolean compareDate(String d1, String d2){
        if (d1.substring(0,10).equals(d2.substring(0,10))){
            return true;
        }else
            return false;
    }
}
