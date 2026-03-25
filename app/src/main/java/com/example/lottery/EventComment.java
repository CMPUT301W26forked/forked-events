package com.example.lottery;
import com.google.firebase.Timestamp;

/**
 * event comment model
 */
public class EventComment {
    private String commentId;
    private String authorName;
    private String entrantId;
    private String text;
    private Timestamp createdAt;

    public String getCommentId() {
        return commentId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getEntrantId() {
        return entrantId;
    }

    public String getText() {
        return text;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public EventComment() {
    }

    public EventComment(String commentId, String authorName, String entrantId, String text, Timestamp createdAt) {
        this.commentId = commentId;
        this.authorName = authorName;
        this.entrantId = entrantId;
        this.text = text;
        this.createdAt = createdAt;
    }



}


