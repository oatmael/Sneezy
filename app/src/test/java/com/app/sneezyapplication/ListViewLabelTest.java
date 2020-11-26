package com.app.sneezyapplication;

import android.location.Location;

import com.app.sneezyapplication.data.SneezeData;
import com.app.sneezyapplication.data.GraphData;
import com.app.sneezyapplication.binding.MultiBind;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ListViewLabelTest {

    GraphData graphData;


    @Before
    public void setup() {
        graphData = new GraphData();
    }

    @Test//1
    public void getListViewLabelFormatTest1(){
        assertEquals("am", graphData.getListViewLabelFormat(0));
    }
    @Test//2
    public void getListViewLabelFormatTest2(){
        assertEquals("5", graphData.getListViewLabelFormat(5));
    }
    @Test//3
    public void getListViewLabelFormatTest3(){
        assertEquals(" 5 ", graphData.getListViewLabelFormat(17));
    }
    @Test//4
    public void getListViewLabelFormatTest4(){
        assertEquals("pm", graphData.getListViewLabelFormat(24));
    }

}
