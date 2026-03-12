package com.example.lottery.Entrant.Activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.Event;
import com.example.lottery.R;

import java.util.List;

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder> {

    public interface OnViewDetailsClickListener {
        void onViewDetailsClick(Event event);
    }

    private final List<Event> eventList;
    private final OnViewDetailsClickListener listener;

    public MyEventsAdapter(List<Event> eventList, OnViewDetailsClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
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

        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventStatus.setText(event.getStatus());

        String status = event.getStatus() == null ? "" : event.getStatus().trim();

        if (status.equalsIgnoreCase("Upcoming")) {
            holder.tvEventStatus.setText("Upcoming");
            holder.tvEventStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            holder.tvEventStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if (status.equalsIgnoreCase("Waitlisted")) {
            holder.tvEventStatus.setText("Waitlisted");
            holder.tvEventStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3E5F5")));
            holder.tvEventStatus.setTextColor(Color.parseColor("#9C27B0"));
        } else {
            holder.tvEventStatus.setText(status);
            holder.tvEventStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEEEEE")));
            holder.tvEventStatus.setTextColor(Color.BLACK);
        }

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailsClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class MyEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle, tvEventDate, tvEventStatus;
        Button btnViewDetails;

        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvMyEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvMyEventDate);
            tvEventStatus = itemView.findViewById(R.id.tvMyEventStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}