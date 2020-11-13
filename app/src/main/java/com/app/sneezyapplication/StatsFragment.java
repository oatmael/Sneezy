package com.app.sneezyapplication;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.sneezyapplication.binding.StatsBind;
import com.app.sneezyapplication.databinding.FragmentStatsBinding;

public class StatsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentStatsBinding mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_stats, container, false);
        View view = mBinding.getRoot();

        StatsBind binding = new StatsBind();
        mBinding.setStats(binding);

        return view;
    }
}
