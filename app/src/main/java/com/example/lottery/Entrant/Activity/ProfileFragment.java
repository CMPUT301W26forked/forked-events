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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lottery.Event;
import com.example.lottery.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

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
    private Button btnDeleteProfile;
    private Switch switchNotifications;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean isGuest;

    private String currentName = "";
    private String currentEmail = "";
    private String currentPhone = "";

    /**
     * Inflates the profile screen layout and initializes all UI elements.
     * Also sets up Firebase, button listeners, and loads the user's profile and registered events.
     */
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
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile);
        switchNotifications = view.findViewById(R.id.switchNotifications);

        recyclerView = view.findViewById(R.id.rvMyEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyEventsAdapter(myEvents, this::openEventDetails);
        recyclerView.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> logoutUser());
        btnDeleteProfile.setOnClickListener(v -> showDeleteProfileDialog());

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

    /**
     * Reloads the profile and event list whenever the fragment becomes visible again.
     * This keeps the displayed data updated after returning from other screens.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        loadMyEvents();
    }

    /**
     * Shows a logout confirmation dialog and signs the user out if confirmed.
     * After logout, the user is redirected to the login screen.
     */
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

    /**
     * Displays a confirmation dialog before permanently deleting the user's profile.
     * This helps prevent accidental profile deletion.
     */
    private void showDeleteProfileDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to permanently delete your profile? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteUserProfile())
                .show();
    }

    /**
     * Starts the profile deletion process by first retrieving the user's registered events.
     * The user must be removed from all event references before profile data is deleted.
     */
    private void deleteUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    List<String> registeredEventIds = (List<String>) userDoc.get("registeredEventIds");
                    if (registeredEventIds == null) {
                        registeredEventIds = new ArrayList<>();
                    }

                    removeUserFromEventsAndDeleteData(currentUser, registeredEventIds);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Removes the user from confirmed and waitlisted event lists for all registered events.
     * Once all event cleanup tasks finish, it continues with notification and profile deletion.
     */
    private void removeUserFromEventsAndDeleteData(FirebaseUser currentUser, List<String> registeredEventIds) {
        List<Task<?>> eventTasks = new ArrayList<>();

        for (String eventId : registeredEventIds) {
            Task<?> task = db.collection("events")
                    .document(eventId)
                    .get()
                    .continueWithTask(taskSnapshot -> {
                        if (!taskSnapshot.isSuccessful() || taskSnapshot.getResult() == null || !taskSnapshot.getResult().exists()) {
                            return Tasks.forResult(null);
                        }

                        DocumentSnapshot eventDoc = taskSnapshot.getResult();

                        List<String> confirmedIds = (List<String>) eventDoc.get("confirmedEntrantIds");
                        List<String> waitlistedIds = (List<String>) eventDoc.get("waitlistedEntrantIds");

                        WriteBatch batch = db.batch();
                        boolean needsUpdate = false;

                        if (confirmedIds != null && confirmedIds.contains(userId)) {
                            batch.update(eventDoc.getReference(),
                                    "confirmedEntrantIds", FieldValue.arrayRemove(userId),
                                    "confirmedCount", Math.max(0, confirmedIds.size() - 1));
                            needsUpdate = true;
                        }

                        if (waitlistedIds != null && waitlistedIds.contains(userId)) {
                            batch.update(eventDoc.getReference(),
                                    "waitlistedEntrantIds", FieldValue.arrayRemove(userId),
                                    "waitlistCount", Math.max(0, waitlistedIds.size() - 1));
                            needsUpdate = true;
                        }

                        if (needsUpdate) {
                            return batch.commit();
                        } else {
                            return Tasks.forResult(null);
                        }
                    });

            eventTasks.add(task);
        }

        Tasks.whenAllComplete(eventTasks)
                .addOnSuccessListener(tasks -> deleteNotificationsThenProfile(currentUser))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to remove user from events", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Deletes all notification documents under the user's notification subcollection.
     * After notifications are removed, it proceeds to delete the main profile and auth account.
     */
    private void deleteNotificationsThenProfile(FirebaseUser currentUser) {
        db.collection("users")
                .document(userId)
                .collection("notification")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Task<?>> deleteTasks = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        deleteTasks.add(doc.getReference().delete());
                    }

                    Tasks.whenAllComplete(deleteTasks)
                            .addOnSuccessListener(tasks -> deleteUserDocumentAndAuth(currentUser))
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to delete notifications", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Deletes the user's Firestore document and then deletes their Firebase Authentication account.
     * If successful, the app returns the user to the login screen.
     */
    private void deleteUserDocumentAndAuth(FirebaseUser currentUser) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(unused -> {
                    currentUser.delete()
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(getContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(requireContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                                requireActivity().finish();
                            })
                            .addOnFailureListener(e -> {
                                String message = e.getMessage() != null ? e.getMessage() : "";

                                if (message.toLowerCase().contains("recent")) {
                                    Toast.makeText(getContext(),
                                            "Please log in again before deleting your account.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            "Failed to delete auth account",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete profile data", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Loads the user's profile data from Firestore and updates the UI fields.
     * If no profile exists yet, a default user document is created.
     */
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentName = safeString(documentSnapshot.getString("name"));
                        Boolean notifEnabled = documentSnapshot.getBoolean("notificationsEnabled");
                        boolean isEnabled = (notifEnabled == null) || notifEnabled; // default true
                        switchNotifications.setChecked(isEnabled);

                        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            db.collection("users")
                                    .document(userId)
                                    .update("notificationsEnabled", isChecked)
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(getContext(),
                                                    isChecked ? "Notifications enabled" : "Notifications disabled",
                                                    Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to update preference", Toast.LENGTH_SHORT).show());
                        });
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

    /**
     * Updates the displayed name, email, and phone number on the profile screen.
     * It also shows a default message when the phone number is missing.
     */
    private void updateProfileUI() {
        tvProfileName.setText(currentName);
        tvProfileEmail.setText(currentEmail);
        tvProfilePhone.setText(TextUtils.isEmpty(currentPhone) ? "Phone: Not added" : "Phone: " + currentPhone);
    }

    /**
     * Opens a dialog where the user can edit their profile information.
     * Guest users can edit email, while regular users only view their auth email.
     */
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

    /**
     * Saves updated profile details to Firestore and refreshes the local UI values.
     * Guest email is updated only when applicable.
     */
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

    /**
     * Checks whether an event with the given ID is already in the user's local event list.
     * This prevents duplicate events from being added to the RecyclerView data.
     */
    private boolean containsEvent(String eventId) {
        for (Event event : myEvents) {
            if (event.getEventId() != null && event.getEventId().equals(eventId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads all events registered by the user from Firestore and displays them in the RecyclerView.
     * Each event is fetched individually and only valid matching events are added.
     */
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

    /**
     * Converts a Firestore event document into an Event object for display in the profile screen.
     * Only events where the user is confirmed or waitlisted are returned.
     */
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

    /**
     * Opens the selected event's details screen by creating and displaying a new fragment.
     * The selected event ID is passed through a bundle.
     */
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

    /**
     * Returns an empty string when the given value is null.
     * This prevents null values from causing UI or string handling issues.
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }
}