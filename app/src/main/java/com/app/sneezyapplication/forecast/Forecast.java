package com.app.sneezyapplication.forecast;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import com.app.sneezyapplication.R;
import com.app.sneezyapplication.SharedPref;


import org.jetbrains.annotations.NotNull;
//import org.json.JSONArray;
import org.json.JSONException;
//import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Forecast {
    private final String TAG = "Forecast";
    private ForecastResult forecastResult;
    private ForecastResult cachedResult;//only used if connection issues or is still up to date
    Context context;

    public Forecast(Context context) {
        this.context = context;
        //load cached forecast
        cachedResult = loadForecastResult();//**null for now TODO

        //load shared prefs
        int sharedPrefLocation = new SharedPref(context).loadLocationPreference();
        if(cachedResult!= null && sharedPrefLocation!= -1){
            int locationIndex = cachedResult.getSelectedCityNo();
            long updateDate = cachedResult.getUpdateDateInMillis();
            if (locationIndex == sharedPrefLocation){
                //initialise object with cached values
                forecastResult = new ForecastResult(locationIndex, updateDate);
                forecastResult.setForecastList(cachedResult.getForecastList());
                //update yesterday value based on age of forecast
                int forecastAge = cachedResult.getYesterdayAge();
                //get date for yesterday
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, -1);
                //set correct yesterday and yesterdayDate
                if(forecastAge<1){
                    forecastResult.setYesterday(cachedResult.getYesterday());
                    forecastResult.setYesterdayDate(cachedResult.getYesterdayDateInMillis());
                }
                else if(forecastAge == 1){
                    forecastResult.setYesterday(forecastResult.getForecastDay(0));
                    forecastResult.setYesterdayDate(c.getTimeInMillis());
                }
                else if(forecastAge == 2){
                    forecastResult.setYesterday(forecastResult.getForecastDay(1));
                    forecastResult.setYesterdayDate(c.getTimeInMillis());
                }
                else if(forecastAge == 3){
                    forecastResult.setYesterday(forecastResult.getForecastDay(2));
                    forecastResult.setYesterdayDate(c.getTimeInMillis());
                }
                else if(forecastAge == 4){
                    forecastResult.setYesterday(forecastResult.getForecastDay(3));
                    forecastResult.setYesterdayDate(c.getTimeInMillis());
                }
                else{
                    forecastResult.setYesterday(forecastResult.getForecastDay(0));
                    forecastResult.setYesterdayDate(Calendar.getInstance().getTimeInMillis());
                }
            }
            else {
                //make blank obj with shared prefs location
                forecastResult = new ForecastResult(sharedPrefLocation);
            }
            //check if up to date
            if(!forecastResult.isUpToDate()){
                updateForecast();
                if(forecastResult.getUpdateConclusion().equals("SUCCESS")){
                    saveForecastResult(forecastResult, context);
                }
                else {
                    forecastResult = cachedResult;
                    //Todo notify user of connection error
                }
            }
        }//IF cachedResult != NULL

        //fetch forecast from web
        else{
            forecastResult = new ForecastResult();
            makeNewForecast();
        }
    }//Forecast constructor END

    private void updateForecast() {
//        GetForecastAsync pollenForecast = (GetForecastAsync)
        new GetForecastAsync((forecastList, updateDate, result) -> {
            if (result.equals("SUCCESS")) {
                forecastResult.setUpdateDateInMillis(updateDate);
                forecastResult.setForecastList(forecastList);
                saveForecastResult(forecastResult, context);
            }
            else {
                Log.d(TAG, "updateForecast: Failed to update. ERROR: " + result);
            }
            forecastResult.setUpdateConclusion(result);
        }).execute(forecastResult.getUrl());
        Log.d(TAG, "updateForecast: finished waiting");
    }//updateForecast END
    //initialise a new forecast when one has not previously been cashed

    private void makeNewForecast(){
        new GetForecastAsync((forecastList, updateDate, result) -> {
            if(result.equals("SUCCESS")){
                forecastResult.setUpdateDateInMillis(updateDate);
                forecastResult.setForecastList(forecastList);
                forecastResult.setYesterdayDate(updateDate);
                forecastResult.setYesterday(forecastList.get(0));
                saveForecastResult(forecastResult, context);
            }
            else{
                Log.d(TAG, "makeNewForecast: Failed to update. ERROR: " + result);
            }
            forecastResult.setUpdateConclusion(result);
        }).execute(forecastResult.getUrl());

    }//makeNewForecast END
    public ForecastResult getForecastResult(){
        return this.forecastResult;
    }

    public void saveForecastResult(@NotNull ForecastResult forecastResult, @NotNull Context context){
        //parse to json object
        String fileName = context.getCacheDir().getPath() +"forecastResultCache.json";
        try{
            //initialise json object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("selectedCityNo",forecastResult.getSelectedCityNo());
            jsonObject.put("updatedDate",forecastResult.getUpdateDateInMillis());
            jsonObject.put("yesterdayDate",forecastResult.getYesterdayDateInMillis());
            if (forecastResult.getYesterday() == null){
                jsonObject.put("yesterday","");
            }
            else {
                jsonObject.put("yesterday",forecastResult.getYesterday());
            }

            ArrayList<String> forecasts = forecastResult.getForecastList();
            JSONArray jsonArray = new JSONArray();
            if (forecasts.size() == 4){
                jsonArray.add(forecasts.get(0));
                jsonArray.add(forecasts.get(1));
                jsonArray.add(forecasts.get(2));
                jsonArray.add(forecasts.get(3));
            }
            jsonObject.put("forecasts", jsonArray);

            //convert object to string
            String jsonString = jsonObject.toString();
            //write file to output stream
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] bytes = jsonString.getBytes();
            fileOutputStream.write(bytes);
            //Toast.makeText(context, "ForecastResult Saved",Toast.LENGTH_LONG).show();
            //close file
            fileOutputStream.close();
            Log.d("ForecastResult","CacheForecastResult: Successfully saved ForecastResult to json file");
        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
            Log.e("ForecastObjJSON","File could not be found");
            Toast.makeText(context, "FILE NOT FOUND EXCEPTION", Toast.LENGTH_LONG).show();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            Log.e("ForecastObjJSON","IO Exception was raised");
            Toast.makeText(context, "IO EXCEPTION", Toast.LENGTH_LONG).show();
        }
    }//saveForecastObj END

    public ForecastResult loadForecastResult() {
        String filePath = context.getCacheDir().getPath() +"forecastResultCache.json";
        try{
            //Read from cache file
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
//            JSONObject jsonObject = (JSONObject) object;
            JSONArray forecast = (JSONArray) jsonObject.get("forecasts");
            long date = Long.parseLong(Objects.requireNonNull(jsonObject.get("updatedDate")).toString());
            int locationIndex = Integer.parseInt(Objects.requireNonNull(jsonObject.get("selectedCityNo")).toString());
            String yesterday = Objects.requireNonNull(jsonObject.get("yesterday")).toString();
            long yesterdayDate = Long.parseLong(Objects.requireNonNull(jsonObject.get("yesterdayDate")).toString());
            if(forecast.size()==4){
                ArrayList<String> forecastList = new ArrayList<>();
                for(int i =0; i< 4; i++){
                    forecastList.add(forecast.get(i).toString());
                }
                ForecastResult fr = new ForecastResult(locationIndex, date);
                fr.setYesterday(yesterday);
                fr.setYesterdayDate(yesterdayDate);
                fr.setForecastList(forecastList);
                if(yesterday.equals("")) {
                    fr.setYesterday(null);
                }
                else {
                    fr.setYesterday(yesterday);
                }
                Toast.makeText(context, "ForecastResult Loaded",Toast.LENGTH_LONG).show();
                return fr;
            }
        }//Try END
        catch (IOException ex){
            ex.printStackTrace();
            Log.e(TAG, "IO Exception was thrown");
        }
        catch (ParseException ex){
            ex.printStackTrace();
            Log.e(TAG, "Parse Exception was thrown");
        }
        catch (Exception ex){
            ex.printStackTrace();
            Log.e(TAG, "Exception was thrown while loading cached forecastResult");
        }
        return null;
    }
}//Forecast Class END


