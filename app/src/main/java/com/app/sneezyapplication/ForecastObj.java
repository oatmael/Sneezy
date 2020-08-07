package com.example.sneezyapplication;

import java.util.ArrayList;
import java.util.Arrays;


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
    private static final int DEFAULT_SELECTED_CITY_NO = 0;//default selected location number
    //VARIABLES
    private ArrayList<String> daysList = new ArrayList<>();//stores the forecast result for each day
    private int selectedCityNo;//stores index number of the location to be used
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
        this.selectedCityNo= selectedCityNo;
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

    public String getCity(int i){
        return CITY_NAMES.get(i);
    }

//    public  int getNumCities(){
//        return CITY_NAMES.size();
//    }

    public ArrayList<String> getDaysList(){
        return daysList;
    }

    public void setDaysList(ArrayList<String> daysList){
        this.daysList = daysList;
    }

    public String getUrl() {
        return LOCATION_URLS.get(selectedCityNo);
    }

}

