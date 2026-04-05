package com.example.lottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery.EventComment;
import com.example.lottery.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying comments in the admin moderation panel.
 * Uses a card-based layout with a dedicated delete button for each comment.
 */
public class AdminCommentAdapter extends RecyclerView.Adapter<AdminCommentAdapter.ViewHolder> {

    private final List<EventComment> commentList;
    private final OnCommentDeleteListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    /**
     * Interface for handling comment deletion requests.
     */
    public interface OnCommentDeleteListener {
        void onCommentDelete(EventComment comment);
    }

    public AdminCommentAdapter(List<EventComment> commentList, OnCommentDeleteListener listener) {
        this.commentList = commentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventComment comment = commentList.get(position);
        
        holder.tvCommentUser.setText(comment.getAuthorName() != null ? comment.getAuthorName() : "Anonymous");
        holder.tvCommentText.setText(comment.getText());
        
        if (comment.getCreatedAt() != null) {
            holder.tvCommentTime.setText(dateFormat.format(comment.getCreatedAt().toDate()));
        } else {
            holder.tvCommentTime.setText("");
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentDelete(comment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentText, tvCommentTime;
        MaterialButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}