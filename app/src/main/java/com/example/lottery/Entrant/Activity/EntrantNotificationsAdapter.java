package com.example.lottery.Entrant.Activity;

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

import com.example.lottery.Entrant.Model.EntrantInvitation;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EntrantNotificationsAdapter extends RecyclerView.Adapter<EntrantNotificationsAdapter.NotificationViewHolder> {

    private final List<EntrantInvitation> invitationList;
    private final Context context;
    private final FirebaseFirestore db;

    public EntrantNotificationsAdapter(List<EntrantInvitation> invitationList, Context context) {
        this.invitationList = invitationList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds invitation data to the notification view holder.
     * Sets up the invitation UI. title, event details, and status.
     * Configures accept/decline button listeners with validation to check
     * the entrant is still in the pending list (not cancelled by organizer) before allowing response.
     *
     * @param holder The view holder to bind data to
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        EntrantInvitation invitation = invitationList.get(position);
        String status = invitation.getStatus();

        if (status == null) {
            status = "PENDING";
        }

        holder.tvNotificationDate.setText("Date unavailable");
        holder.tvNotificationStatus.setText(status);

        if ("WAITLISTED".equalsIgnoreCase(status)) {
            holder.tvNotificationTitle.setText("Waitlist Confirmation");
            holder.tvNotificationEvent.setText("You joined the waitlist for Event ID: " + invitation.getEventId());
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
        } else {
            holder.tvNotificationTitle.setText("You have been invited to an event!");
            holder.tvNotificationEvent.setText("Event ID: " + invitation.getEventId());

            if ("ACCEPTED".equalsIgnoreCase(status) || "DECLINED".equalsIgnoreCase(status)) {
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnDecline.setVisibility(View.GONE);
            } else {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnDecline.setVisibility(View.VISIBLE);
            }
        }

        holder.btnAccept.setOnClickListener(v -> {
            if ("ACCEPTED".equalsIgnoreCase(invitation.getStatus()) ||
                    "DECLINED".equalsIgnoreCase(invitation.getStatus())) {
                return;
            }

            // Check if entrant is still in pending list (not cancelled by organizer)
            db.collection("events")
                    .document(invitation.getEventId())
                    .get()
                    .addOnSuccessListener(eventDoc -> {
                        if (!eventDoc.exists()) {
                            Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> pendingIds = (List<String>) eventDoc.get("pendingEntrantIds");
                        if (pendingIds == null || !pendingIds.contains(invitation.getEntrantId())) {
                            Toast.makeText(context, "This invitation is no longer valid", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("invitations")
                                .document(invitation.getInvitationId())
                                .update("status", "ACCEPTED")
                                .addOnSuccessListener(unused -> {
                                    db.collection("events")
                                            .document(invitation.getEventId())
                                            .update(
                                                    "registeredEntrantIds", FieldValue.arrayUnion(invitation.getEntrantId()),
                                                    "pendingEntrantIds", FieldValue.arrayRemove(invitation.getEntrantId()),
                                                    "cancelledEntrantIds", FieldValue.arrayRemove(invitation.getEntrantId()),
                                                    "confirmedCount", FieldValue.increment(1)
                                            )
                                            .addOnSuccessListener(v2 -> {
                                                db.collection("events")
                                                        .document(invitation.getEventId())
                                                        .collection("entrants")
                                                        .document(invitation.getEntrantId())
                                                        .update("status", "CONFIRMED");

                                                invitation.setStatus("ACCEPTED");
                                                holder.tvNotificationStatus.setText("ACCEPTED");
                                                holder.btnAccept.setVisibility(View.GONE);
                                                holder.btnDecline.setVisibility(View.GONE);

                                                int adapterPosition = holder.getAdapterPosition();
                                                if (adapterPosition != RecyclerView.NO_POSITION) {
                                                    notifyItemChanged(adapterPosition);
                                                }

                                                Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(context, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                            );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to accept: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to verify invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        holder.btnDecline.setOnClickListener(v -> {
            if ("ACCEPTED".equalsIgnoreCase(invitation.getStatus()) ||
                    "DECLINED".equalsIgnoreCase(invitation.getStatus())) {
                return;
            }

            // Check if entrant is still in pending list (not cancelled by organizer)
            db.collection("events")
                    .document(invitation.getEventId())
                    .get()
                    .addOnSuccessListener(eventDoc -> {
                        if (!eventDoc.exists()) {
                            Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<String> pendingIds = (List<String>) eventDoc.get("pendingEntrantIds");
                        if (pendingIds == null || !pendingIds.contains(invitation.getEntrantId())) {
                            Toast.makeText(context, "This invitation is no longer valid", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("invitations")
                                .document(invitation.getInvitationId())
                                .update("status", "DECLINED")
                                .addOnSuccessListener(unused -> {
                                    db.collection("events")
                                            .document(invitation.getEventId())
                                            .update(
                                                    "pendingEntrantIds", FieldValue.arrayRemove(invitation.getEntrantId()),
                                                    "registeredEntrantIds", FieldValue.arrayRemove(invitation.getEntrantId()),
                                                    "cancelledEntrantIds", FieldValue.arrayUnion(invitation.getEntrantId())
                                            )
                                            .addOnSuccessListener(v2 -> {
                                                db.collection("events")
                                                        .document(invitation.getEventId())
                                                        .collection("entrants")
                                                        .document(invitation.getEntrantId())
                                                        .update("status", "DECLINED");

                                                invitation.setStatus("DECLINED");
                                                holder.tvNotificationStatus.setText("DECLINED");
                                                holder.btnAccept.setVisibility(View.GONE);
                                                holder.btnDecline.setVisibility(View.GONE);

                                                int adapterPosition = holder.getAdapterPosition();
                                                if (adapterPosition != RecyclerView.NO_POSITION) {
                                                    notifyItemChanged(adapterPosition);
                                                }

                                                Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(context, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                            );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to decline: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to verify invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            EntrantEventDetailsFragment fragment = new EntrantEventDetailsFragment();
            Bundle args = new Bundle();
            args.putString("eventId", invitation.getEventId());
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

    @Override
    public int getItemCount() {
        return invitationList.size();
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
}