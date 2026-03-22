package com.example.lottery;

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
 * adapter for event comments list
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    public interface OnDeleteClickListener {
        void onDeleteClick(EventComment comment);
    }

    private final List<EventComment> commentList;
    private final OnDeleteClickListener deleteClickListener;

    public CommentsAdapter(List<EventComment> commentList, OnDeleteClickListener deleteClickListener) {
        this.commentList = commentList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_comment, parent,false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        EventComment comment = commentList.get(position);

        holder.tvAuthorName.setText(comment.getAuthorName().isEmpty() ? "Unknown User" : comment.getAuthorName());
        holder.tvCommentText.setText(comment.getText().isEmpty() ? "(empty comment)" : comment.getText());
        holder.tvCommentDate.setText(formatTimeStamp(comment.getCreatedAt()));

        holder.btnDeleteComment.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(comment);
            }
        });
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthorName;
        TextView tvCommentText;
        TextView tvCommentDate;
        MaterialButton btnDeleteComment;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);
        }
    }

    private String formatTimeStamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
