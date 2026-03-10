package com.example.lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
        holder.tvTitle.setText(notification.getTitle());
        holder.tvEvent.setText(notification.getEventName());
        holder.tvDate.setText(notification.getDate());
        holder.tvStatus.setText(notification.getStatus());

        if ("Joined".equalsIgnoreCase(notification.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_open_text));
        } else if ("Waitlisted".equalsIgnoreCase(notification.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_waitlisted);
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.tag_waitlisted_text));
        } else if ("Invitation".equalsIgnoreCase(notification.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open); // Should ideally be a blue one
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
}
