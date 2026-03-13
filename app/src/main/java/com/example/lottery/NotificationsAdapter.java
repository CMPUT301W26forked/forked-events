package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Entrant.Activity.EntrantEventDetailsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;

    public NotificationsAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
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
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
        }

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
