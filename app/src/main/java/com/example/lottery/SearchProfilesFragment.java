package com.example.lottery;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * fragment for searching and displaying user profiles to invite
 */
public class SearchProfilesFragment extends Fragment {

    private String eventId;
    private String eventName;
    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<EntrantProfile> profileList;
    private List<EntrantProfile> filteredList;
    private Set<String> allowedIds;
    private FirebaseFirestore db;
    private FSEventRepo repo;
    private EditText etSearch;

    public static SearchProfilesFragment newInstance(String eventId, String eventName) {
        SearchProfilesFragment fragment = new SearchProfilesFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("event_name", eventName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_profiles, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");
            eventName = getArguments().getString("event_name");
        }

        db = FirebaseFirestore.getInstance();
        repo = new FSEventRepo();
        profileList = new ArrayList<>();
        filteredList = new ArrayList<>();
        allowedIds = new HashSet<>();

        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.rvProfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProfileAdapter(filteredList, profile -> {
            inviteUser(profile);
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        setupSearch();
        fetchAllProfiles();

        return view;
    }

    private void inviteUser(EntrantProfile profile) {
        if (eventId == null) {
            Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Update lists: add to pending, remove from cancelled (if they were there)
        db.collection("events").document(eventId)
                .update(
                        "pendingEntrantIds", FieldValue.arrayUnion(profile.getId()),
                        "cancelledEntrantIds", FieldValue.arrayRemove(profile.getId())
                )
                .addOnSuccessListener(aVoid -> {
                    // 2. Create a notification for the user using the repository
                    repo.createNotification(eventId, profile.getId(), eventName,
                        "You have been invited to join the private event: " + (eventName != null ? eventName : "New Event"),
                        new RepoCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Toast.makeText(getContext(), "Invited " + profile.getName(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("Invite", "Failed to send notification", e);
                                Toast.makeText(getContext(), "User invited, but notification failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e("Invite", "Failed to invite user", e);
                    Toast.makeText(getContext(), "Failed to invite user", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * sets up text listener for search input
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * fetches relevant user profiles from firestore
     */
    private void fetchAllProfiles() {
        if (eventId == null) return;

        // First fetch the event to see who is allowed to be invited
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            allowedIds.clear();
            if (doc.exists()) {
                List<String> pending = (List<String>) doc.get("pendingEntrantIds");
                List<String> registered = (List<String>) doc.get("registeredEntrantIds");
                List<String> cancelled = (List<String>) doc.get("cancelledEntrantIds");

                if (pending != null) allowedIds.addAll(pending);
                if (registered != null) allowedIds.addAll(registered);
                if (cancelled != null) allowedIds.addAll(cancelled);
            }

            // Then fetch all users and filter
            db.collection("users")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        profileList.clear();
                        for (QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                            if (allowedIds.contains(userDoc.getId())) {
                                EntrantProfile profile = userDoc.toObject(EntrantProfile.class);
                                if (profile != null) {
                                    profile.setId(userDoc.getId());
                                    profileList.add(profile);
                                }
                            }
                        }
                        filterProfiles(etSearch.getText().toString());
                    });
        }).addOnFailureListener(e -> Log.e("SearchProfiles", "Error fetching event", e));
    }

    /**
     * filters the profile list based on query string
     * @param query search text
     */
    private void filterProfiles(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase().trim();

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(profileList);
        } else {
            for (EntrantProfile profile : profileList) {
                String name = profile.getName() != null ? profile.getName().toLowerCase() : "";
                String email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";
                
                if (name.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filteredList.add(profile);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
