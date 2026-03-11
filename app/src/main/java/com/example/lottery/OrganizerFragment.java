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

public class OrganizerFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrganizerAdapter adapter;
    private List<Event> organizerEvents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer, container, false);

        recyclerView = view.findViewById(R.id.rvOrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        organizerEvents = new ArrayList<>();
        // sample data with IDs
        organizerEvents.add(new Event("1", "Example Event 1", "Open", "Description 1", "Location 1", "2023-12-01", "100", "Info", "10"));
        organizerEvents.add(new Event("2", "Example Event 2", "Open", "Description 2", "Location 2", "2023-12-02", "50", "Info", "5"));

        adapter = new OrganizerAdapter(organizerEvents, new OrganizerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                navigateToManagement(event);
            }

            @Override
            public void onEditClick(Event event) {
                navigateToEdit(event);
            }

            @Override
            public void onManageClick(Event event) {
                navigateToManagement(event);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void navigateToManagement(Event event) {
        EventManagementFragment fragment = new EventManagementFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToEdit(Event event) {
        EventBuilderFragment fragment = new EventBuilderFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}