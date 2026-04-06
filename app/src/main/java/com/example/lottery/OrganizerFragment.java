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
import com.google.firebase.firestore.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment representing the Organizer dashboard.
 * Displays events created by the user or where the user is a co-organizer.
 * Provides navigation to event creation and management.
 */
public class OrganizerFragment extends Fragment {

    private static final String TAG = "OrganizerFragment";
    private RecyclerView recyclerView;
    private OrganizerAdapter adapter;
    private List<Event> organizerEvents;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI.
     */
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

    /**
     * Loads events where the current user is either the primary organizer or a co-organizer.
     * Uses a snapshot listener to keep the list updated in real-time.
     */
    private void loadOrganizerEvents() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        db.collection("events")
                .where(Filter.or(
                        Filter.equalTo("organizerId", userId),
                        Filter.arrayContains("coOrganizerIds", userId)
                ))
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

    /**
     * Checks if the user is authorized to access organizer features.
     * Redirects to the entrant view if the user is blocked.
     * @param Allow Runnable to execute if access is granted.
     */
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

    /**
     * Redirects the user away from the organizer dashboard and displays an error message.
     * Triggered when a user is found in the blocked organizers list.
     */
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
