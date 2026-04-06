package com.example.lottery;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.RepoCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment for searching and displaying user profiles to invite or add as co-organizer.
 */
public class SearchProfilesFragment extends Fragment {

    private String eventId;
    private String eventName;
    private boolean isCoOrganizerMode = false;
    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<EntrantProfile> profileList;
    private List<EntrantProfile> filteredList;
    private Set<String> allowedIds;
    private FirebaseFirestore db;
    private FSEventRepo repo;
    private EditText etSearch;

    /**
     * Creates a new instance of SearchProfilesFragment for inviting users.
     * @param eventId The ID of the event.
     * @param eventName The name of the event.
     * @return A new instance of SearchProfilesFragment.
     */
    public static SearchProfilesFragment newInstance(String eventId, String eventName) {
        return newInstance(eventId, eventName, false);
    }

    /**
     * Creates a new instance of SearchProfilesFragment with an option for co-organizer mode.
     * @param eventId The ID of the event.
     * @param eventName The name of the event.
     * @param isCoOrganizerMode True if searching for a co-organizer, false for inviting entrants.
     * @return A new instance of SearchProfilesFragment.
     */
    public static SearchProfilesFragment newInstance(String eventId, String eventName, boolean isCoOrganizerMode) {
        SearchProfilesFragment fragment = new SearchProfilesFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        args.putString("event_name", eventName);
        args.putBoolean("is_co_organizer_mode", isCoOrganizerMode);
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
            isCoOrganizerMode = getArguments().getBoolean("is_co_organizer_mode", false);
        }

        db = FirebaseFirestore.getInstance();
        repo = new FSEventRepo();
        profileList = new ArrayList<>();
        filteredList = new ArrayList<>();
        allowedIds = new HashSet<>();

        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.rvProfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        if (tvTitle != null && isCoOrganizerMode) {
            tvTitle.setText("Add Co-organizer");
        }

        adapter = new ProfileAdapter(filteredList, profile -> {
            if (isCoOrganizerMode) {
                selectCoOrganizer(profile);
            } else {
                inviteUser(profile);
            }
        });

        if (isCoOrganizerMode) {
            adapter.setButtonText("Add");
        }

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

    /**
     * Sets the selected profile as a co-organizer and pops the back stack.
     * @param profile The selected entrant profile.
     */
    private void selectCoOrganizer(EntrantProfile profile) {
        Bundle result = new Bundle();
        result.putString("co_organizer_id", profile.getId());
        result.putString("co_organizer_name", profile.getName());
        getParentFragmentManager().setFragmentResult("co_organizer_selected", result);
        getParentFragmentManager().popBackStack();
    }

    /**
     * Invites the selected user to the event.
     * @param profile The entrant profile to invite.
     */
    private void inviteUser(EntrantProfile profile) {
        if (eventId == null) {
            Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .update(
                        "pendingEntrantIds", FieldValue.arrayUnion(profile.getId()),
                        "cancelledEntrantIds", FieldValue.arrayRemove(profile.getId())
                )
                .addOnSuccessListener(aVoid -> {
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
     * Sets up the search bar to filter profiles as the user types.
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
     * Fetches all eligible profiles from Firestore based on the current mode.
     * Excludes guest users and the current user.
     */
    private void fetchAllProfiles() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (isCoOrganizerMode) {
            db.collection("users")
                    .whereEqualTo("isGuest", false)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        profileList.clear();
                        for (QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                            if (currentUserId != null && currentUserId.equals(userDoc.getId())) {
                                continue;
                            }
                            EntrantProfile profile = userDoc.toObject(EntrantProfile.class);
                            if (profile != null) {
                                profile.setId(userDoc.getId());
                                profileList.add(profile);
                            }
                        }
                        filterProfiles(etSearch.getText().toString());
                    });
        } else {
            if (eventId == null) return;
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

                db.collection("users")
                        .whereEqualTo("isGuest", false)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            profileList.clear();
                            for (QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                                if (currentUserId != null && currentUserId.equals(userDoc.getId())) {
                                    continue;
                                }
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
    }

    /**
     * Filters the profile list based on the search query.
     * @param query The search query string.
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
