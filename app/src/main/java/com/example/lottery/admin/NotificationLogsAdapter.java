package com.example.lottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Notification;
import com.example.lottery.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * adapter for notification logs list
 */
public class NotificationLogsAdapter extends RecyclerView.Adapter<NotificationLogsAdapter.LogViewHolder> {
    private List<NotificationLogItem> logItems;

    public NotificationLogsAdapter(List<NotificationLogItem> logItems) {
        this.logItems = logItems;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        NotificationLogItem item = logItems.get(position);

        String eventLabel = item.getEventName() != null && !item.getEventName().isEmpty() ? item.getEventName() : item.getEventId();

        holder.tvLogDate.setText(formatTimestamp(item.getCreatedAt()));
        holder.tvLogMessage.setText(item.getMessage() == null ? "" : item.getMessage());
        holder.tvLogRecipient.setText("To: " + item.getAudience() + " (" + item.getRecipientCount() + ")");
        holder.tvLogTitle.setText(eventLabel + " - " + item.getType());
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogTitle, tvLogRecipient, tvLogMessage, tvLogDate;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogTitle = itemView.findViewById(R.id.tvLogTitle);
            tvLogRecipient = itemView.findViewById(R.id.tvLogRecipient);
            tvLogMessage = itemView.findViewById(R.id.tvLogMessage);
            tvLogDate = itemView.findViewById(R.id.tvLogDate);
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "Invalid time";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    @Override
    public int getItemCount() {
        return logItems.size();
    }
}
