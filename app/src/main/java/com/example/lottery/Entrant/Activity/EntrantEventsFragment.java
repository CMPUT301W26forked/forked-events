package com.example.lottery.Entrant.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Event;
import com.example.lottery.EventAdapter;
import com.example.lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EntrantEventsFragment extends Fragment {

    private RecyclerView rvEvents;
    private EditText searchBar;

    private ArrayList<Event> allEvents;
    private ArrayList<Event> filteredEvents;

    private EventAdapter adapter;
    private FirebaseFirestore db;

    public EntrantEventsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);

        rvEvents = view.findViewById(R.id.recyclerView);
        searchBar = view.findViewById(R.id.search_bar);

        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();

        adapter = new EventAdapter(filteredEvents, event -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());

            EntrantEventDetailsFragment detailsFragment = new EntrantEventDetailsFragment();
            detailsFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvEvents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    filteredEvents.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            allEvents.add(event);
                        }
                    }

                    filteredEvents.addAll(allEvents);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }

    private void filterEvents(String keyword) {
        filteredEvents.clear();

        String searchText = keyword.trim().toLowerCase();

        if (searchText.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            for (Event event : allEvents) {
                String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
                String description = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
                String facilityName = event.getFacilityName() != null ? event.getFacilityName().toLowerCase() : "";
                String organizerName = event.getOrganizerName() != null ? event.getOrganizerName().toLowerCase() : "";

                if (title.contains(searchText)
                        || description.contains(searchText)
                        || facilityName.contains(searchText)
                        || organizerName.contains(searchText)) {
                    filteredEvents.add(event);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}