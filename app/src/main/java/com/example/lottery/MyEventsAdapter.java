package com.example.lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder> {

    private List<Event> eventList;

    public MyEventsAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_event, parent, false);
        return new MyEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate());
        holder.tvStatus.setText(event.getStatus());

        if ("Upcoming".equalsIgnoreCase(event.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_event_tag_open);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.tag_open_text));
        } else if ("Waitlisted".equalsIgnoreCase(event.getStatus())) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_waitlisted);
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.tag_waitlisted_text));
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class MyEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvStatus;

        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMyEventTitle);
            tvDate = itemView.findViewById(R.id.tvMyEventDate);
            tvStatus = itemView.findViewById(R.id.tvMyEventStatus);
        }
    }
}
