package com.example.lottery.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.lottery.R;
import com.example.lottery.Entrant.Activity.LoginActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment representing the admin dashboard.
 * Displays moderation stats and provides navigation to admin features.
 */
public class AdminDashboardFragment extends Fragment {

    private TextView tvAdminId;
    private View cvEventModeration, cvImageModeration, cvProfileModeration, cvNotificationLog;
    private Button btnTempLogout;
    private TextView tvImageModCount, tvEventModCount, tvProfileModCount;
    private FirebaseFirestore db;
    private TextView tvNotificationLogTitle;
    private TextView tvNotificationLogCount;

    /**
     * Initializes the UI, Firebase instance, and loads dashboard data.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        db = FirebaseFirestore.getInstance();

        tvAdminId = view.findViewById(R.id.tvAdminId);
        cvEventModeration = view.findViewById(R.id.cvEventModeration);
        cvImageModeration = view.findViewById(R.id.cvImageModeration);
        cvProfileModeration = view.findViewById(R.id.cvProfileModeration);
        cvNotificationLog = view.findViewById(R.id.cvNotificationLog);
        btnTempLogout = view.findViewById(R.id.btnTempLogout);

        tvImageModCount = view.findViewById(R.id.tvImageModCount);
        tvEventModCount = view.findViewById(R.id.tvEventModCount);
        tvProfileModCount = view.findViewById(R.id.tvProfileModCount);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tvAdminId.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        tvNotificationLogTitle = view.findViewById(R.id.tvNotificationsTitle);
        tvNotificationLogCount = view.findViewById(R.id.tvNotificaionLogCount);

        loadNotificationLogCount();
        loadModerationCounts();
        setupClickListeners();

        return view;
    }

    /**
     * Loads and displays counts for image, event, and profile moderation from Firestore.
     */
    private void loadModerationCounts() {
        // Image Moderation Count
        db.collection("events")
                .whereGreaterThan("posterUri", "")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvImageModCount != null) {
                        tvImageModCount.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load image count", Toast.LENGTH_SHORT).show();
                    }
                });

        // Event Moderation Count
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvEventModCount != null) {
                        tvEventModCount.setText(String.valueOf(querySnapshot.size()));
                    }
                });

        // Profile Moderation Count (excluding admins and organizers)
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int profileCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String role = doc.getString("role");

                        if ("admin".equalsIgnoreCase(role) || "organizer".equalsIgnoreCase(role)) {
                            continue;
                        }

                        profileCount++;
                    }

                    if (tvProfileModCount != null) {
                        tvProfileModCount.setText(String.valueOf(profileCount));
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load profile count", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Sets click listeners for dashboard cards and logout button.
     * Handles navigation between admin fragments and logout action.
     */
    private void setupClickListeners() {
        cvEventModeration.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, new AdminEventModerationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cvNotificationLog.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, new NotificationLogsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cvImageModeration.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, new ImageModerationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cvProfileModeration.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, new AdminProfileModerationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnTempLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    /**
     * Loads and groups notification logs from Firestore
     * to display the number of unique organizer-sent notifications.
     */
    private void loadNotificationLogCount() {
        FirebaseFirestore.getInstance()
                .collectionGroup("notification")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Integer> groupedLogs = new HashMap<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String type = doc.getString("type");
                        if (!isOrganizerSentType(type)) {
                            continue;
                        }

                        String eventId = doc.getString("eventId");
                        if (eventId == null || eventId.isEmpty()) {
                            eventId = "";
                        }

                        String audience = doc.getString("audience");
                        if (audience == null || audience.isEmpty()) {
                            audience = "Unknown";
                        }

                        Timestamp createdAt = doc.getTimestamp("createdAt");
                        long createdAtMillis = createdAt != null ? createdAt.toDate().getTime() : 0L;

                        String key = eventId + "|" + createdAtMillis + "|" + type + "|" + audience;
                        groupedLogs.put(key, 1);
                    }

                    tvNotificationLogCount.setText(String.valueOf(groupedLogs.size()));
                })
                .addOnFailureListener(e -> tvNotificationLogCount.setText("-"));
    }

    /**
     * Checks whether a notification type was sent by an organizer.
     */
    private boolean isOrganizerSentType(String type) {
        return "MESSAGE".equalsIgnoreCase(type)
                || "SELECTED".equalsIgnoreCase(type)
                || "NOT_SELECTED".equalsIgnoreCase(type)
                || "WAITLIST_INVITE".equalsIgnoreCase(type)
                || "CO_ORGANIZER_INVITE".equalsIgnoreCase(type);
    }
}