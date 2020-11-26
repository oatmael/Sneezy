package com.app.sneezyapplication;

import com.app.sneezyapplication.binding.MultiBind;
import com.app.sneezyapplication.data.GraphData;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultiBindTest {

//    SneezeData s;
//    Location l;
//    Date d;
//    int i = 1;
    GraphData graphData;
    MultiBind multiBind;

    @Before
    public void setup() {
        multiBind = new MultiBind();
    }

    @Test
    public void MultiBindResetIndicatorTest(){
        multiBind.multiNum = 99;
        assertEquals("1X", multiBind.getMulti(1));
    }

    @Test
    public void MultiBindAddWhileNegative(){
        multiBind.multiNum = -5;
        assertEquals("-4X", multiBind.getMulti(2));
    }

    @Test
    public void MultiBindAddWhileMinus1(){
        multiBind.multiNum = -1;
        assertEquals("1X", multiBind.getMulti(2));
    }

    @Test
    public void MultiBindAddWhilePositive(){
        multiBind.multiNum = 1;
        assertEquals("2X", multiBind.getMulti(2));
    }


    @Test
    public void MultiBindMinusWhileNegative(){
        multiBind.multiNum = -1;
        assertEquals("-2X", multiBind.getMulti(3));
    }

    @Test
    public void MultiBindMinusWhile1(){
        multiBind.multiNum = 1;
        assertEquals("-1X", multiBind.getMulti(3));
    }

    @Test
    public void MultiBindMinusWhilePositive(){
        multiBind.multiNum = 12;
        assertEquals("11X", multiBind.getMulti(3));
    }
}
