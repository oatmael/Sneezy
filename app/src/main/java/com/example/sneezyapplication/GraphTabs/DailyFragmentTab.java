package com.example.sneezyapplication.GraphTabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.androidplot.Region;
import com.androidplot.ui.Anchor;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.SeriesBundle;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.TextOrientation;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.util.PixelUtils;
import com.androidplot.util.SeriesUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesFormatter;

import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;

import com.example.sneezyapplication.R;

public class DailyFragmentTab extends Fragment {


    private static final String NO_SELECTION_TXT = "Touch bar to select.";
    private XYPlot plot;

    private CheckBox series1CheckBox;
    private CheckBox series2CheckBox;
    private Spinner spRenderStyle, spWidthStyle, spSeriesSize;
    private SeekBar sbFixedWidth, sbVariableWidth;

    private XYSeries series1;
    private XYSeries series2;

    private enum SeriesSize {
        WEEK,
        MONTH,
        YEAR
    }
    private SeriesSize selectedSize;

    // Create a couple arrays of y-values to plot:
    Number[] series1Numbers7 = {2, null, 5, 2, 7, 4, 3};
    Number[] series2Numbers7 = {4, 6, 3, null, 2, 0, 7};
    Number[] series1Numbers20 = {2, null, 5, 2, 7, 4, 3, 7, 4, 5, 7, 4, 5, 8, 5, 3, 6, 3, 9, 3};
    Number[] series2Numbers20 = {4, 6, 3, null, 2, 0, 7, 4, 5, 4, 9, 6, 2, 8, 4, 0, 7, 4, 7, 9};
    Number[] series1Numbers60 = {2, null, 5, 2, 7, 4, 3, 7, 4, 5, 7, 4, 5, 8, 5, 3, 6, 3, 9, 3, 2, null, 5, 2, 7, 4, 3, 7, 4, 5, 7, 4, 5, 8, 5, 3, 6, 3, 9, 3, 2, null, 5, 2, 7, 4, 3, 7, 4, 5, 7, 4, 5, 8, 5, 3, 6, 3, 9, 3};
    Number[] series2Numbers60 = {4, 6, 3, null, 2, 0, 7, 4, 5, 4, 9, 6, 2, 8, 4, 0, 7, 4, 7, 9, 4, 6, 3, null, 2, 0, 7, 4, 5, 4, 9, 6, 2, 8, 4, 0, 7, 4, 7, 9, 4, 6, 3, null, 2, 0, 7, 4, 5, 4, 9, 6, 2, 8, 4, 0, 7, 4, 7, 9};
    Number[] series1Numbers = series1Numbers7;
    Number[] series2Numbers = series2Numbers7;

    private MyBarFormatter formatter1;

    private MyBarFormatter formatter2;

    private MyBarFormatter selectionFormatter;

    private TextLabelWidget selectionWidget;

    private Pair<Integer, XYSeries> selection;

