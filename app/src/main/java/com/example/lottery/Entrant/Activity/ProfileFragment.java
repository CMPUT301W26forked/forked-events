package com.example.lottery.Entrant.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lottery.Event;
import com.example.lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyEventsAdapter adapter;
    private final List<Event> myEvents = new ArrayList<>();

    private TextView tvProfileName, tvProfileEmail, tvProfilePhone;
    private Button btnEditDetails;
    private Button btnLogout;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean isGuest;

    private String currentName = "";
    private String currentEmail = "";
    private String currentPhone = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return view;
        }

        userId = currentUser.getUid();
        isGuest = currentUser.isAnonymous();

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        btnEditDetails = view.findViewById(R.id.btnEditDetails);
        btnLogout = view.findViewById(R.id.btnLogout);

        recyclerView = view.findViewById(R.id.rvMyEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyEventsAdapter(myEvents, this::openEventDetails);
        recyclerView.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> logoutUser());

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EntrantEventsFragment())
                    .commit();

            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNav);

            bottomNav.setSelectedItemId(R.id.nav_events);
        });

        btnEditDetails.setOnClickListener(v -> showEditProfileDialog());

        loadUserProfile();
        loadMyEvents();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        loadMyEvents();
    }

    private void logoutUser() {
        new AlertDialog.Builder(getContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log Out", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    requireActivity().finish();
                })
                .show();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentName = safeString(documentSnapshot.getString("name"));
                        currentPhone = safeString(documentSnapshot.getString("phone"));

                        if (isGuest) {
                            currentEmail = safeString(documentSnapshot.getString("email"));
                        } else {
                            currentEmail = safeString(currentUser.getEmail());
                        }
                    } else {
                        Map<String, Object> newUser = new HashMap<>();
                        newUser.put("uid", userId);
                        newUser.put("name", "");
                        newUser.put("phone", "");
                        newUser.put("registeredEventIds", new ArrayList<String>());
                        newUser.put("isGuest", isGuest);
                        newUser.put("role", isGuest ? "guest" : "entrant");

                        if (isGuest) {
                            newUser.put("email", "");
                        } else {
                            newUser.put("email", safeString(currentUser.getEmail()));
                        }

                        db.collection("users")
                                .document(userId)
                                .set(newUser, SetOptions.merge());

                        currentName = "";
                        currentPhone = "";
                        currentEmail = isGuest ? "" : safeString(currentUser.getEmail());
                    }

                    if (TextUtils.isEmpty(currentName)) currentName = "No name";
                    if (TextUtils.isEmpty(currentEmail)) currentEmail = "No email";

                    updateProfileUI();
                    btnEditDetails.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateProfileUI() {
        tvProfileName.setText(currentName);
        tvProfileEmail.setText(currentEmail);
        tvProfilePhone.setText(TextUtils.isEmpty(currentPhone) ? "Phone: Not added" : "Phone: " + currentPhone);
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);

        EditText etEditName = dialogView.findViewById(R.id.etEditName);
        EditText etEditEmail = dialogView.findViewById(R.id.etEditEmail);
        EditText etEditPhone = dialogView.findViewById(R.id.etEditPhone);

        etEditName.setText("No name".equals(currentName) ? "" : currentName);
        etEditPhone.setText(currentPhone);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (isGuest) {
            etEditEmail.setText("No email".equals(currentEmail) ? "" : currentEmail);
            etEditEmail.setEnabled(true);
        } else {
            etEditEmail.setText(currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : currentEmail);
            etEditEmail.setEnabled(false);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Details")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etEditName.getText().toString().trim();
                    String newPhone = etEditPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(newName)) {
                        Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isGuest) {
                        String newEmail = etEditEmail.getText().toString().trim();

                        if (TextUtils.isEmpty(newEmail)) {
                            Toast.makeText(getContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveProfile(newName, newEmail, newPhone);
                    } else {
                        saveProfile(newName, null, newPhone);
                    }
                })
                .show();
    }

    private void saveProfile(String name, String email, String phone) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        if (isGuest && email != null) {
            updates.put("email", email);
        }

        db.collection("users")
                .document(userId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    currentName = name;
                    currentPhone = phone;

                    if (isGuest && email != null) {
                        currentEmail = email;
                    } else {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        currentEmail = currentUser != null && currentUser.getEmail() != null
                                ? currentUser.getEmail()
                                : currentEmail;
                    }

                    updateProfileUI();
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private boolean containsEvent(String eventId) {
        for (Event event : myEvents) {
            if (event.getEventId() != null && event.getEventId().equals(eventId)) {
                return true;
            }
        }
        return false;
    }

    private void loadMyEvents() {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    myEvents.clear();

                    if (!userDoc.exists()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    List<String> registeredEventIds = (List<String>) userDoc.get("registeredEventIds");
                    if (registeredEventIds == null || registeredEventIds.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    final int totalEvents = registeredEventIds.size();
                    final int[] loadedCount = {0};

                    for (String eventId : registeredEventIds) {
                        db.collection("events")
                                .document(eventId)
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    loadedCount[0]++;

                                    if (eventDoc.exists()) {
                                        Event event = mapFirestoreDocToEvent(eventDoc);
                                        if (event != null && !containsEvent(event.getEventId())) {
                                            myEvents.add(event);
                                        }
                                    }

                                    if (loadedCount[0] == totalEvents) {
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalEvents) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                );
    }

    private Event mapFirestoreDocToEvent(DocumentSnapshot doc) {
        String eventId = doc.getId();
        String title = safeString(doc.getString("name"));
        String description = safeString(doc.getString("description"));
        String location = safeString(doc.getString("location"));

        String date = "";
        java.util.Date start = doc.getDate("registrationStart");
        java.util.Date end = doc.getDate("registrationEnd");
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if (start != null && end != null) {
            date = sdf.format(start) + " - " + sdf.format(end);
        }

        List<String> waitlistedEntrantIds = (List<String>) doc.get("waitlistedEntrantIds");
        List<String> confirmedEntrantIds = (List<String>) doc.get("confirmedEntrantIds");

        String status;
        if (confirmedEntrantIds != null && confirmedEntrantIds.contains(userId)) {
            status = "Upcoming";
        } else if (waitlistedEntrantIds != null && waitlistedEntrantIds.contains(userId)) {
            status = "Waitlisted";
        } else {
            return null;
        }

        Event event = new Event(title, status, description, location, date, "", "", "");
        event.setEventId(eventId);
        return event;
    }

    private void openEventDetails(Event event) {
        EntrantEventDetailsFragment detailsFragment = new EntrantEventDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getEventId());
        detailsFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}