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

    public CommentsAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentText, tvCommentTime, tvOrganizerBadge;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvOrganizerBadge = itemView.findViewById(R.id.tvOrganizerBadge);
        }
    }
}