package com.app.sneezyapplication.forecast;

import com.anychart.scales.DateTime;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ForecastObj {
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
    private ArrayList<Integer> forecastValueNums;//stores the forecast value index for each day (e.g. low = 0, extreme = 4)
    private int selectedCityNo;//stores index number of the location to be used
    Date updateDate;
    //TODO maybe
    //make datetime variable to store when the forecast was last updated
    //add bool: isUpToDate which contains a method to check if the forecast is more than x hours old
    //--if returns TRUE the forecast will update
    //--if returns FALSE nothing will happen

    //CONSTRUCTORS
    public ForecastObj(){
        this(DEFAULT_SELECTED_CITY_NO);
    }

    public ForecastObj(int selectedCityNo){
        this.selectedCityNo = selectedCityNo;
    }

    //GETTERS AND SETTERS
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

//    public ArrayList<String> getForecastList(){
//        return forecastList;
//    }

    public void setForecastList(ArrayList<String> forecastList){
        this.forecastList = forecastList;
    }

    public ArrayList<Integer> getIndexValues(){
        forecastValueNums = new ArrayList<>();
        int valueIndex;
        String dayValue = "";
        for (int i =0; i <forecastList.size();i++) {
            dayValue = forecastList.get(i).toUpperCase();
            valueIndex = POLLEN_INDEX_VALUES.indexOf(dayValue);
            forecastValueNums.add(valueIndex);
        }
        return forecastValueNums;
    }//getIndexValues END

    public Date getForecastUpdateDate(){
        return this.updateDate;
    }
    public void setForecastUpdateDate(Date updateDate){
        this.updateDate = updateDate;
    }

    public String getForecastUpdateDateAsString(){
    String date =new SimpleDateFormat("dd-MM HH:mm").toString();
//    SimpleDateFormat date =new SimpleDateFormat("dd-MM HH:mm");
    return date;
    }
    public String getUrl() {
        return LOCATION_URLS.get(selectedCityNo);
    }

}//ForecastObj


