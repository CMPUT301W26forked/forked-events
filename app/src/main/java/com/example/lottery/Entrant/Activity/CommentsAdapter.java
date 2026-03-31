package com.example.lottery.Entrant.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final List<Comment> commentList;
    private String organizerId;

    /**
     * Constructor initializes the adapter with a list of comments.
     * This list is used to populate the RecyclerView.
     */
    public CommentsAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    /**
     * Sets the organizer ID to identify organizer comments.
     * Triggers UI refresh to update organizer badges.
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder by inflating the comment layout.
     * Called when RecyclerView needs a new item view.
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds comment data to the ViewHolder for display.
     * Also handles organizer badge visibility and timestamp formatting.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.tvCommentUser.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());

        if (organizerId != null && organizerId.equals(comment.getUserId())) {
            holder.tvOrganizerBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrganizerBadge.setVisibility(View.GONE);
        }

        if (comment.getTimestamp() != null) {
            String formatted = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                    .format(comment.getTimestamp().toDate());
            holder.tvCommentTime.setText(formatted);
        } else {
            holder.tvCommentTime.setText("");
        }
    }

    /**
     * Returns the total number of comments in the list.
     * Used by RecyclerView to determine list size.
     */
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentText, tvCommentTime, tvOrganizerBadge;

        /**
         * Initializes all view references for a single comment item.
         * Called when a ViewHolder is created.
         */
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvOrganizerBadge = itemView.findViewById(R.id.tvOrganizerBadge);
        }
    }
}