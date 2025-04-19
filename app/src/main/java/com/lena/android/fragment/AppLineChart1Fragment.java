package com.lena.android.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.lena.android.R;
import com.lena.android.databinding.AppFragmentLineChart1Binding;

import java.util.ArrayList;

/**
 * MPAndroidChartExample
 */
public class AppLineChart1Fragment extends ParentFragment {

    private AppFragmentLineChart1Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AppFragmentLineChart1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayList<Entry> values = new ArrayList<>();
        final Drawable drawable = ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.app_ic_launcher_background, null);

        final int base = 12;
        for (int i = 0; i < 12; i++) {
            float value = (float) (base + Math.pow(-1, i));
            values.add(new Entry(i, value, drawable));
        }

        final LineDataSet lineDataSet = new LineDataSet(values, "DataSet 1");
        lineDataSet.setDrawIcons(false);
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        lineDataSet.setValueTextSize(9f);
        lineDataSet.enableDashedHighlightLine(10f, 5f, 0f);
        lineDataSet.setDrawFilled(true);

        final ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        final LineData data = new LineData(dataSets);

        binding.chart1.setData(data);
    }
}