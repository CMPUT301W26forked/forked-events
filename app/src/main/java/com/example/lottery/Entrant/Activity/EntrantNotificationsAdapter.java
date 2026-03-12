package com.example.lottery.Entrant.Activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Model.EntrantInvitation;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EntrantNotificationsAdapter extends RecyclerView.Adapter<EntrantNotificationsAdapter.NotificationViewHolder> {

    private final ArrayList<EntrantInvitation> invitationList;
    private final Context context;
    private final FirebaseFirestore db;

    public EntrantNotificationsAdapter(ArrayList<EntrantInvitation> invitationList, Context context) {
        this.invitationList = invitationList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotificationTitle, tvNotificationEvent, tvNotificationDate, tvNotificationStatus;
        MaterialButton btnAccept, btnDecline, btnViewDetails;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationEvent = itemView.findViewById(R.id.tvNotificationEvent);
            tvNotificationDate = itemView.findViewById(R.id.tvNotificationDate);
            tvNotificationStatus = itemView.findViewById(R.id.tvNotificationStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        EntrantInvitation invitation = invitationList.get(position);

        holder.tvNotificationTitle.setText("You have been invited to an event!");
        holder.tvNotificationEvent.setText("Event ID: " + invitation.getEventId());
        holder.tvNotificationDate.setText("Date unavailable");
        holder.tvNotificationStatus.setText(invitation.getStatus());

        holder.btnAccept.setOnClickListener(v -> {
            db.collection("invitations")
                    .document(invitation.getInvitationId())
                    .update("status", "ACCEPTED")
                    .addOnSuccessListener(unused -> {
                        // 1. Update status in subcollection to CONFIRMED
                        db.collection("events").document(invitation.getEventId())
                                .collection("entrants").document(invitation.getEntrantId())
                                .update("status", "CONFIRMED")
                                .addOnSuccessListener(aVoid -> {
                                    // 2. Add to registeredEntrantIds array (Final List)
                                    // Also remove from pendingEntrantIds array
                                    db.collection("events").document(invitation.getEventId())
                                            .update("registeredEntrantIds", FieldValue.arrayUnion(invitation.getEntrantId()),
                                                    "pendingEntrantIds", FieldValue.arrayRemove(invitation.getEntrantId()),
                                                    "confirmedCount", FieldValue.increment(1))
                                            .addOnSuccessListener(v2 -> {
                                                holder.tvNotificationStatus.setText("ACCEPTED");
                                                Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show();
                                            });
                                });
                    });
        });

        holder.btnDecline.setOnClickListener(v -> {
            db.collection("invitations")
                    .document(invitation.getInvitationId())
                    .update("status", "DECLINED")
                    .addOnSuccessListener(unused -> {
                        // 1. Update subcollection status to CANCELLED
                        db.collection("events").document(invitation.getEventId())
                                .collection("entrants").document(invitation.getEntrantId())
                                .update("status", "CANCELLED")
                                .addOnSuccessListener(aVoid -> {
                                    // 2. Remove from pending and add to cancelled array
                                    db.collection("events").document(invitation.getEventId())
                                            .update("pendingEntrantIds", FieldValue.arrayRemove(invitation.getEntrantId()),
                                                    "cancelledEntrantIds", FieldValue.arrayUnion(invitation.getEntrantId()))
                                            .addOnSuccessListener(v2 -> {
                                                holder.tvNotificationStatus.setText("DECLINED");
                                                Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show();
                                            });
                                });
                    });
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            Toast.makeText(context, "Open event details here", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return invitationList.size();
    }
}
