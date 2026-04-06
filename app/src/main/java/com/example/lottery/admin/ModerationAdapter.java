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

/**
 * Adapter for displaying items in the moderation list.
 * Supports image loading via Glide and provides a removal callback for administrative actions.
 */
public class ModerationAdapter extends RecyclerView.Adapter<ModerationAdapter.ViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a moderation item is removed.
     */
    public interface OnRemoveListener {
        /**
         * Called when the remove button for an item is clicked.
         *
         * @param eventId  The ID of the event associated with the item.
         * @param position The adapter position of the item.
         */
        void onRemove(String eventId, int position);
    }

    private List<ModerationItem> items;
    private OnRemoveListener removeListener;

    /**
     * Constructs a new ModerationAdapter.
     *
     * @param items          The list of moderation items to be displayed.
     * @param removeListener The listener to handle removal events.
     */
    public ModerationAdapter(List<ModerationItem> items, OnRemoveListener removeListener) {
        this.items = items;
        this.removeListener = removeListener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View for a moderation item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_moderation, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder to update.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModerationItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());

        Glide.with(holder.ivPoster.getContext())
                .load(item.getImageUrl())
                .centerCrop()
                .into(holder.ivPoster);

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && removeListener != null) {
                removeListener.onRemove(item.getEventId(), currentPos);
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of moderation items.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class for moderation list items, providing access to UI components.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivPoster;
        MaterialButton btnRemove;

        /**
         * Constructs a new ViewHolder.
         *
         * @param itemView The view representing a single moderation item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvModerationTitle);
            ivPoster = itemView.findViewById(R.id.ivPosterThumbnail);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