    //View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphs_dailytab, container, false);

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_graphs);

        // initialize our XYPlot reference:
        plot = (XYPlot) view.findViewById(R.id.plot);

        formatter1 = new MyBarFormatter(Color.rgb(100, 150, 100), Color.LTGRAY);
        formatter1.setMarginLeft(PixelUtils.dpToPix(1));
        formatter1.setMarginRight(PixelUtils.dpToPix(1));
        formatter2 = new MyBarFormatter(Color.rgb(100, 100, 150), Color.LTGRAY);
        formatter2.setMarginLeft(PixelUtils.dpToPix(1));
        formatter2.setMarginRight(PixelUtils.dpToPix(1));
        selectionFormatter = new MyBarFormatter(Color.YELLOW, Color.WHITE);

        selectionWidget = new TextLabelWidget(plot.getLayoutManager(), NO_SELECTION_TXT,
                new Size(
                        PixelUtils.dpToPix(100), SizeMode.ABSOLUTE,
                        PixelUtils.dpToPix(100), SizeMode.ABSOLUTE),
                TextOrientation.HORIZONTAL);

        selectionWidget.getLabelPaint().setTextSize(PixelUtils.dpToPix(16));

        // add a dark, semi-transparent background to the selection label widget:
        Paint p = new Paint();
        p.setARGB(100, 0, 0, 0);
        selectionWidget.setBackgroundPaint(p);

        selectionWidget.position(
                0, HorizontalPositioning.RELATIVE_TO_CENTER,
                PixelUtils.dpToPix(45), VerticalPositioning.ABSOLUTE_FROM_TOP,
                Anchor.TOP_MIDDLE);
        selectionWidget.pack();

        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);
        plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);

        plot.setLinesPerDomainLabel(2);

        // setup checkbox listers:
        series1CheckBox = (CheckBox) view.findViewById(R.id.s1CheckBox);
        series1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onS1CheckBoxClicked(b);
            }
        });

        series2CheckBox = (CheckBox) view.findViewById(R.id.s2CheckBox);
        series2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onS2CheckBoxClicked(b);
            }
        });

        plot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onPlotClicked(new PointF(motionEvent.getX(), motionEvent.getY()));
                }
                return true;
            }
        });

        spRenderStyle = (Spinner) view.findViewById(R.id.spRenderStyle);
        ArrayAdapter<BarRenderer.BarOrientation> adapter = new ArrayAdapter<BarRenderer.BarOrientation>(getActivity(),
                android.R.layout.simple_spinner_item, BarRenderer.BarOrientation
                .values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRenderStyle.setAdapter(adapter);
        spRenderStyle.setSelection(BarRenderer.BarOrientation.OVERLAID.ordinal());
        spRenderStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                updatePlot();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spWidthStyle = (Spinner) view.findViewById(R.id.spWidthStyle);
        ArrayAdapter<BarRenderer.BarGroupWidthMode> adapter1 = new ArrayAdapter<BarRenderer.BarGroupWidthMode>(
                getActivity(), android.R.layout.simple_spinner_item, BarRenderer.BarGroupWidthMode
                .values());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWidthStyle.setAdapter(adapter1);
        spWidthStyle.setSelection(BarRenderer.BarGroupWidthMode.FIXED_GAP.ordinal());
        spWidthStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                sbFixedWidth.setVisibility(View.INVISIBLE);
//                sbVariableWidth.setVisibility(View.INVISIBLE);
                spWidthStyle.setVisibility(View.INVISIBLE);

                updatePlot();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spSeriesSize = (Spinner) view.findViewById(R.id.spSeriesSize);
        ArrayAdapter<SeriesSize> adapter11 = new ArrayAdapter<SeriesSize>(getActivity(),
                android.R.layout.simple_spinner_item, SeriesSize.values());
        adapter11.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSeriesSize.setAdapter(adapter11);
        spSeriesSize.setSelection(SeriesSize.WEEK.ordinal());
        spSeriesSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final SeriesSize _selectedSize = (SeriesSize) arg0.getSelectedItem();
                switch (_selectedSize) {
                    case WEEK:
                        series1Numbers = series1Numbers7;
                        series2Numbers = series2Numbers7;
                        selectedSize = (SeriesSize) arg0.getSelectedItem();
                        break;
                    case MONTH:
                        series1Numbers = series1Numbers20;
                        series2Numbers = series2Numbers20;
                        selectedSize = (SeriesSize) arg0.getSelectedItem();
                        break;
                    case YEAR:
                        series1Numbers = series1Numbers60;
                        series2Numbers = series2Numbers60;
                        selectedSize = (SeriesSize) arg0.getSelectedItem();
                        break;
                    default:
                        break;
                }
                updatePlot(_selectedSize);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

//        sbFixedWidth = (SeekBar) view.findViewById(R.id.sbFixed);
//        sbFixedWidth.setProgress(50);
//        sbFixedWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                updatePlot();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
//
//        sbVariableWidth = (SeekBar) view.findViewById(R.id.sbVariable);
//        sbVariableWidth.setProgress(1);
//        sbVariableWidth.setVisibility(View.INVISIBLE);
//        sbVariableWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                updatePlot();
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new NumberFormat() {
                    @Override
                    public StringBuffer format(double value, StringBuffer buffer,
                                               FieldPosition field) {
                        int day = (int) (value + 0.5d);
                        int year = (int) (value + 0.5d) / 12;
                        int month = (int) ((value + 0.5d) % 12);

                        final SeriesSize _selectedSize = selectedSize;
                        switch (_selectedSize) {
                            case WEEK:
                                return new StringBuffer(DateFormatSymbols.getInstance()
                                        .getShortWeekdays()[day] + " " + day);

                            case MONTH:
                                return new StringBuffer(DateFormatSymbols.getInstance()
                                        .getShortMonths()[month] + " '0" + year);

                            case YEAR:

                                return new StringBuffer(DateFormatSymbols.getInstance()
                                        .getShortMonths()[month] + " '0" + year);
                            default:
                                break;
                        }

                        return new StringBuffer(DateFormatSymbols.getInstance()
                                .getShortWeekdays()[day] + " " + day);
                    }

                    @Override
                    public StringBuffer format(long value, StringBuffer buffer,
                                               FieldPosition field) {
                        throw new UnsupportedOperationException("Not yet implemented.");
                    }

                    @Override
                    public Number parse(String string, ParsePosition position) {
                        throw new UnsupportedOperationException("Not yet implemented.");
                    }
                });
        updatePlot();

        return view;
    }

    private void updatePlot() {
        updatePlot(null);
    }

    private void updatePlot(SeriesSize seriesSize) {

        // Remove all current series from each plot
        plot.clear();

        // Setup our Series with the selected number of elements
        series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Personal");
        series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Global Average");

        plot.setDomainBoundaries(-1, series1.size(), BoundaryMode.FIXED);
        plot.setRangeUpperBoundary(
                SeriesUtils.minMax(series1, series2).
                        getMaxY().doubleValue() + 1, BoundaryMode.FIXED);

        if(seriesSize != null) {
            switch(seriesSize) {
                case WEEK:
                    plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 2);
                    break;
                case MONTH:
                    plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 4);
                    break;
                case YEAR:
                    plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 6);
                    break;
            }
        }

        // add a new series' to the xyplot:
        if (series1CheckBox.isChecked()) plot.addSeries(series1, formatter1);
        if (series2CheckBox.isChecked()) plot.addSeries(series2, formatter2);

        // Setup the BarRenderer with our selected options
        MyBarRenderer renderer = plot.getRenderer(MyBarRenderer.class);
        renderer.setBarOrientation((BarRenderer.BarOrientation) spRenderStyle.getSelectedItem());
        final BarRenderer.BarGroupWidthMode barGroupWidthMode
                = (BarRenderer.BarGroupWidthMode) spWidthStyle.getSelectedItem();
        renderer.setBarGroupWidth(barGroupWidthMode,
                barGroupWidthMode == BarRenderer.BarGroupWidthMode.FIXED_WIDTH
                        ? 50 : 1);

        //final BarRenderer.BarGroupWidthMode barGroupWidthMode
        //        = (BarRenderer.BarGroupWidthMode) spWidthStyle.getSelectedItem();
        //renderer.setBarGroupWidth(barGroupWidthMode,
        //        barGroupWidthMode == BarRenderer.BarGroupWidthMode.FIXED_WIDTH
        //                ? sbFixedWidth.getProgress() : sbVariableWidth.getProgress());

        if (BarRenderer.BarOrientation.STACKED.equals(spRenderStyle.getSelectedItem())) {
            plot.getInnerLimits().setMaxY(15);
        } else {
            plot.getInnerLimits().setMaxY(0);
        }

        plot.redraw();

    }

    private void onPlotClicked(PointF point) {

        // make sure the point lies within the graph area.  we use gridrect
        // because it accounts for margins and padding as well.
        if (plot.containsPoint(point.x, point.y)) {
            Number x = plot.getXVal(point);
            Number y = plot.getYVal(point);

            selection = null;
            double xDistance = 0;
            double yDistance = 0;

            // find the closest value to the selection:
            for (SeriesBundle<XYSeries, ? extends XYSeriesFormatter> sfPair : plot
                    .getRegistry().getSeriesAndFormatterList()) {
                XYSeries series = sfPair.getSeries();
                for (int i = 0; i < series.size(); i++) {
                    Number thisX = series.getX(i);
                    Number thisY = series.getY(i);
                    if (thisX != null && thisY != null) {
                        double thisXDistance =
                                Region.measure(x, thisX).doubleValue();
                        double thisYDistance =
                                Region.measure(y, thisY).doubleValue();
                        if (selection == null) {
                            selection = new Pair<>(i, series);
                            xDistance = thisXDistance;
                            yDistance = thisYDistance;
                        } else if (thisXDistance < xDistance) {
                            selection = new Pair<>(i, series);
                            xDistance = thisXDistance;
                            yDistance = thisYDistance;
                        } else if (thisXDistance == xDistance &&
                                thisYDistance < yDistance &&
                                thisY.doubleValue() >= y.doubleValue()) {
                            selection = new Pair<>(i, series);
                            xDistance = thisXDistance;
                            yDistance = thisYDistance;
                        }
                    }
                }
            }

        } else {
            // if the press was outside the graph area, deselect:
            selection = null;
        }

        if (selection == null) {
            selectionWidget.setText(NO_SELECTION_TXT);
        } else {
            selectionWidget.setText("Selected: " + selection.second.getTitle() +
                    " Value: " + selection.second.getY(selection.first));
        }
        plot.redraw();
    }

    private void onS1CheckBoxClicked(boolean checked) {
        if (checked) {
            plot.addSeries(series1, formatter1);
        } else {
            plot.removeSeries(series1);
        }
        plot.redraw();
    }

    private void onS2CheckBoxClicked(boolean checked) {
        if (checked) {
            plot.addSeries(series2, formatter2);
        } else {
            plot.removeSeries(series2);
        }
        plot.redraw();
    }

    class MyBarFormatter extends BarFormatter {

        public MyBarFormatter(int fillColor, int borderColor) {
            super(fillColor, borderColor);
        }

        @Override
        public Class<? extends SeriesRenderer> getRendererClass() {
            return MyBarRenderer.class;
        }

        @Override
        public SeriesRenderer doGetRendererInstance(XYPlot plot) {
            return new MyBarRenderer(plot);
        }
    }

    class MyBarRenderer extends BarRenderer<MyBarFormatter> {

        public MyBarRenderer(XYPlot plot) {
            super(plot);
        }

        /**
         * Implementing this method to allow us to inject our
         * special selection getFormatter.
         * @param index index of the point being rendered.
         * @param series XYSeries to which the point being rendered belongs.
         * @return
         */
        @Override
        public MyBarFormatter getFormatter(int index, XYSeries series) {
            if (selection != null &&
                    selection.second == series &&
                    selection.first == index) {
                return selectionFormatter;
            } else {
                return getFormatter(series);
            }
        }
    }
}
