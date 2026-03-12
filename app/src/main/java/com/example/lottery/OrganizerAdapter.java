package com.example.lottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;

/**
 * Adapter for the organizer dashboard event list
 * Each item can be managed or edited
 */
public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.EventViewHolder> {

    private List<Event> eventList;
    private FragmentManager fragmentManager;
    MaterialButton btnManage;

    public OrganizerAdapter(List<Event> eventList, FragmentManager fragmentManager) {
        this.eventList = eventList;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organizer_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvTitle.setText(event.getTitle());
        // to management
        holder.btnManage.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getEventId());

            EventManagementFragment fragment = new EventManagementFragment();
            fragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        // to edit
        holder.btnEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getEventId());

            EventBuilderFragment fragment = new EventBuilderFragment();
            fragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        Button btnManage;
        Button btnEdit;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvOrganizerEventTitle);
            btnManage = itemView.findViewById(R.id.btnManage);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}