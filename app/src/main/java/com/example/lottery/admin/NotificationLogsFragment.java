package com.example.lottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * admin view for organizer notification logs
 */
public class NotificationLogsFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationLogsAdapter adapter;
    private List<NotificationLogItem> logItems = new ArrayList<>();
    private TextView tvLogCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_logs, container, false);

        recyclerView = view.findViewById(R.id.rvNotificationLogs);
        tvLogCount = view.findViewById(R.id.tvLogCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationLogsAdapter(logItems);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        loadLogs();

        return view;
    }

    /**
     * load notification logs
     */
    public void loadLogs() {
        FirebaseFirestore.getInstance()
                .collectionGroup("notification")
                .get()
                .addOnSuccessListener(qs -> {
                    logItems.clear();

                    Map<String, TempLogGroup> groupedLogs = new HashMap<>();

                    for (QueryDocumentSnapshot doc: qs) {
                        String type = doc.getString("type");
                        if (!isOrganizerSentType(type)) {
                            continue;
                        }

                        String eventId = doc.getString("eventId");
                        if (eventId == null) {
                            eventId = "";
                        }

                        String eventName = doc.getString("eventName");
                        if (eventName == null || eventName.isEmpty()) {
                            eventName = eventId;
                        }

                        String audience = doc.getString("audience");
                        if (audience == null || audience.isEmpty()) {
                            audience = "Unknown";
                        }

                        String message = doc.getString("message");
                        if (message == null) {
                            message = "";
                        }

                        Timestamp createdAt = doc.getTimestamp("createdAt");
                        long createdAtMillis = createdAt != null ? createdAt.toDate().getTime() : 0L;

                        String key = eventId + "|" + createdAtMillis + "|" + type + "|" + audience;

                        if (!groupedLogs.containsKey(key)) {
                            groupedLogs.put(key, new TempLogGroup(
                                    eventName,
                                    eventId,
                                    type,
                                    audience,
                                    message,
                                    createdAt,
                                    1
                            ));
                        } else {
                            groupedLogs.get(key).recipientCount++;
                        }
                    }

                    for (TempLogGroup group : groupedLogs.values()) {
                        logItems.add(new NotificationLogItem(
                                group.eventName,
                                group.eventId,
                                group.type,
                                group.message,
                                group.createdAt,
                                group.recipientCount,
                                group.audience
                        ));
                    }

                    Collections.sort(logItems, (a, b) -> {
                        long aTime = a.getCreatedAt() != null ? a.getCreatedAt().toDate().getTime() : 0L;
                        long bTime = b.getCreatedAt() != null ? b.getCreatedAt().toDate().getTime() : 0L;
                        return Long.compare(bTime, aTime);
                    });

                    tvLogCount.setText(String.valueOf(logItems.size()));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * check if sent by organizer by type
     * @param type
     * @return
     */
    private boolean isOrganizerSentType(String type) {
        return "MESSAGE".equalsIgnoreCase(type)
                || "SELECTED".equalsIgnoreCase(type)
                || "NOT_SELECTED".equalsIgnoreCase(type)
                || "WAITLIST_INVITE".equalsIgnoreCase(type)
                || "CO_ORGANIZER_INVITE".equalsIgnoreCase(type);
    }

    /**
     * temporary group for sorting
     */
    private static class TempLogGroup {
        String eventName;
        String eventId;
        String type;
        String audience;
        String message;
        Timestamp createdAt;
        int recipientCount;

        TempLogGroup(String eventName, String eventId, String type, String audience, String message, Timestamp createdAt, int recipientCount) {
            this.eventName = eventName;
            this.eventId = eventId;
            this.type = type;
            this.audience = audience;
            this.message = message;
            this.createdAt = createdAt;
            this.recipientCount = recipientCount;
        }
    }


}
