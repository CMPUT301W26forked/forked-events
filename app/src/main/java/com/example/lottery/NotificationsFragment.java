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

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.rvNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        // sample
        notificationList.add(new Notification("You have successfully joined the event!", "Swimming Lessons - Beginner Kids", "March 14 – May 14, 2026", "Joined"));
        notificationList.add(new Notification("You are currently on the waitlist.", "Swimming Lessons - Beginner Kids", "March 14 – May 14, 2026", "Waitlisted"));
        notificationList.add(new Notification("You're invited to an event!", "Swimming Lessons - Beginner Kids", "March 14 – May 14, 2026", "Invitation"));
        notificationList.add(new Notification("You have been added to waitlist for the event!", "Adult Basketball League", "April 01 – June 01, 2026", "Waitlisted"));

        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}