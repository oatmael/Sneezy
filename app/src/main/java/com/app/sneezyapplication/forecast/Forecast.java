package com.app.sneezyapplication.forecast;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.app.sneezyapplication.SharedPref;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class Forecast {
    private final String TAG = "Forecast";
    private ForecastResult forecastResult;
    private ForecastResult cachedResult;//only used if connection issues or is still up to date
    Context context;

    public Forecast(Context context) {
        this.context = context;
//        load cached forecast
//        -ForecastResult cachedResult = getCachedForecastResult();
         cachedResult = loadForecastResult();//**null for now
//        load shared prefs
        int sharedPrefLocation = new SharedPref(context).loadLocationPreference();
        if(cachedResult!= null && sharedPrefLocation!= -1){
            int locationIndex = cachedResult.getSelectedCityNo();
            long updateDate = cachedResult.getUpdateDateInMillis();
            if (locationIndex == sharedPrefLocation){
                //initialise object with cached values
                forecastResult = new ForecastResult(locationIndex, updateDate);
                forecastResult.setForecastList(cachedResult.getForecastList());
                //update yesterday value based on age of forecast
                float forecastAge = forecastResult.getUpdateDateInMillis()/ TimeUnit.HOURS.toMillis(1);
                if(forecastAge<24){
                    forecastResult.setYesterday(cachedResult.getYesterday());
                }
                else if(24 <= forecastAge && forecastAge < 48){
                    forecastResult.setYesterday(forecastResult.getForecastDay(0));
                }
                else if(24 <= forecastAge && forecastAge < 48){
                    forecastResult.setYesterday(forecastResult.getForecastDay(1));
                }
                else if(48 <= forecastAge && forecastAge < 96){
                    forecastResult.setYesterday(forecastResult.getForecastDay(2));
                }
                else if(96 <= forecastAge && forecastAge < 120){
                    forecastResult.setYesterday(forecastResult.getForecastDay(3));
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
                }
            }
        }//IF cachedResult != NULL
        //fetch forecast from web
        else{
            forecastResult = new ForecastResult();
            updateForecast();
        }

        //Check if forecast is up to date

        //save forecast
    }//Forecast constructor END

    private void updateForecast() {
//        GetForecastAsync pollenForecast = (GetForecastAsync)
        new GetForecastAsync((forecastList, updateDate, result) -> {
            if (result.equals("SUCCESS")) {
                forecastResult.setUpdateDateInMillis(updateDate);
//                saveForecastResult(forecastResult, context);
            }
            else {
                Log.d(TAG, "updateForecast: Failed to update. ERROR: " + result);
                forecastResult.setUpdateConclusion(result);
            }
            forecastResult.setForecastList(forecastList);
        }).execute(forecastResult.getUrl());
        Log.d(TAG, "updateForecast: finished waiting");
    }//updateForecast END

    public ForecastResult getForecastResult(){
        return this.forecastResult;
    }

    public void saveForecastResult(@NotNull ForecastResult forecastResult, @NotNull Context context){

        //parse to json object
        String fileName = context.getCacheDir().getPath() +"forecastObjCache.json";
        try{
            //initialise json object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("selectedCityNo",forecastResult.getSelectedCityNo());
            jsonObject.put("updatedDate",forecastResult.getUpdateDateInMillis());
            jsonObject.put("yesterday",forecastResult.getYesterday());

            ArrayList<String> forecasts = forecastResult.getForecastList();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(0, forecasts.get(0));
            jsonArray.put(1, forecasts.get(1));
            jsonArray.put(2, forecasts.get(2));
            jsonArray.put(3, forecasts.get(3));
            jsonObject.put("forecasts", jsonArray);
            //convert object to string
            String jsonString = jsonObject.toString();
            //TODO
            //make file
//                File file = new File(this.getCacheDir(), fileName);
//                file.createNewFile();
            //write file to output stream
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] bytes = jsonString.getBytes();
            fileOutputStream.write(bytes);
            Toast.makeText(context, "forecastObj cached",Toast.LENGTH_LONG).show();
            //close file
            fileOutputStream.close();
//            Log.d("ForecastResult","CacheForecastResult: Successfully saved ForecastResult to json file");
        }
        catch(JSONException ex){
            ex.printStackTrace();
            Log.e("ForecastObjJSON","Forecast object could not be parsed to JSON object");
        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
            Log.e("ForecastObjJSON","File could not be found");
        } catch (IOException e) {
            e.printStackTrace();
        }
/*
            try{
//                file.createNewFile();
//                FileOutputStream outputStream = new FileOutputStream(file);

//            OutputStream outputStream = getContext().getCacheDir().createNewFile()
            }
            catch (IOException ex){
                ex.printStackTrace();
                Log.e("ForecastObjJSON","IOException");
            }

            try{
//                File file = new File(this.getCacheDir(), fileName);
//                FileWriter fileWriter = new FileWriter(file);
//                BufferedWriter bufferedReader = new BufferedWriter(fileWriter);
//                BufferedWriter.write(jsonString);
//                bufferedReader.close();
            }
            catch (Exception ex){
                ex.printStackTrace();
                Log.e("ForecastObj","");
            }
*/
    }//cacheForecastObj END

    public ForecastResult loadForecastResult() {
        /*
        String jsonString;
        try{
            InputStream is = FileInputStream.open("heat_map_dummy_data.json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
            JSONObject forecastObjJSON = new JSONObject(jsonString);
            forecastObj.setSelectedCityNo(Integer.parseInt(forecastObjJSON.getString("selectedCityNo")));
//            Calendar d = new cal();
//            d.setTime(Long.parseLong(forecastObjJSON.getString("updateDate")));
            forecastObj.setForecastUpdateDate(new Date(forecastObjJSON.getString("updatedDate")));
        }
        catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        */
        //TODO
        return null;
    }
}//Forecast Class END


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



