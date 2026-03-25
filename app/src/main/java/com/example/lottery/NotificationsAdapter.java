package com.example.lottery;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Activity.EntrantEventDetailsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private final String entrantId;
    private final Context context;
    private final FirebaseFirestore db;

    public NotificationsAdapter(List<Notification> notificationList, String entrantId, Context context) {
        this.notificationList = notificationList;
        this.entrantId = entrantId;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getMessage());
        holder.tvEvent.setText(notification.getEventName());
        holder.tvDate.setText(formatTimeStamp(notification.getCreatedAt()));
        holder.tvStatus.setText(notification.getType());

        // Default visibility
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnDecline.setVisibility(View.GONE);
        holder.btnViewDetails.setVisibility(View.VISIBLE);

        if ("JOIN_WAITLIST".equalsIgnoreCase(notification.getType())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_waitlisted);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_waitlisted_text));
            holder.tvStatus.setText("Waitlisted");
        } else if ("SELECTED".equalsIgnoreCase(notification.getType())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_invitation_text));
            holder.tvStatus.setText("Selected"); // ?
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
        } else if ("NOT_SELECTED".equalsIgnoreCase(notification.getType())) { // US 01.04.02
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_waitlisted);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_waitlisted_text));
            holder.tvStatus.setText("Not Selected");
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
        } else if ("WAITLIST_INVITE".equalsIgnoreCase(notification.getType())) { // US 01.05.06
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_invitation_text));
            holder.tvStatus.setText("Waitlist Invite");
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
        } else if ("CO_ORGANIZER_INVITE".equalsIgnoreCase(notification.getType())) { // US 01.09.01
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_invitation_text));
            holder.tvStatus.setText("Co-Organizer Invite");
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
        }

        holder.btnAccept.setOnClickListener(v -> {
            String eventId = notification.getEventId();
            if (eventId == null || entrantId == null) {
                Toast.makeText(context, "Missing event or user info", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("events").document(eventId)
                    .update("registeredEntrantIds", FieldValue.arrayUnion(entrantId),
                            "pendingEntrantIds", FieldValue.arrayRemove(entrantId),
                            "confirmedCount", FieldValue.increment(1))
                    .addOnSuccessListener(unused -> {
                        holder.tvStatus.setText("Accepted");
                        holder.btnAccept.setEnabled(false);
                        holder.btnDecline.setEnabled(false);
                        Toast.makeText(context, "You have accepted the invitation", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        holder.btnDecline.setOnClickListener(v -> {
            String eventId = notification.getEventId();
            if (eventId == null || entrantId == null) {
                Toast.makeText(context, "Missing event or user info", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("events").document(eventId)
                    .update("cancelledEntrantIds", FieldValue.arrayUnion(entrantId),
                            "pendingEntrantIds", FieldValue.arrayRemove(entrantId))
                    .addOnSuccessListener(unused -> {
                        holder.tvStatus.setText("Declined");
                        holder.btnAccept.setEnabled(false);
                        holder.btnDecline.setEnabled(false);
                        Toast.makeText(context, "You have declined the invitation", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (notification.getEventId() != null) {
                EntrantEventDetailsFragment fragment = new EntrantEventDetailsFragment();
                Bundle args = new Bundle();
                args.putString("eventId", notification.getEventId());
                fragment.setArguments(args);

                ((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvEvent, tvDate, tvStatus;
        MaterialButton btnAccept, btnDecline, btnViewDetails;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvEvent = itemView.findViewById(R.id.tvNotificationEvent);
            tvDate = itemView.findViewById(R.id.tvNotificationDate);
            tvStatus = itemView.findViewById(R.id.tvNotificationStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }

    private String formatTimeStamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
}
