package com.example.lottery.Entrant.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private ArrayList<Event> eventList;
    private EventAdapter adapter;
    private FirebaseFirestore db;

    public EntrantEventsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);

        rvEvents = view.findViewById(R.id.recyclerView); // change this if your RecyclerView ID is different
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();

        adapter = new EventAdapter(eventList, event -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());

            EntrantEventDetailsFragment detailsFragment = new EntrantEventDetailsFragment();
            detailsFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment) // change if your container ID is different
                    .addToBackStack(null)
                    .commit();
        });

        rvEvents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();

        return view;
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            eventList.add(event);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }
}