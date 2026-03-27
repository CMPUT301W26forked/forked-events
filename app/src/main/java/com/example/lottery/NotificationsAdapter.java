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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private final List<Notification> notificationList;
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

        holder.tvTitle.setText(getDisplayTitle(notification));
        holder.tvEvent.setText(notification.getEventName() == null ? "" : notification.getEventName());
        holder.tvDate.setText(getDisplayDate(notification));
        holder.btnViewDetails.setVisibility(notification.getEventId() == null ? View.GONE : View.VISIBLE);

        bindStatusAndButtons(holder, notification);

        holder.btnAccept.setOnClickListener(v -> acceptInvitation(notification, holder));
        holder.btnDecline.setOnClickListener(v -> declineInvitation(notification, holder));

        holder.btnViewDetails.setOnClickListener(v -> {
            if (notification.getEventId() == null) {
                return;
            }

            EntrantEventDetailsFragment fragment = new EntrantEventDetailsFragment();
            Bundle args = new Bundle();
            args.putString("eventId", notification.getEventId());
            fragment.setArguments(args);

            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void bindStatusAndButtons(@NonNull NotificationViewHolder holder, @NonNull Notification notification) {
        String type = safe(notification.getType());
        String status = safe(notification.getStatus());

        holder.btnAccept.setVisibility(View.GONE);
        holder.btnDecline.setVisibility(View.GONE);

        if ("JOIN_WAITLIST".equalsIgnoreCase(type)) {
            holder.tvStatus.setText("Waitlisted");
            return;
        }

        if ("NOT_SELECTED".equalsIgnoreCase(type)) {
            holder.tvStatus.setText("Not Selected");
            return;
        }

        boolean isInvite =
                "SELECTED".equalsIgnoreCase(type) ||
                        "WAITLIST_INVITE".equalsIgnoreCase(type) ||
                        "CO_ORGANIZER_INVITE".equalsIgnoreCase(type);

        if (isInvite) {
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Accepted");
                return;
            }

            if ("DECLINED".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Declined");
                return;
            }

            if ("WAITLIST_INVITE".equalsIgnoreCase(type)) {
                holder.tvStatus.setText("Waitlist Invite");
            } else if ("CO_ORGANIZER_INVITE".equalsIgnoreCase(type)) {
                holder.tvStatus.setText("Co-Organizer Invite");
            } else {
                holder.tvStatus.setText("Selected");
            }

            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
            return;
        }

        if (!status.isEmpty()) {
            holder.tvStatus.setText(status);
        } else if (!type.isEmpty()) {
            holder.tvStatus.setText(type);
        } else {
            holder.tvStatus.setText("");
        }
    }

    private void acceptInvitation(@NonNull Notification notification, @NonNull NotificationViewHolder holder) {
        if (notification.getNotificationId() == null || notification.getEventId() == null || entrantId == null) {
            Toast.makeText(context, "Missing notification or event info", Toast.LENGTH_SHORT).show();
            return;
        }

        holder.btnAccept.setEnabled(false);
        holder.btnDecline.setEnabled(false);

        DocumentReference notificationRef = db.collection("users")
                .document(entrantId)
                .collection("notification")
                .document(notification.getNotificationId());

        notificationRef.get()
                .addOnSuccessListener(snapshot -> handleAcceptAfterCheck(snapshot, notificationRef, notification, holder))
                .addOnFailureListener(e -> {
                    holder.btnAccept.setEnabled(true);
                    holder.btnDecline.setEnabled(true);
                    Toast.makeText(context, "Failed to check notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleAcceptAfterCheck(
            @NonNull DocumentSnapshot snapshot,
            @NonNull DocumentReference notificationRef,
            @NonNull Notification notification,
            @NonNull NotificationViewHolder holder
    ) {
        String currentStatus = snapshot.getString("status");

        if ("ACCEPTED".equalsIgnoreCase(currentStatus) || "DECLINED".equalsIgnoreCase(currentStatus)) {
            notification.setStatus(currentStatus);
            bindStatusAndButtons(holder, notification);
            Toast.makeText(context, "You have already responded to this invitation", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(notification.getEventId())
                .update(
                        "registeredEntrantIds", FieldValue.arrayUnion(entrantId),
                        "pendingEntrantIds", FieldValue.arrayRemove(entrantId),
                        "cancelledEntrantIds", FieldValue.arrayRemove(entrantId),
                        "confirmedCount", FieldValue.increment(1)
                )
                .addOnSuccessListener(unused ->
                        notificationRef.update("status", "ACCEPTED")
                                .addOnSuccessListener(unused2 -> {
                                    notification.setStatus("ACCEPTED");
                                    bindStatusAndButtons(holder, notification);

                                    int adapterPosition = holder.getAdapterPosition();
                                    if (adapterPosition != RecyclerView.NO_POSITION) {
                                        notifyItemChanged(adapterPosition);
                                    }

                                    Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    holder.btnAccept.setEnabled(true);
                                    holder.btnDecline.setEnabled(true);
                                    Toast.makeText(context, "Failed to update notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    holder.btnAccept.setEnabled(true);
                    holder.btnDecline.setEnabled(true);
                    Toast.makeText(context, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void declineInvitation(@NonNull Notification notification, @NonNull NotificationViewHolder holder) {
        if (notification.getNotificationId() == null || notification.getEventId() == null || entrantId == null) {
            Toast.makeText(context, "Missing notification or event info", Toast.LENGTH_SHORT).show();
            return;
        }

        holder.btnAccept.setEnabled(false);
        holder.btnDecline.setEnabled(false);

        DocumentReference notificationRef = db.collection("users")
                .document(entrantId)
                .collection("notification")
                .document(notification.getNotificationId());

        notificationRef.get()
                .addOnSuccessListener(snapshot -> handleDeclineAfterCheck(snapshot, notificationRef, notification, holder))
                .addOnFailureListener(e -> {
                    holder.btnAccept.setEnabled(true);
                    holder.btnDecline.setEnabled(true);
                    Toast.makeText(context, "Failed to check notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleDeclineAfterCheck(
            @NonNull DocumentSnapshot snapshot,
            @NonNull DocumentReference notificationRef,
            @NonNull Notification notification,
            @NonNull NotificationViewHolder holder
    ) {
        String currentStatus = snapshot.getString("status");

        if ("ACCEPTED".equalsIgnoreCase(currentStatus) || "DECLINED".equalsIgnoreCase(currentStatus)) {
            notification.setStatus(currentStatus);
            bindStatusAndButtons(holder, notification);
            Toast.makeText(context, "You have already responded to this invitation", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(notification.getEventId())
                .update(
                        "pendingEntrantIds", FieldValue.arrayRemove(entrantId),
                        "registeredEntrantIds", FieldValue.arrayRemove(entrantId),
                        "cancelledEntrantIds", FieldValue.arrayUnion(entrantId)
                )
                .addOnSuccessListener(unused ->
                        notificationRef.update("status", "DECLINED")
                                .addOnSuccessListener(unused2 -> {
                                    notification.setStatus("DECLINED");
                                    bindStatusAndButtons(holder, notification);

                                    int adapterPosition = holder.getAdapterPosition();
                                    if (adapterPosition != RecyclerView.NO_POSITION) {
                                        notifyItemChanged(adapterPosition);
                                    }

                                    Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    holder.btnAccept.setEnabled(true);
                                    holder.btnDecline.setEnabled(true);
                                    Toast.makeText(context, "Failed to update notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    holder.btnAccept.setEnabled(true);
                    holder.btnDecline.setEnabled(true);
                    Toast.makeText(context, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getDisplayTitle(Notification notification) {
        if (notification.getMessage() != null && !notification.getMessage().trim().isEmpty()) {
            return notification.getMessage();
        }
        if (notification.getTitle() != null && !notification.getTitle().trim().isEmpty()) {
            return notification.getTitle();
        }
        return "Notification";
    }

    private String getDisplayDate(Notification notification) {
        if (notification.getDate() != null && !notification.getDate().trim().isEmpty()) {
            return notification.getDate();
        }

        Timestamp createdAt = notification.getCreatedAt();
        if (createdAt == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(createdAt.toDate());
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
}