/**
 * HOW TO USE
 * Create a GetForecastAsync instance and assign the interface to handle the response to it
 * new GetForecastAsync(new GetForecastResponse() {
 *             @Override
 *             public void taskComplete(ArrayList<String> forecastList, long updateDate, String result) {
 *                 //your code here
 *             }
 *         }).execute(forecastResult.getUrl());
 */

interface GetForecastResponse{
    void taskComplete(ArrayList<String> forecastList, long updateDate, String result);
}

//scrapes the forecast from the url passed to it and saves it to cache
class GetForecastAsync extends AsyncTask<String, String, String> {
    private String TAG = "ForecastResult - getForecastAsync";
    private long updateDate;
    private ArrayList<String> forecastList;

    public GetForecastResponse delegate = null;
    public GetForecastAsync(GetForecastResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "Retrieving forecast");
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "/getForecastAsync --> onPostExecute The connection result: " + result);
        delegate.taskComplete(this.forecastList, this.updateDate, result);
    }

    @Override
    protected String doInBackground(String... strings) {
        String result;
        final String TAG = "getForecastAsync - doInBackground";

        try {
            String url = strings[0];
            //parse the page html to the document
            Document pageHtml = Jsoup.connect(url).timeout(15*1000).get();
            //Check document is not empty before getting values
            if (pageHtml != null) {
                Elements listElement = pageHtml.select("ul.pollen_graph");
                String forecasts = "" + (listElement.select("li").text()).toUpperCase();
                result = "successful\n Values:" + forecasts;//**
                //split the pollen forecast values into array (potential values: Low, Moderate, High, Very High, Extreme)
                forecasts = forecasts.replace("VERY HIGH", "VERY_HIGH");
                String delim = "\\W+";
                String[] days = forecasts.split(delim);
                forecastList = new ArrayList<>();
                Collections.addAll(this.forecastList, days);
                this.updateDate = Calendar.getInstance().getTimeInMillis();
                result = "SUCCESS";
            }
            else {
                result = "FAIL";
            }
        }//end of try
        catch (SocketTimeoutException ex) {
            result = "TIMEOUT";
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("ForecastObj", "An Exception was thrown while fetching the forecast from the web:\n" + ex);
            result = "FAIL";
        }//end of catch
        return result;//returns result to onPostExecute
    }//end of doInBackground

}// GetForecastAsync Class END



