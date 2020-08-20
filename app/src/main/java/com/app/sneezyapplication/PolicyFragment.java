package com.app.sneezyapplication;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PolicyFragment extends Fragment {

    TextView googleLink;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_policy, container, false);

        googleLink = view.findViewById(R.id.googleLink);
        googleLink.setMovementMethod(LinkMovementMethod.getInstance());


        return view;
    }
}
