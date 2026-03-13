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

/**
 * Fragment that displays a list of all available lottery events.
 * <p>
 * Fetches events from Firestore on load and presents them in a
 * scrollable list. Tapping an event navigates to
 * EntrantEventDetailsFragment for that event.
 * </p>
 */

public class EntrantEventsFragment extends Fragment {

    private RecyclerView rvEvents;
    private ArrayList<Event> eventList;
    private EventAdapter adapter;
    private FirebaseFirestore db;

    public EntrantEventsFragment() {
    }

    /**
     * Inflates the fragment layout, sets up the RecyclerView and adapter,
     * and triggers the initial load of events from Firestore.
     *
     * @param inflater  the LayoutInflater used to inflate the fragment view
     * @param container the parent view that the fragment UI should attach to
     * @param savedInstanceState previously saved state, or null if none exists
     * @return the inflated root view for this fragment
     */

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

    /**
     * Fetches all events from the Firestore "events" collection and
     * populates the RecyclerView.
     * <p>
     * On success, clears the current event list, maps each Firestore
     * document to an Event object, and notifies the adapter of the update.
     * On failure, displays a toast message to the user.
     * </p>
     */
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