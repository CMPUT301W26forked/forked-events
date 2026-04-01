package com.example.lottery;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * event comment model
 */
public class EventComment {
    private String commentId;
    private String authorName;
    private String entrantId;
    private String text;
    private Timestamp createdAt;

    private String parentCommentId;
    private String replyToEntrantId;
    private String replyToAuthorName;
    private List<String> mentionedUserNames;

    private int depth;

    public EventComment() {
        this.mentionedUserNames = new ArrayList<>();
        this.depth = 0;
    }

    public EventComment(String commentId,
                        String authorName,
                        String entrantId,
                        String text,
                        Timestamp createdAt) {
        this.commentId = commentId;
        this.authorName = authorName;
        this.entrantId = entrantId;
        this.text = text;
        this.createdAt = createdAt;
        this.parentCommentId = null;
        this.replyToEntrantId = null;
        this.replyToAuthorName = null;
        this.mentionedUserNames = new ArrayList<>();
        this.depth = 0;
    }

    public EventComment(String commentId,
                        String authorName,
                        String entrantId,
                        String text,
                        Timestamp createdAt,
                        String parentCommentId,
                        String replyToEntrantId,
                        String replyToAuthorName,
                        List<String> mentionedUserNames) {
        this.commentId = commentId;
        this.authorName = authorName;
        this.entrantId = entrantId;
        this.text = text;
        this.createdAt = createdAt;
        this.parentCommentId = parentCommentId;
        this.replyToEntrantId = replyToEntrantId;
        this.replyToAuthorName = replyToAuthorName;
        this.mentionedUserNames = mentionedUserNames != null ? mentionedUserNames : new ArrayList<>();
        this.depth = 0;
    }

    public String getCommentId() { return commentId; }
    public String getAuthorName() { return authorName; }
    public String getEntrantId() { return entrantId; }
    public String getText() { return text; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getParentCommentId() { return parentCommentId; }
    public String getReplyToEntrantId() { return replyToEntrantId; }
    public String getReplyToAuthorName() { return replyToAuthorName; }
    public List<String> getMentionedUserNames() { return mentionedUserNames; }
    public int getDepth() { return depth; }

    public void setCommentId(String commentId) { this.commentId = commentId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setEntrantId(String entrantId) { this.entrantId = entrantId; }
    public void setText(String text) { this.text = text; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }
    public void setReplyToEntrantId(String replyToEntrantId) { this.replyToEntrantId = replyToEntrantId; }
    public void setReplyToAuthorName(String replyToAuthorName) { this.replyToAuthorName = replyToAuthorName; }
    public void setMentionedUserNames(List<String> mentionedUserNames) {
        this.mentionedUserNames = mentionedUserNames != null ? mentionedUserNames : new ArrayList<>();
    }
    public void setDepth(int depth) { this.depth = depth; }

    public boolean isReply() {
        return parentCommentId != null && !parentCommentId.trim().isEmpty();
    }
}