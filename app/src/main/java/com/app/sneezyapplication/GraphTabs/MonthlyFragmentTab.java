package com.app.sneezyapplication.GraphTabs;

import android.app.DatePickerDialog;
import android.content.res.TypedArray;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

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
import com.app.sneezyapplication.MainActivity;
import com.app.sneezyapplication.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.TELECOM_SERVICE;
import static com.app.sneezyapplication.MainActivity.graphData;

public class MonthlyFragmentTab extends Fragment {

    public View view;
    public Calendar selectedDate;
    public Calendar currentDate;
    public Button nextMonthbutton;
    Cartesian cartesian;
    Column column1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_graphs_monthlytab, container, false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //Custom Date Picker
        Calendar c = Calendar.getInstance();
        selectedDate = c;
        currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());

        //creates the graph object
        cartesian = AnyChart.column();
        createGraph(view);

        //onClick handlers for Date picker, sends 1 or -1 to got to next or previous month
        nextMonthbutton = (Button) view.findViewById(R.id.next_month_button);
        //updates displayed date immediately
        updateSelectedMonth(0);

        nextMonthbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateSelectedMonth(1);
            }
        });

        Button previousMonthButton = (Button) view.findViewById(R.id.previous_month_button);
        previousMonthButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateSelectedMonth(-1);
            }
        });



        return view;
    }
    public void updateSelectedMonth(int i) {
        //Add or remove one month to the calendar date
        selectedDate.add(Calendar.MONTH, i);
        //create a nes date instance to check against current date
        Calendar c = Calendar.getInstance();
        c.setTime(selectedDate.getTime());
        c.add(Calendar.MONTH, 1);
        //if the next month has not occurred disable the next button
        if (c.compareTo(currentDate) > 0 && nextMonthbutton != null) {
            nextMonthbutton.setEnabled(false);
        } else
            nextMonthbutton.setEnabled(true);

        TextView selectedDateText = view.findViewById(R.id.current_month_text);
        selectedDateText.setText(selectedDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + selectedDate.get(Calendar.YEAR));

        //update Graph
        updateGraph();
    }

    public void updateGraph(){
        List<DataEntry> data = graphData.getMonthlyUserData(selectedDate);
        //cartesian.data(data);
        column1.data(data);
    }

    public void createGraph(View view){
        //Create Graph;
        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar));

        List<DataEntry> data = graphData.getMonthlyUserData(selectedDate);

        //cartesian.data(data);
        column1 = cartesian.column(data);
        anyChartView.setChart(cartesian);
        cartesian.tooltip()
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

        cartesian.xAxis(0).title("Date");
        cartesian.yAxis(0).title("Sneezes");

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
        column1.color(colorAccent);

        //Recycle the colors
        c1.recycle();
        c2.recycle();

    }


}

