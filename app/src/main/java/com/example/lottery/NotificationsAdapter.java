package com.example.lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        // for selected, *** rest type not implemented ***
        if ("SELECTED".equalsIgnoreCase(notification.getType())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_invitation_text));
        }

        if ("Joined".equalsIgnoreCase(notification.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_open_text));
        } else if ("Waitlisted".equalsIgnoreCase(notification.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_waitlisted);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_waitlisted_text));
        } else if ("Invitation".equalsIgnoreCase(notification.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_invitation_text));
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvEvent, tvDate, tvStatus;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvEvent = itemView.findViewById(R.id.tvNotificationEvent);
            tvDate = itemView.findViewById(R.id.tvNotificationDate);
            tvStatus = itemView.findViewById(R.id.tvNotificationStatus);
        }
    }

    private String formatTimeStamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
}
