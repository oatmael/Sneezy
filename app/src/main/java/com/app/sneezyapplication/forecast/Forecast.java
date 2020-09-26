package com.app.sneezyapplication.forecast;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.app.sneezyapplication.HomeFragment;
import com.app.sneezyapplication.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;



class Forecast {
    ForecastObj forecastObj;
    //load values from shared prefs
    //load values form cache
    //if cache location = shared prefs location

    //else get new cache

    //make new forecast obj
    //if updateDate is > 1hr old






    public void cacheForecastObj(ForecastObj forecastObj, Context context){
        //parse to json object
        String fileName = context.getCacheDir().getPath() +"forecastObjCache.json";
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("selectedCityNo",forecastObj.getSelectedCityNo());
            jsonObject.put("updatedDate",forecastObj.getForecastUpdateDate());


            //convert object to string
            String jsonString = jsonObject.toString();

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

/*

    public ForecastObj getCachedForecastObj() {
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
        return forecastObj;
    }
*/
}


//scrapes the forecast from the url passed to it and save
class getForecastAsync extends AsyncTask<String, ForecastObj, String> {
    //(url, ForecastObj, )
    ForecastObj forecastObj;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("ForecastObj", "Retrieving forecast");
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("ForecastObj", "/getForecastAsync --> onPostExecute The connection result: " + result);

        HomeFragment.upDatePollenForecastViewOnPostExecute(forecastObj);


    }

    @Override
    protected String doInBackground(String... strings) {
        String result;
        final String TAG = "getForecastAsync - doInBackground";

        try {
            String url = strings[0];
            //parse the page html to the document
            Document pageHtml = Jsoup.connect(url).get();
            //Check document is not empty
            if (pageHtml != null) {
                Elements listElement = pageHtml.select("ul.pollen_graph");
                String forecasts = "" + (listElement.select("li").text()).toUpperCase();
                result = "successful\n Values:" + forecasts;//**
                //split the pollen forecast values into array (potential values: Low, Moderate, High, Very High, Extreme)
                forecasts = forecasts.replace("VERY HIGH", "VERY_HIGH");
                String delim = "\\W+";
                String[] days = forecasts.split(delim);
                ArrayList<String> daysList = new ArrayList<>();
                Collections.addAll(daysList, days);
                forecastObj.setForecastList(daysList);//update daysList in forecastObj
                forecastObj.setForecastUpdateDate(new Date());
            } else {
                result = "could not retrieve page from site\nUrl: " + url;
            }
        }//end of try
        catch (Exception ex) {
            ex.printStackTrace();
//                Log.e(TAG,"An Exception was thrown\n" +ex);
            Log.e("ForecastObj", "An Exception was thrown\n" + ex);
            result = "An Exception was thrown\n" + ex;
        }//end of catch
        return result;//returns result to onPostExecute
    }//end of doInBackground
}// getForecastAsync End



