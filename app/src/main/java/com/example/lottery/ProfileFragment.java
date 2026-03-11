package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvMyEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Event> myEvents = new ArrayList<>();
        // sample
        myEvents.add(new Event("1", "Swimming Lessons - Beginner Kids", "Upcoming", "", "", "Mar 14 – May 14, 2026", "", "", ""));
        myEvents.add(new Event("2", "Swimming Lessons - Beginner Kids", "Waitlisted", "", "", "February 21, 2026 - March 21, 2026", "", "", ""));

        MyEventsAdapter adapter = new MyEventsAdapter(myEvents);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}