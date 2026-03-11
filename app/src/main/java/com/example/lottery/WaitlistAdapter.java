package com.example.lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.EntrantViewHolder> {

    private List<Entrant> entrantList;

    public WaitlistAdapter(List<Entrant> entrantList) {
        this.entrantList = entrantList;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waitlist_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = entrantList.get(position);
        holder.tvName.setText(entrant.getName());
        holder.tvDetails.setText(entrant.getEmail());
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEntrantName);
            tvDetails = itemView.findViewById(R.id.tvEntrantDetails);
        }
    }
}