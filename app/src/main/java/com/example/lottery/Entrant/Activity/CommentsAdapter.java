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
import java.util.Map;

/**
 * Adapter for displaying comments in a RecyclerView.
 * Supports nested replies and reaction interactions.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    /**
     * Interface for handling reply button clicks.
     */
    public interface OnReplyClickListener {
        void onReplyClick(Comment comment);
    }

    /**
     * Interface for handling reaction clicks.
     */
    public interface OnReactionClickListener {
        void onReactionClick(Comment comment, String reactionType);
    }

    /** List of comments to display */
    private final List<Comment> commentList;

    /** Organizer ID to show organizer badge */
    private String organizerId;

    /** Listener for reply actions */
    private final OnReplyClickListener replyClickListener;

    /** Listener for reaction actions */
    private final OnReactionClickListener reactionClickListener;

    /** Current logged-in user ID */
    private final String currentUserId;

    /**
     * Constructor for the adapter.
     */
    public CommentsAdapter(List<Comment> commentList,
                           String currentUserId,
                           OnReplyClickListener replyClickListener,
                           OnReactionClickListener reactionClickListener) {
        this.commentList = commentList;
        this.currentUserId = currentUserId;
        this.replyClickListener = replyClickListener;
        this.reactionClickListener = reactionClickListener;
    }

    /**
     * Sets the organizer ID and refreshes the list.
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
        notifyDataSetChanged();
    }

    /**
     * Inflates the comment layout.
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds comment data to each view.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.tvCommentUser.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());

        // Show reply info
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

        // Show organizer badge
        if (organizerId != null && organizerId.equals(comment.getUserId())) {
            holder.tvOrganizerBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrganizerBadge.setVisibility(View.GONE);
        }

        // Format timestamp
        if (comment.getTimestamp() != null) {
            String formatted = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                    .format(comment.getTimestamp().toDate());
            holder.tvCommentTime.setText(formatted);
        } else {
            holder.tvCommentTime.setText("");
        }

        // Apply indentation based on reply depth
        int left = dpToPx(holder.itemView, 12 + (comment.getDepth() * 28));
        holder.commentContentContainer.setPadding(
                left,
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12),
                dpToPx(holder.itemView, 12)
        );

        // Reply click
        holder.btnReply.setOnClickListener(v -> {
            if (replyClickListener != null) {
                replyClickListener.onReplyClick(comment);
            }
        });

        // Reaction counts
        int likeCount = getReactionCount(comment, "like");
        int loveCount = getReactionCount(comment, "love");
        int fireCount = getReactionCount(comment, "fire");

        holder.tvLikeReaction.setText("👍 " + likeCount);
        holder.tvLoveReaction.setText("❤️ " + loveCount);
        holder.tvfireReaction.setText("🔥 " + fireCount);

        // Highlight if user reacted
        holder.tvLikeReaction.setAlpha(hasUserReacted(comment, "like") ? 1.0f : 0.5f);
        holder.tvLoveReaction.setAlpha(hasUserReacted(comment, "love") ? 1.0f : 0.5f);
        holder.tvfireReaction.setAlpha(hasUserReacted(comment, "fire") ? 1.0f : 0.5f);

        // Reaction click handlers
        holder.tvLikeReaction.setOnClickListener(v -> {
            if (reactionClickListener != null) {
                reactionClickListener.onReactionClick(comment, "like");
            }
        });

        holder.tvLoveReaction.setOnClickListener(v -> {
            if (reactionClickListener != null) {
                reactionClickListener.onReactionClick(comment, "love");
            }
        });

        holder.tvfireReaction.setOnClickListener(v -> {
            if (reactionClickListener != null) {
                reactionClickListener.onReactionClick(comment, "fire");
            }
        });
    }

    /**
     * @return number of comments
     */
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /**
     * Converts dp to pixels.
     */
    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Gets count of a specific reaction type.
     */
    private int getReactionCount(Comment comment, String reactionType) {
        if (comment.getReactions() == null) return 0;

        Map<String, List<String>> reactions = comment.getReactions();
        List<String> users = reactions.get(reactionType);

        return users == null ? 0 : users.size();
    }

    /**
     * Checks if current user reacted.
     */
    private boolean hasUserReacted(Comment comment, String reactionType) {
        if (currentUserId == null || comment.getReactions() == null) return false;

        Map<String, List<String>> reactions = comment.getReactions();
        List<String> users = reactions.get(reactionType);

        return users != null && users.contains(currentUserId);
    }

    /**
     * ViewHolder for comment items.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {

        /** Container for indentation */
        View commentContentContainer;

        /** UI elements */
        TextView tvCommentUser;
        TextView tvCommentText;
        TextView tvCommentTime;
        TextView tvOrganizerBadge;
        TextView tvReplyInfo;
        TextView btnReply;

        /** Reaction views */
        TextView tvLikeReaction;
        TextView tvLoveReaction;
        TextView tvfireReaction;

        /**
         * Constructor binds UI elements.
         */
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentContentContainer = itemView.findViewById(R.id.commentContentContainer);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvOrganizerBadge = itemView.findViewById(R.id.tvOrganizerBadge);
            tvReplyInfo = itemView.findViewById(R.id.tvReplyInfo);
            btnReply = itemView.findViewById(R.id.btnReply);

            tvLikeReaction = itemView.findViewById(R.id.tvLikeReaction);
            tvLoveReaction = itemView.findViewById(R.id.tvLoveReaction);
            tvfireReaction = itemView.findViewById(R.id.tvfireReaction);
        }
    }
}