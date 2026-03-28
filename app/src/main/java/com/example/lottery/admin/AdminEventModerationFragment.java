package com.example.lottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Event;
import com.example.lottery.EventAdapter;
import com.example.lottery.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment for admin moderation
 * including view event, organizer removal
 */
public class AdminEventModerationFragment extends Fragment {
    private List<Event> eventList = new ArrayList<>();
    private EventAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_event_moderation, container, false);
        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(eventList, event -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, AdminEventModerationDetailFragment.newInstance(event.getEventId()))
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        loadEvents();
        return view;
    }

    /**
     * load events for admin event moderation
     */
    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(qs -> {
                    eventList.clear();

                    for (DocumentSnapshot doc: qs) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

}

