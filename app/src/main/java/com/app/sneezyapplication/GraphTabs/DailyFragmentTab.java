package com.app.sneezyapplication.GraphTabs;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.app.sneezyapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static com.app.sneezyapplication.MainActivity.repo;
import com.app.sneezyapplication.data.SneezeItem;
import com.app.sneezyapplication.data.SneezeData;

public class DailyFragmentTab extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphs_dailytab, container, false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.column();

        //Currently working on dummy data, will require adjustments later
        List<DataEntry> data = new ArrayList<>();

        String date;
        int sneezes;
        List<SneezeItem> weeklySneezes = repo.getWeeklyDummyData();

        if (weeklySneezes.size() != 0) {
            for (SneezeItem s : weeklySneezes) {
                sneezes = 0;

                date = s.getDate();
                for (SneezeData d : s.getSneezes()) {
                    sneezes++;
                }
                //adds at index 0 for chronological order
                //displays only first 3 chars of date string
                data.add(0, new ValueDataEntry(date.substring(0,3), sneezes));
            }
        }

        //dummy data
//        data.add(new ValueDataEntry("Sat", 4));
//        data.add(new ValueDataEntry("Sun", 3));
//        data.add(new ValueDataEntry("Mon", 5));
//        data.add(new ValueDataEntry("Tue", 1));
//        data.add(new ValueDataEntry("Wed", 2));
//        data.add(new ValueDataEntry("Thu", 4));
//        data.add(new ValueDataEntry("Fri", 0));

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(1d)
                .format("{%Value}{groupsSeparator: }");

        cartesian.animation(true);
        cartesian.title("DailySneezes");

        cartesian.yScale()
                .minimum(0d)
                .ticks().interval(1);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Your Week");
        cartesian.yAxis(0).title("Sneezes");

        anyChartView.setChart(cartesian);

        //Appearance Options
        //cartesian.background().fill("GET COLOR FROM STYLE");
        //column.color("red");

        return view;
    }
}
