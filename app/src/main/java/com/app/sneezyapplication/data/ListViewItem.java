package com.app.sneezyapplication.data;

import com.anychart.chart.common.dataentry.DataEntry;

import java.util.List;

public class ListViewItem {
    private List<DataEntry> dataList;
    private int dailyTotal;

    public List<DataEntry> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataEntry> dataList) {
        this.dataList = dataList;
    }

    public int getDailyTotal() {
        return dailyTotal;
    }

    public void setDailyTotal(int dailyTotal) {
        this.dailyTotal = dailyTotal;
    }
}
