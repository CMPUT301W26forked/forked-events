package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OrganizerFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrganizerAdapter adapter;
    private List<Event> organizerEvents;
    private Button btnCreateEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer, container, false);

        recyclerView = view.findViewById(R.id.rvOrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        btnCreateEvent.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventBuilderFragment())
                    .addToBackStack(null)
                    .commit();
        });

        organizerEvents = new ArrayList<>();

        adapter = new OrganizerAdapter(organizerEvents);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
