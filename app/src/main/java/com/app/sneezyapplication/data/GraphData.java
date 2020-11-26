package com.app.sneezyapplication.data;

import android.icu.text.SimpleDateFormat;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.app.sneezyapplication.Application.*;

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

    public ListViewItem getListViewGraphData(Date _date){
        return getListViewGraphableData(repo.getSneezeItems(_date, SneezeRepository.Scope.USER));

    }

    private void updateWeeklyUserData(){
        //gets graphable data with arguments for 7 days and todays date, this will get the last week of sneezes for the graph
        weeklyUserData = getDailyGraphableData(repo.getWeeklyUserSneezeItems(), 7, new Date(), "dayOfWeek");
    }

    private void updateMonthlyUserData(Calendar month){
        Calendar c = Calendar.getInstance();
        c.setTime(month.getTime());
        c.set(Calendar.DAY_OF_MONTH, 0);
        int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        c.add(Calendar.DATE, daysInMonth );
        //TODO: getAllUserSneezeItems can be changed to use the new getSneezeItems method which would improve performance
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
                data.add(0, new ValueDataEntry(new SimpleDateFormat("EE", Locale.US).format(c.getTime()), sneezes));
            }
            else if (labelFormat == "dayOfMonth"){
                data.add(0, new ValueDataEntry(new SimpleDateFormat("dd", Locale.US).format(c.getTime()), sneezes));
            }
            //take one from calender date to check previous day
            c.add(Calendar.DATE, -1);
        }

        return data;
    }

    private ListViewItem getListViewGraphableData(List<SneezeItem> _sneezeList) {

        int totalSneezes = 0;
        Date date;
        int sneezes = 0;
        List<SneezeItem> sneezeList = _sneezeList;
        List<DataEntry> resultList;
        resultList = get24HourFormat();
        //gets selected calender date to work back though the days
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        for (SneezeItem s : sneezeList) {
            int prevHour = 0;

            for (SneezeData d : s.getSneezes()) {
                date = d.dateAsAndroidDate();
                int hour = date.getHours();
                if (hour == prevHour)
                    sneezes++;
                else
                    sneezes = 1;
                String label = getListViewLabelFormat(hour);
                resultList.set(hour, new ValueDataEntry(label, sneezes));
                prevHour = hour;
                totalSneezes++;
            }
            //sneezes = 0;
        }
        ListViewItem l = new ListViewItem();
        l.setDataList(resultList);
        l.setDailyTotal(totalSneezes);
        return l;
    }

    private Boolean compareDate(String d1, String d2){
        if (d1.substring(0,10).equals(d2.substring(0,10))){
            return true;
        }else
            return false;
    }

    public String getListViewLabelFormat(int hour){
        if (hour == 0)
            return "am";
        else if (hour >= 1 && hour <=12)
            return ""+hour;
        else if (hour >= 13 && hour <=23)
            return " " + (hour - 12) + " ";
        else if (hour == 24)
            return "pm";
        return "null";
    }
    private List<DataEntry> get24HourFormat(){
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("am", 0));
        data.add(new ValueDataEntry("1", 0));
        data.add(new ValueDataEntry("2", 0));
        data.add(new ValueDataEntry("3", 0));
        data.add(new ValueDataEntry("4", 0));
        data.add(new ValueDataEntry("5", 0));
        data.add(new ValueDataEntry("6", 0));
        data.add(new ValueDataEntry("7", 0));
        data.add(new ValueDataEntry("8", 0));
        data.add(new ValueDataEntry("9", 0));
        data.add(new ValueDataEntry("10", 0));
        data.add(new ValueDataEntry("11", 0));
        data.add(new ValueDataEntry("12", 0));
        data.add(new ValueDataEntry(" 1 ", 0));
        data.add(new ValueDataEntry(" 2 ", 0));
        data.add(new ValueDataEntry(" 3 ", 0));
        data.add(new ValueDataEntry(" 4 ", 0));
        data.add(new ValueDataEntry(" 5 ", 0));
        data.add(new ValueDataEntry(" 6 ", 0));
        data.add(new ValueDataEntry(" 7 ", 0));
        data.add(new ValueDataEntry(" 8 ", 0));
        data.add(new ValueDataEntry(" 9 ", 0));
        data.add(new ValueDataEntry(" 10 ", 0));
        data.add(new ValueDataEntry(" 11 ", 0));
        data.add(new ValueDataEntry("pm", 0));

        return data;
    }
}
