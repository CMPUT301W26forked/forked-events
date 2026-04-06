package com.example.lottery;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying event comments in a RecyclerView.
 * Handles nested comments (replies) and delete actions.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    /**
     * Interface for handling delete button clicks on comments.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when a comment delete button is clicked.
         * @param comment the comment to delete
         */
        void onDeleteClick(EventComment comment);
    }

    /** List of comments to display */
    private final List<EventComment> commentList;

    /** Listener for delete actions */
    private final OnDeleteClickListener deleteClickListener;

    /**
     * Constructor for CommentsAdapter.
     * @param commentList list of comments
     * @param deleteClickListener delete button listener
     */
    public CommentsAdapter(List<EventComment> commentList, OnDeleteClickListener deleteClickListener) {
        this.commentList = commentList;
        this.deleteClickListener = deleteClickListener;
    }

    /**
     * Creates a new ViewHolder for a comment item.
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds comment data to the ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        EventComment comment = commentList.get(position);

        Log.d("COMMENTS_DEBUG",
                "BIND: text=" + comment.getText()
                        + ", depth=" + comment.getDepth()
                        + ", parent=" + comment.getParentCommentId());

        String author = comment.getAuthorName();
        String text = comment.getText();

        // Set author name or default value
        holder.tvAuthorName.setText(
                author == null || author.trim().isEmpty() ? "Unknown User" : author
        );

        // Set comment text or default value
        holder.tvCommentText.setText(
                text == null || text.trim().isEmpty() ? "(empty comment)" : text
        );

        // Format and display timestamp
        holder.tvCommentDate.setText(formatTimeStamp(comment.getCreatedAt()));

        // Show reply information if comment is a reply
        if (comment.isReply()) {
            holder.tvReplyInfo.setVisibility(View.VISIBLE);

            String replyToName = comment.getReplyToAuthorName();
            if (replyToName == null || replyToName.trim().isEmpty()) {
                holder.tvReplyInfo.setText("Reply");
            } else {
                holder.tvReplyInfo.setText("Replying to @" + replyToName);
            }
        } else {
            holder.tvReplyInfo.setVisibility(View.GONE);
        }

        // Set indentation based on comment depth (for nested replies)
        int left = dpToPx(holder.itemView, 12 + (comment.getDepth() * 28));
        holder.commentContentContainer.setPadding(
                left,
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12)
        );

        // Handle delete button click
        holder.btnDeleteComment.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(comment);
            }
        });

        android.util.Log.d("DEPTH_CHECK",
                "text=" + comment.getText() + " depth=" + comment.getDepth());
    }

    /**
     * ViewHolder class for comment items.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        View commentContentContainer;
        TextView tvAuthorName;
        TextView tvReplyInfo;
        TextView tvCommentText;
        TextView tvCommentDate;
        MaterialButton btnDeleteComment;

        /**
         * Initializes all UI components for a comment item.
         */
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentContentContainer = itemView.findViewById(R.id.commentContentContainer);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvReplyInfo = itemView.findViewById(R.id.tvReplyInfo);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);
        }
    }

    /**
     * Formats a Firestore Timestamp into a readable string.
     * @param timestamp Firestore timestamp
     * @return formatted date string
     */
    private String formatTimeStamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Returns total number of comments.
     */
    @Override
    public int getItemCount() {
        return commentList.size();
    }
}