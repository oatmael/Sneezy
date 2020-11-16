package com.app.sneezyapplication.GraphTabs;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.sneezyapplication.MainActivity;
import com.app.sneezyapplication.R;

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
import com.app.sneezyapplication.data.GraphData;
import com.app.sneezyapplication.data.ListViewItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.app.sneezyapplication.MainActivity.graphData;

public class DailyGraphListView extends ArrayAdapter<Date>{

    private final Activity context;
    private final Date[] days;

    public DailyGraphListView(Activity context,
                              Date[] days) {
        super(context, R.layout.daily_graph_list, days);
        this.context = context;
        this.days = days;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.daily_graph_list, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        TextView txtTotal = (TextView) rowView.findViewById(R.id.total_txt);
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        Calendar c = Calendar.getInstance();
        c.setTime(days[position]);
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.US).format(c.getTime());
        txtTitle.setText(dayOfWeek);

        AnyChartView anyChartView = rowView.findViewById(R.id.any_chart_view);
        //anyChartView.setProgressBar(rowView.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.column();

        ListViewItem l =graphData.getListViewGraphData(days[position]);
        List<DataEntry> data = l.getDataList();
        String dailyTotal = ""+l.getDailyTotal();
        txtTotal.setText(dailyTotal);

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(1d)
                .format("{%Value}{groupsSeparator: }");

        cartesian.animation(false);
        cartesian.barGroupsPadding(0.3);

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        //cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        //cartesian.interactivity().hoverMode(HoverMode.BY_X);

        //cartesian.xAxis(0).title("");
        //cartesian.yAxis(0).title("");
        cartesian.minPointLength(0);
        anyChartView.setChart(cartesian);

        //Color Options
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

        //Recycle the colors
        c1.recycle();
        c2.recycle();


        return rowView;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }
}