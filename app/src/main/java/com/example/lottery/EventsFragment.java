package com.example.lottery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of public events.
 * Retrieves event data from Firestore and shows it in a RecyclerView.
 */
public class EventsFragment extends Fragment {

    private static final String TAG = "EventsFragment";
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        adapter = new EventAdapter(eventList, event -> {
        });
        recyclerView.setAdapter(adapter);

        fetchEventsFromFirestore();

        return view;
    }

    /**
     * Fetches events from Firestore database.
     * Only public (non-private) events are added to the list.
     */
    private void fetchEventsFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setEventId(document.getId());
                            
                            if (!event.isPrivate()) {
                                eventList.add(event);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}