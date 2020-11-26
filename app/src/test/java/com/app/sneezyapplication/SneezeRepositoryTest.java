package com.app.sneezyapplication;

import com.app.sneezyapplication.binding.MultiBind;
import com.app.sneezyapplication.data.GraphData;
import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.RealmList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SneezeRepositoryTest {
    SneezeRepository sp;
    Calendar today;

    @Before
    public void setup() {
        sp = new SneezeRepository();
        today = Calendar.getInstance();
    }

    @Test//8
    public void getSneezeItemAverageTest1(){ // expecting 6.2657 rounded to nearest whole number
        int[] numbersToAverage = new int[] {3,7,3,8,5,13,5};
        assertEquals(6f, sp.sneezeItemAverage(getTestData(numbersToAverage, today), false), 0);
    }

    @Test//9
    public void getSneezeItemAverageTest2(){ // expecting 3.2857 rounded to nearest whole number
        int[] numbersToAverage = new int[] {1,2,2,5,3,2,8};
        assertEquals(3f, sp.sneezeItemAverage(getTestData(numbersToAverage, today), false), 0);
    }

    @Test//10
    public void sneezeItemDaysTest1(){
        //sneezes form week is used to generate a week's worth of sneeze items that are then arranged by day of the week
        int[] sneezesFromWeek = new int[] {1,2,2,5,3,2,8};
        int[] sneezesExpected = new int[] {1,2,2,5,3,2,8};
        //takes
        Calendar c = Calendar.getInstance();
        c.set(2020, 10, 2); //months are base 0??
        assertArrayEquals(sneezesExpected, sp.sneezeItemDays(getTestData(sneezesFromWeek, c)));
    }

    @Test//11
    public void sneezeItemDaysTest2(){
        //Same data but from a different week
        int[] sneezesFromWeek = new int[] {1,2,2,5,3,2,8};
        int[] sneezesExpected = new int[] {8,1,2,2,5,3,2};

        Calendar c = Calendar.getInstance();
        c.set(2020, 11, 1); //this date is a sunday therefore the result should be shifted by one day
        assertArrayEquals(sneezesExpected, sp.sneezeItemDays(getTestData(sneezesFromWeek, c)));
    }


    //used to test the test
    public String arrayToString(int[] ints) {
        String output = "";
        for (int i: ints)
        {
            output = output + i;
        }
        return output;
    }

    //makes a list of sneeze items using the numbers to average
    public List<SneezeItem> getTestData(int[] nums, Calendar c){
        ArrayList<SneezeItem> dummyList = new ArrayList<>();

        int rSneeze;
        RealmList<SneezeData> sl;
        SneezeItem si;

        for (int d = 0; d < nums.length; d++) {
            sl = new RealmList<SneezeData>();
            rSneeze = nums[d];
            for (int i = 0; i < rSneeze; i++) {
                SneezeData sd = new SneezeData(c.getTime().toString(), "111,111");
                sl.add(sd);
            }
            si = new SneezeItem(c.getTime().toString(), "ownerID01", sl);
            dummyList.add(si);
            c.add(Calendar.DATE, 1);
        }
        return dummyList;
    }

}
