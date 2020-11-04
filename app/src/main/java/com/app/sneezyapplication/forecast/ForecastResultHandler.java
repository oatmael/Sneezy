package com.app.sneezyapplication.forecast;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.app.sneezyapplication.SharedPref;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ForecastResultHandler {
    private final String TAG = "Forecast";
    private ForecastResult forecastResult;
    private ForecastResult cachedResult;//only used if connection issues or is still up to date
    Context context;

    //On creation sets up the RorecastResult form avilable options
    public ForecastResultHandler(Context context) {
        this.context = context;
        //load cached forecast
        cachedResult = loadForecastResult();
        //load shared prefs
        int sharedPrefLocation = new SharedPref(context).loadLocationPreference();

        if (cachedResult != null && sharedPrefLocation != -1) {
            int locationIndex = cachedResult.getSelectedCityNo();
            long updateDate = cachedResult.getUpdateDateInMillis();
            if (locationIndex == sharedPrefLocation) {
                //initialise object with cached values
                forecastResult = new ForecastResult(locationIndex, updateDate);
                forecastResult.setForecastList(cachedResult.getForecastList());
                //update yesterday value based on age of forecast
                int forecastAge = cachedResult.getYesterdayAge();
                //get date for yesterday
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, -1);
                //set correct yesterday and yesterdayDate
                if (forecastAge < 1) {
                    forecastResult.setYesterday(cachedResult.getYesterday());
                    forecastResult.setYesterdayDateInMillis(cachedResult.getYesterdayDateInMillis());
                } else if (forecastAge == 1) {
                    forecastResult.setYesterday(forecastResult.getForecastDay(0));
                    forecastResult.setYesterdayDateInMillis(c.getTimeInMillis());
                } else if (forecastAge == 2) {
                    forecastResult.setYesterday(forecastResult.getForecastDay(1));
                    forecastResult.setYesterdayDateInMillis(c.getTimeInMillis());
                } else if (forecastAge == 3) {
                    forecastResult.setYesterday(forecastResult.getForecastDay(2));
                    forecastResult.setYesterdayDateInMillis(c.getTimeInMillis());
                } else if (forecastAge == 4) {
                    forecastResult.setYesterday(forecastResult.getForecastDay(3));
                    forecastResult.setYesterdayDateInMillis(c.getTimeInMillis());
                } else {
                    forecastResult.setYesterday(forecastResult.getForecastDay(0));
                    forecastResult.setYesterdayDateInMillis(Calendar.getInstance().getTimeInMillis());
                }
            } else {
                //make blank obj with shared prefs location
                forecastResult = new ForecastResult(sharedPrefLocation);
            }
            //check if up to date
            if (!forecastResult.isUpToDate()) {
                fetchForecastFromWeb(false);
//                updateForecast();
                if (forecastResult.getUpdateConclusion().equals("SUCCESS")) {
                    saveForecastResult(forecastResult, context);
                } else {
                    forecastResult = cachedResult;
                    //Todo notify user of connection error
                }
            }
        }//IF cachedResult != NULL
        //fetch forecast from web if not cached
        else if (sharedPrefLocation != -1) {
            forecastResult = new ForecastResult(sharedPrefLocation);
            fetchForecastFromWeb(false);
        } else {
            forecastResult = new ForecastResult();
            fetchForecastFromWeb(false);
        }
    }//Forecast constructor END

    public ForecastResult getForecastResult() {
        return this.forecastResult;
    }

    public void setForecastResult(ForecastResult forecastResult) {
        this.forecastResult = forecastResult;
    }

    public void changeLocation(int locationIndex) {
        forecastResult = new ForecastResult(locationIndex);
        fetchForecastFromWeb(true);
    }

    public void saveForecastResult(@NotNull ForecastResult forecastResult, @NotNull Context context) {
        //SAVE TO SHARED PREFS
        SharedPref sharedPref = new SharedPref(context);
        sharedPref.saveLocationPreference(forecastResult.getSelectedCityNo());

        //SAVE TO CACHE
        //parse to json object
        String fileName = context.getCacheDir().getPath() + "forecastResultCache.json";
        try {
            //initialise json object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("selectedCityNo", forecastResult.getSelectedCityNo());
            jsonObject.put("updatedDate", forecastResult.getUpdateDateInMillis());
            jsonObject.put("yesterdayDate", forecastResult.getYesterdayDateInMillis());
            if (forecastResult.getYesterday() == null) {
                jsonObject.put("yesterday", "");
            } else {
                jsonObject.put("yesterday", forecastResult.getYesterday());
            }

            ArrayList<String> forecasts = forecastResult.getForecastList();
            JSONArray jsonArray = new JSONArray();
            if (forecasts.size() == 4) {
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
//            Toast.makeText(context, "ForecastResult Saved",Toast.LENGTH_SHORT).show();
            //close file
            fileOutputStream.close();
            Log.d("ForecastResult", "CacheForecastResult: Successfully saved ForecastResult to json file");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Log.e("ForecastObjJSON", "File could not be found");
            Toast.makeText(context, "FILE NOT FOUND EXCEPTION", Toast.LENGTH_LONG).show();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("ForecastObjJSON", "IO Exception was raised");
            Toast.makeText(context, "IO EXCEPTION", Toast.LENGTH_LONG).show();
        }
    }//saveForecastObj END

    public ForecastResult loadForecastResult() {
        String filePath = context.getCacheDir().getPath() + "forecastResultCache.json";
        try {
            //Read from cache file
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
            JSONArray forecast = (JSONArray) jsonObject.get("forecasts");
            long date = Long.parseLong(Objects.requireNonNull(jsonObject.get("updatedDate")).toString());
            int locationIndex = Integer.parseInt(Objects.requireNonNull(jsonObject.get("selectedCityNo")).toString());
            String yesterday = Objects.requireNonNull(jsonObject.get("yesterday")).toString();
            long yesterdayDate = Long.parseLong(Objects.requireNonNull(jsonObject.get("yesterdayDate")).toString());
            if (forecast.size() == 4) {
                ArrayList<String> forecastList = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    forecastList.add(forecast.get(i).toString());
                }
                ForecastResult fr = new ForecastResult(locationIndex, date);
                fr.setYesterday(yesterday);
                fr.setYesterdayDateInMillis(yesterdayDate);
                fr.setForecastList(forecastList);
                if (yesterday.equals("")) {
                    fr.setYesterday(null);
                } else {
                    fr.setYesterday(yesterday);
                }
//                Toast.makeText(context, "ForecastResult Loaded",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Cashed Forecast Result Loaded Scuccesfully");
                return fr;
            }
        }//Try END
        catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, "IO Exception was thrown");
        } catch (ParseException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Parse Exception was thrown");
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "Exception was thrown while loading cached forecastResult");
        }
        return null;
    }

    private void handleFetchForecastResponse(String result, ArrayList<String> forecastList, Long updateDate, boolean makeNewForecast) {
        if (result.equals("SUCCESS")) {
            if (makeNewForecast) {
                this.forecastResult.setYesterdayDateInMillis(updateDate);
                this.forecastResult.setYesterday(forecastList.get(0));
            }
            this.forecastResult.setUpdateDateInMillis(updateDate);
            this.forecastResult.setForecastList(forecastList);
            saveForecastResult(this.forecastResult, this.context);
        } else {
            Log.e("handleForecastResponce", "Not Successful. Result: " + result);
        }
        this.forecastResult.setUpdateConclusion(result);
    }//handleNewForecastResponce END

    public void fetchForecastFromWeb(boolean makeNewForecast) {
        String result;
        ArrayList<String> forecastList = new ArrayList<>();
        long updateDate = 0;
        final String TAG = "fetchForecastFromWeb";

        try {
            String url = forecastResult.getUrl();
            //parse the page html to the document
            Document pageHtml = Jsoup.connect(url).timeout(15 * 1000).get();
            //Check document is not empty before getting values
            if (pageHtml != null) {
                Elements listElement = pageHtml.select("ul.pollen_graph");
                String forecasts = "" + (listElement.select("li").text()).toUpperCase();
                result = "successful\n Values:" + forecasts;//**
                //split the pollen forecast values into array (potential values: Low, Moderate, High, Very High, Extreme)
                forecasts = forecasts.replace("VERY HIGH", "VERY_HIGH");
                String delim = "\\W+";
                String[] days = forecasts.split(delim);
                Collections.addAll(forecastList, days);
                updateDate = Calendar.getInstance().getTimeInMillis();
                result = "SUCCESS";
                Log.d(TAG, "Connection result: " + result + " Values: " + forecastList.toString());
            } else {
                result = "FAIL";
                Log.e(TAG, "Page fetched was empty");
            }
        }//end of try
        catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Connection timmed out:\n" + ex);
            result = "TIMEOUT";
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "An Exception was thrown while fetching the forecast from the web:\n" + ex);
            result = "FAIL";
        }//end of catch
        handleFetchForecastResponse(result, forecastList, updateDate, makeNewForecast);
    }//getForecastFromWeb END
}