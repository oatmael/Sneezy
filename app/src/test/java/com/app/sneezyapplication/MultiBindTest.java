package com.app.sneezyapplication;

import com.app.sneezyapplication.binding.MultiBind;
import com.app.sneezyapplication.data.GraphData;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultiBindTest {

    MultiBind multiBind;

    @Before
    public void setup() {
        multiBind = new MultiBind();
    }

    @Test//12
    public void MultiBindResetIndicatorTest(){
        multiBind.multiNum = 99;
        assertEquals("1X", multiBind.getMulti(1));
    }

    @Test//13
    public void MultiBindAddWhileNegative(){
        multiBind.multiNum = -5;
        assertEquals("-4X", multiBind.getMulti(2));
    }

    @Test//14
    public void MultiBindAddWhileMinus1(){
        multiBind.multiNum = -1;
        assertEquals("1X", multiBind.getMulti(2));
    }

    @Test//15
    public void MultiBindAddWhilePositive(){
        multiBind.multiNum = 1;
        assertEquals("2X", multiBind.getMulti(2));
    }

    @Test//16
    public void MultiBindMinusWhileNegative(){
        multiBind.multiNum = -1;
        assertEquals("-2X", multiBind.getMulti(3));
    }

    @Test//17
    public void MultiBindMinusWhile1(){
        multiBind.multiNum = 1;
        assertEquals("-1X", multiBind.getMulti(3));
    }

    @Test//18
    public void MultiBindMinusWhilePositive(){
        multiBind.multiNum = 12;
        assertEquals("11X", multiBind.getMulti(3));
    }
}
