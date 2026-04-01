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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        EventComment comment = commentList.get(position);

        Log.d("COMMENTS_DEBUG",
                "BIND: text=" + comment.getText()
                        + ", depth=" + comment.getDepth()
                        + ", parent=" + comment.getParentCommentId());

        String author = comment.getAuthorName();
        String text = comment.getText();

        holder.tvAuthorName.setText(
                author == null || author.trim().isEmpty() ? "Unknown User" : author
        );

        holder.tvCommentText.setText(
                text == null || text.trim().isEmpty() ? "(empty comment)" : text
        );

        holder.tvCommentDate.setText(formatTimeStamp(comment.getCreatedAt()));

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

        int left = dpToPx(holder.itemView, 12 + (comment.getDepth() * 28));
        holder.commentContentContainer.setPadding(
                left,
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12)
        );

        holder.btnDeleteComment.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(comment);
            }
        });

        android.util.Log.d("DEPTH_CHECK",
                "text=" + comment.getText() + " depth=" + comment.getDepth());
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        View commentContentContainer;
        TextView tvAuthorName;
        TextView tvReplyInfo;
        TextView tvCommentText;
        TextView tvCommentDate;
        MaterialButton btnDeleteComment;

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

    private String formatTimeStamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}