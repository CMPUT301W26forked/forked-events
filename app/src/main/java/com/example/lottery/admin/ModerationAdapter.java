package com.example.lottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ModerationAdapter extends RecyclerView.Adapter<ModerationAdapter.ViewHolder> {

    public interface OnRemoveListener {
        void onRemove(String eventId, int position);
    }

    private List<ModerationItem> items;
    private OnRemoveListener removeListener;

    public ModerationAdapter(List<ModerationItem> items, OnRemoveListener removeListener) {
        this.items = items;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_moderation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModerationItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());

        // Load image with Glide
        Glide.with(holder.ivPoster.getContext())
                .load(item.getImageUrl())
                .centerCrop()
                .into(holder.ivPoster);

        // Remove button — use getAdapterPosition() at click time, not the stale bind-time position
        holder.btnOption1.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_ID && removeListener != null) {
                removeListener.onRemove(item.getEventId(), currentPos);
            }
        });

        holder.btnOption2.setOnClickListener(v -> {
            // Reserved for future use
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivPoster;
        MaterialButton btnOption1, btnOption2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvModerationTitle);
            ivPoster = itemView.findViewById(R.id.ivPosterThumbnail);
            btnOption1 = itemView.findViewById(R.id.btnOption1);
            btnOption2 = itemView.findViewById(R.id.btnOption2);
        }
    }
}
