package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventBuilderFragment extends Fragment {

    int startDate;
    int endDate;
    boolean geoToggle;
    int limit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_builder, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        view.findViewById(R.id.btnFinish).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("FragmentTracker", "builder opened");
    }


}
