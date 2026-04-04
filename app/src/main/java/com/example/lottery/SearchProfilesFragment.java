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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private FirebaseFirestore db;
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
        profileList = new ArrayList<>();
        filteredList = new ArrayList<>();

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

        // 1. Add user to pendingEntrantIds in the event document
        db.collection("events").document(eventId)
                .update("pendingEntrantIds", FieldValue.arrayUnion(profile.getId()))
                .addOnSuccessListener(aVoid -> {
                    // 2. Create a notification for the user
                    sendInvitationNotification(profile);
                    Toast.makeText(getContext(), "Invited " + profile.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Invite", "Failed to invite user", e);
                    Toast.makeText(getContext(), "Failed to invite user", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendInvitationNotification(EntrantProfile profile) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", profile.getId());
        notification.put("eventId", eventId);
        notification.put("title", "Event Invitation");
        notification.put("message", "You have been invited to join the event: " + (eventName != null ? eventName : "New Event"));
        notification.put("type", "invitation");
        notification.put("timestamp", com.google.firebase.Timestamp.now());
        notification.put("isRead", false);

        db.collection("notifications").add(notification);
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
     * fetches all user profiles from firestore
     */
    private void fetchAllProfiles() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    profileList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        EntrantProfile profile = doc.toObject(EntrantProfile.class);
                        profile.setId(doc.getId());
                        profileList.add(profile);
                    }
                    filterProfiles(etSearch.getText().toString());
                });
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
