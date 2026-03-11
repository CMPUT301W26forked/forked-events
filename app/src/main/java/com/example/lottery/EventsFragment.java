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

public class EventsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Event> eventList = new ArrayList<>();
        // sample
        eventList.add(new Event(
                "1",
                "Swimming Lessons - Kids",
                "Open",
                "Fun and safe swimming lessons for children aged 6-10. Learn basic strokes, water safety, and build...",
                "West Side Pool",
                "3/14/2026 - 5/14/2026",
                "20 spots available",
                "Waitlist Open\ncloses 2/16/2026",
                "47 Joined"
        ));
        eventList.add(new Event(
                "2",
                "Adult Basketball League",
                "Lottery Pending",
                "Fun and safe swimming lessons for children aged 6-10. Learn basic strokes, water safety, and build...",
                "City Gym",
                "4/01/2026 - 6/01/2026",
                "10 spots available",
                "Lottery closes\n3/01/2026",
                "12 Joined"
        ));

        EventAdapter adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}