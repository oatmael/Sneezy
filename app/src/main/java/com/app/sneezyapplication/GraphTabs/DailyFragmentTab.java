package com.app.sneezyapplication.GraphTabs;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.app.sneezyapplication.GraphsFragment;
import com.app.sneezyapplication.MainActivity;
import com.app.sneezyapplication.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static com.app.sneezyapplication.MainActivity.graphData;
import static com.app.sneezyapplication.MainActivity.repo;

import com.app.sneezyapplication.data.GraphData;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;

public class DailyFragmentTab extends Fragment {

    ListView list;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphs_dailytab, container, false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = graphData.getWeeklyUserData();

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(1d)
                .format("{%Value}{groupsSeparator: }");

        cartesian.animation(true);
        //cartesian.title("DailySneezes");

        cartesian.yScale()
                .minimum(0d)
                .ticks().interval(1);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        //cartesian.xAxis(0).title("Your Week");
        //cartesian.yAxis(0).title("Sneezes");

        anyChartView.setChart(cartesian);

        //Appearance Options
        TypedArray c1;
        TypedArray c2;

        if (MainActivity.sharedPref.loadNightModeState()){
            c1 = getContext().getTheme().obtainStyledAttributes(
                    R.style.darkTheme,
                    new int[] { R.attr.colorPrimary });
            c2 = getContext().getTheme().obtainStyledAttributes(
                    R.style.darkTheme,
                    new int[] { R.attr.colorAccent });
        } else {
            c1 = getContext().getTheme().obtainStyledAttributes(
                    R.style.AppTheme,
                    new int[] { R.attr.colorPrimary });
            c2 = getContext().getTheme().obtainStyledAttributes(
                    R.style.AppTheme,
                    new int[] { R.attr.colorAccent });
        }

        // Get color hex code (eg, #fff) and format string to match API conventions
        //ColorPrimary
        int intColor1 = c1.getColor(0 /* index */, 0 /* defaultVal */);
        String colorPrimary = "#" +Integer.toHexString(intColor1).substring(2);
        //ColorAccent
        int intColor2 = c2.getColor(0 /* index */, 0 /* defaultVal */);
        String colorAccent = "#" +Integer.toHexString(intColor2).substring(2);

        cartesian.background().fill(colorPrimary);
        column.color(colorAccent);
        //sets background during load
        anyChartView.setBackgroundColor(colorPrimary);

        // Recycle
        c1.recycle();
        c2.recycle();

        //List View -----------------------------------------
        DailyGraphListView listAdapter = new
                DailyGraphListView(getActivity(), GetListViewDays());
        list=view.findViewById(R.id.list);
        list.setAdapter(listAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(getActivity(), "You Clicked at " + listViewDays[+position], Toast.LENGTH_SHORT).show();
            }

        });
        return view;
    }

    private Date[] GetListViewDays(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        //Date array for list view
        Date[] listViewDays = new Date[7];

        for (int i = 0; i < 7; i++) {
            listViewDays[i] = c.getTime();
            c.add(Calendar.DATE, -1);
        }

        return listViewDays;
    }
}
