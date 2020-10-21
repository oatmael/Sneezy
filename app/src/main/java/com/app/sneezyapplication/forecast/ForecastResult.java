package com.app.sneezyapplication.forecast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ForecastResult {
    //CONSTANT VALUES
    private static final ArrayList<String> LOCATION_URLS = new ArrayList<>(Arrays.asList(
            "https://www.weatherzone.com.au/pollen-index/sa/adelaide/adelaide",
            "https://www.weatherzone.com.au/pollen-index/qld/brisbane/brisbane",
            "https://www.weatherzone.com.au/pollen-index/act/act/canberra",
            "https://www.weatherzone.com.au/pollen-index/tas/lower-derwent/hobart",
            "https://www.weatherzone.com.au/pollen-index/vic/melbourne/melbourne",
            "https://www.weatherzone.com.au/pollen-index/wa/perth/perth",
            "https://www.weatherzone.com.au/pollen-index/nsw/sydney/sydney"));
    //stores Names of cities
    private static final ArrayList<String> CITY_NAMES = new ArrayList<>(Arrays.asList("Adelaide", "Brisbane", "Canberra", "Hobart", "Melbourne", "Perth", "Sydney"));
    //sores Names of States
    private static final ArrayList<String> CITY_STATES = new ArrayList<>(Arrays.asList("SA", "QLD", "ACT", "TAS", "VIC", "WA", "NSW"));
    private static final ArrayList<String> POLLEN_INDEX_VALUES = new ArrayList<>(Arrays.asList("LOW", "MODERATE", "HIGH", "VERY_HIGH", "EXTREME"));
    private static final int DEFAULT_SELECTED_CITY_NO = 0;//default selected location number

    //VARIABLES
    private ArrayList<String> forecastList = new ArrayList<>();//stores the forecast result for each day
//    private ArrayList<Integer> forecastValueNums;//stores the forecast value index for each day (e.g. low = 0, extreme = 4)
    private int selectedCityNo;//stores index number of the location to be used
    private Calendar updateDate;
    private String yesterday;
    private String updateConclusion;
    //CONSTRUCTORS
    public ForecastResult(){
        this(DEFAULT_SELECTED_CITY_NO);
    }

    public ForecastResult(int selectedCityNo){
        this(selectedCityNo, Calendar.getInstance().getTimeInMillis());
    }

    public ForecastResult(int selectedCityNo, long updateTime){
        updateDate = Calendar.getInstance();
        this.updateDate.setTimeInMillis(updateTime);
        this.selectedCityNo = selectedCityNo;
        yesterday = null;
    }
    //GETTERS, SETTERS AND METHODS
    public void setSelectedCityNo(int selectedCityNo) {
        this.selectedCityNo = selectedCityNo;
    }

    public int getSelectedCityNo() {
        return selectedCityNo;
    }

    public int getCityIndex(String selectedCity){
        return CITY_NAMES.indexOf(selectedCity);
    }

    public String getCityName(int i){
        return CITY_NAMES.get(i);
    }
    public String getStateName(int i){return CITY_STATES.get(i); }

    public ArrayList<String> getForecastList(){
        return forecastList;
    }

    public void setForecastList(ArrayList<String> forecastList){
        this.forecastList = forecastList;
    }

    public ArrayList<Integer> getIndexValues(){
        ArrayList<Integer> forecastValueNums = new ArrayList<>();
        int valueIndex;
        String dayValue = "";
        for (int i =0; i <forecastList.size();i++) {
            dayValue = forecastList.get(i).toUpperCase();
            valueIndex = POLLEN_INDEX_VALUES.indexOf(dayValue);
            forecastValueNums.add(valueIndex);
        }
        return forecastValueNums;
    }//getIndexValues END

    public long getUpdateDateInMillis(){
        return this.updateDate.getTimeInMillis();
    }
    public void setUpdateDateInMillis(long updateDateInMillis){
        updateDate.setTimeInMillis(updateDateInMillis);
    }

    public String getYesterday() {
        return yesterday;
    }

    public void setYesterday(String yesterday) {
        this.yesterday = yesterday;
    }

    public String getUpdateDateAsString(){
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM hh:mm a");
        return simpleDate.format(updateDate.getTime());
    }

    public String getUrl() {
        return LOCATION_URLS.get(selectedCityNo);
    }

    public boolean isUpToDate(){
        final long hrInMillis = 7200000;
        TimeZone tz = TimeZone.getDefault();
        Calendar currentTime = Calendar.getInstance(tz);
        float difInMillis = currentTime.getTimeInMillis() - updateDate.getTimeInMillis();
        float difInHrs = difInMillis/ hrInMillis;
        if (difInHrs < 2 && this.forecastList.size() == 4){
            return true;
        }
        return false;
    }

    public String getUpdateConclusion() {
        return updateConclusion;
    }

    public void setUpdateConclusion(String updateConclusion) {
        this.updateConclusion = updateConclusion;
    }

    public String getForecastDay(int i) {
        return forecastList.get(i);
    }
}//ForecastObj END


