package com.example.lottery.Entrant.Activity;

import android.util.Log;
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

        Log.d("ENTRANT_DEPTH_CHECK",
                "text=" + comment.getText()
                        + ", depth=" + comment.getDepth()
                        + ", parent=" + comment.getParentCommentId());

        holder.tvCommentUser.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());

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

        int left = dpToPx(holder.itemView, 12 + (comment.getDepth() * 28));
        holder.commentContentContainer.setPadding(
                left,
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12)
        );
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        View commentContentContainer;
        TextView tvCommentUser, tvCommentText, tvCommentTime, tvOrganizerBadge, tvReplyInfo;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentContentContainer = itemView.findViewById(R.id.commentContentContainer);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvOrganizerBadge = itemView.findViewById(R.id.tvOrganizerBadge);
            tvReplyInfo = itemView.findViewById(R.id.tvReplyInfo);
        }
    }
}