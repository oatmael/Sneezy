package com.app.sneezyapplication.data;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.app.sneezyapplication.MainActivity.repo;

public class GraphData {

    private List<DataEntry> weeklyUserData;

    public List<DataEntry> getWeeklyUserData() {
        updateWeeklyUserData();
        return weeklyUserData;
    }

    private void updateWeeklyUserData(){
        weeklyUserData = getGraphableData(repo.getAllUserSneezeItems(), "Week");
    }

    public GraphData(){
        weeklyUserData = new ArrayList<>();
    }

    private List<DataEntry> getGraphableData(List<SneezeItem> _sneezeList, String forGraph) {

        int days = 0;
        if (forGraph == "Week")
        {
            days = 7;
        }

        String date = new Date().toString();
        int sneezes = 0;
        List<SneezeItem> sneezeList = _sneezeList;
        List<DataEntry> data = new ArrayList<>();
        //gets todays calender date to work back from
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        if (sneezeList.size() != 0) {
            for (int i = days; i > 0; i--) {
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
                data.add(new ValueDataEntry(c.getTime().toString().substring(0, 3), sneezes));
                //take one from calender date to check previous day
                c.add(Calendar.DATE, -1);
            }
        }
        return data;
    }

    private Boolean compareDate(String d1, String d2){
        if (d1.substring(0, 7).equals(d2.substring(0,7))){
            return true;
        }else
            return false;
    }
}
