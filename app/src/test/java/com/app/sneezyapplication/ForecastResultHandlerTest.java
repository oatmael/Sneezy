package com.app.sneezyapplication;

import android.app.Instrumentation;

import com.app.sneezyapplication.forecast.ForecastResult;
import com.app.sneezyapplication.forecast.ForecastResultHandlerTestClass;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ForecastResultHandlerTest extends Instrumentation {

    @Before
    public void setup(){

    }

    @Test
    public void ForecastResultHandlerConstructor() {
        //should make an up to date ForecastResult (within 30 min)
        //with the shared prefs location if the shared prefs location is -1 the default location is used (0 = Adelaide)
        ForecastResultHandlerTestClass frh = new ForecastResultHandlerTestClass(getContext());
        assertTrue(frh.getForecastResult().isUpToDate());
    }

    @Test
    public void ForcastResultChangeLocation() {
        ForecastResultHandlerTestClass frh = new ForecastResultHandlerTestClass(getContext());
        int setLocation = 2;
        ForecastResult fr1 = frh.getForecastResult();
        frh.changeLocation(setLocation);
        ForecastResult fr2 = frh.getForecastResult();
//        check fr1 & fr2 values DONT match
//        check fr2 location is correct(what it was set to earlier) and conclusion.equals("SUCCESS")
        assertNotEquals(fr1.getSelectedCityNo(), fr2.getSelectedCityNo());
        assertEquals(setLocation, fr2.getSelectedCityNo());
    }

    @Test
    public void ForcastResultUpdateForecast() {
        ForecastResultHandlerTestClass frh = new ForecastResultHandlerTestClass(getContext());
        long t1 = new Date().getTime();
        //frh.updateForecast();
        ForecastResult fr = frh.getForecastResult();
        long t2 = fr.getUpdateDateInMillis();
        long dif = (t1 - t2); //time passed
        long expected = 5; //5ms

        assertTrue(dif < expected);
    }

}
