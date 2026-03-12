package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class EventManagementFragment extends Fragment {

    private Event event;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        CardView cardViewLists = view.findViewById(R.id.cardViewLists);
        cardViewLists.setOnClickListener(v -> {
            WaitlistFragment fragment = new WaitlistFragment();
            Bundle args = new Bundle();
            args.putSerializable("event", event);
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        CardView cardSampleAttendees = view.findViewById(R.id.cardSampleAttendees);
        cardSampleAttendees.setOnClickListener(v -> {
            // Handle sample attendees
        });
        
        return view;
    }
}