package com.example.lottery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Activity.EntrantEventsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizer dashboard fragment
 * Display events and allow navigation
 */
public class OrganizerFragment extends Fragment {

    private static final String TAG = "OrganizerFragment";
    private RecyclerView recyclerView;
    private OrganizerAdapter adapter;
    private List<Event> organizerEvents;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.rvOrganizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        organizerEvents = new ArrayList<>();
        adapter = new OrganizerAdapter(organizerEvents, getParentFragmentManager());
        recyclerView.setAdapter(adapter);

        // to event builder if accessible
        view.findViewById(R.id.btnCreateEvent).setOnClickListener(v -> {
            checkOrganizerAccess(new Runnable() {
                @Override
                public void run() {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new EventBuilderFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        });

        checkOrganizerAccess(new Runnable() {
            @Override
            public void run() {
                loadOrganizerEvents();
            }
        });

        return view;
    }

    private void loadOrganizerEvents() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        db.collection("events")
                .whereEqualTo("organizerId", userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        organizerEvents.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(doc.getId());
                                organizerEvents.add(event);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void checkOrganizerAccess(Runnable Allow) {
        String userId = mAuth.getUid();
        if (userId == null) {
            redirectBlocked();
            return;
        }

        db.collection("blocked_organizers")
                .document(userId)
                .get()
                .addOnSuccessListener(ds -> {
                    if (!isAdded()) return;
                    if (ds.exists()) {
                        redirectBlocked();
                    } else if (Allow != null) {
                        Allow.run();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    redirectBlocked();
                    }
                );
    }

    private void redirectBlocked() {
        Toast.makeText(requireContext(), "Sorry, you do not have access to event management", Toast.LENGTH_SHORT).show();

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_events);
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new EntrantEventsFragment())
                .commit();
    }
}
