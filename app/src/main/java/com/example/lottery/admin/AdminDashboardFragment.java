package com.example.lottery.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdminDashboardFragment extends Fragment {

    private TextView tvAdminId;
    private View cvEventModeration, cvImageModeration, cvProfileModeration, cvNotificationLog;
    private Button btnTempLogout;
    private TextView tvNotificationLogTitle;
    private TextView tvNotificationLogCount;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        tvAdminId = view.findViewById(R.id.tvAdminId);
        cvEventModeration = view.findViewById(R.id.cvEventModeration);
        cvImageModeration = view.findViewById(R.id.cvImageModeration);
        cvProfileModeration = view.findViewById(R.id.cvProfileModeration);
        cvNotificationLog = view.findViewById(R.id.cvNotificationLog);
        btnTempLogout = view.findViewById(R.id.btnTempLogout);
        tvNotificationLogTitle = view.findViewById(R.id.tvNotificationsTitle);
        tvNotificationLogCount = view.findViewById(R.id.tvNotificaionLogCount);


        loadNotificationLogCount();

        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        cvEventModeration.setOnClickListener(v -> {
            // Navigate to Event Moderation
        });

        cvNotificationLog.setOnClickListener(v -> {
            // Navigate to Notification logs
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
            // Navigate to Profile Moderation
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

    private boolean isOrganizerSentType(String type) {
        return "MESSAGE".equalsIgnoreCase(type)
                || "SELECTED".equalsIgnoreCase(type)
                || "NOT_SELECTED".equalsIgnoreCase(type)
                || "WAITLIST_INVITE".equalsIgnoreCase(type)
                || "CO_ORGANIZER_INVITE".equalsIgnoreCase(type);
    }


}
