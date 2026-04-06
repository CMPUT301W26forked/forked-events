package com.example.lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter class for displaying a list of events in a RecyclerView.
 * Binds event data to the item_event layout.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /**
     * Constructor for EventAdapter.
     * @param eventList list of events
     * @param listener click listener
     */
    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder for an event item.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvEventTitle.setText(event.getTitle());
        holder.tvStatus.setText(event.getStatus());
        holder.tvDescription.setText(event.getDescription());
        holder.tvLocation.setText(event.getLocation());
        holder.tvDate.setText(event.getDate());
        holder.tvSpots.setText(event.getSpots());
        holder.tvWaitlist.setText(event.getWaitlistInfo());
        holder.tvJoined.setText(event.getJoinedCount());

        if (event.getPosterUri() != null && !event.getPosterUri().isEmpty()) {
            holder.ivEventPoster.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(event.getPosterUri())
                    .centerCrop()
                    .into(holder.ivEventPoster);
        } else {
            holder.ivEventPoster.setVisibility(View.GONE);
        }

        holder.eventCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    /**
     * Returns total number of events.
     */
    @Override
    public int getItemCount() {
        return eventList == null ? 0 : eventList.size();
    }

    /**
     * ViewHolder class for holding event item views.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        CardView eventCard;
        ImageView ivEventPoster;
        TextView tvEventTitle, tvStatus, tvDescription, tvLocation, tvDate, tvSpots, tvWaitlist, tvJoined;

        /**
         * Constructor that initializes all views in the item layout.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventCard = itemView.findViewById(R.id.eventCard);
            ivEventPoster = itemView.findViewById(R.id.ivEventPoster);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSpots = itemView.findViewById(R.id.tvSpots);
            tvWaitlist = itemView.findViewById(R.id.tvWaitlist);
            tvJoined = itemView.findViewById(R.id.tvJoined);
        }
    }
}