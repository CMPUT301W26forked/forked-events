package com.example.lottery.Entrant.Activity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a comment made by a user on an event.
 * Supports nested replies, mentions, and reactions.
 */
public class Comment {

    /** Unique ID of the comment */
    private String commentId;

    /** ID of the user who posted the comment */
    private String userId;

    /** Display name of the user */
    private String userName;

    /** Text content of the comment */
    private String text;

    /** Time when the comment was created */
    private Timestamp timestamp;

    /** ID of the parent comment (null if top-level comment) */
    private String parentCommentId;

    /** ID of the user being replied to */
    private String replyToEntrantId;

    /** Name of the user being replied to */
    private String replyToAuthorName;

    /** List of usernames mentioned in the comment */
    private List<String> mentionedUserNames;

    /** Depth level for nested replies (0 = top-level) */
    private int depth;

    /**
     * Stores reactions on the comment.
     * Key = reaction type (e.g., "like", "love")
     * Value = list of user IDs who reacted
     */
    private Map<String, List<String>> reactions;

    /**
     * Default constructor required for Firestore.
     */
    public Comment() {
        this.mentionedUserNames = new ArrayList<>();
        this.depth = 0;
        this.reactions = new HashMap<>();
    }

    /**
     * Constructor for creating a new top-level comment.
     */
    public Comment(String userId, String userName, String text, Timestamp timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
        this.mentionedUserNames = new ArrayList<>();
        this.depth = 0;
        this.reactions = new HashMap<>();
    }

    /**
     * Full constructor for comments with replies and mentions.
     */
    public Comment(String commentId,
                   String userId,
                   String userName,
                   String text,
                   Timestamp timestamp,
                   String parentCommentId,
                   String replyToEntrantId,
                   String replyToAuthorName,
                   List<String> mentionedUserNames) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
        this.parentCommentId = parentCommentId;
        this.replyToEntrantId = replyToEntrantId;
        this.replyToAuthorName = replyToAuthorName;
        this.mentionedUserNames = mentionedUserNames != null ? mentionedUserNames : new ArrayList<>();
        this.depth = 0;
        this.reactions = new HashMap<>();
    }

    /** @return comment ID */
    public String getCommentId() {
        return commentId;
    }

    /** @return user ID */
    public String getUserId() {
        return userId;
    }

    /** @return username */
    public String getUserName() {
        return userName;
    }

    /** @return comment text */
    public String getText() {
        return text;
    }

    /** @return timestamp of comment */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /** @return parent comment ID */
    public String getParentCommentId() {
        return parentCommentId;
    }

    /** @return replied user ID */
    public String getReplyToEntrantId() {
        return replyToEntrantId;
    }

    /** @return replied user name */
    public String getReplyToAuthorName() {
        return replyToAuthorName;
    }

    /** @return list of mentioned usernames */
    public List<String> getMentionedUserNames() {
        return mentionedUserNames;
    }

    /** @return nesting depth */
    public int getDepth() {
        return depth;
    }

    /** @return reactions map */
    public Map<String, List<String>> getReactions() {
        return reactions;
    }

    /** Sets comment ID */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /** Sets user ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** Sets username */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** Sets comment text */
    public void setText(String text) {
        this.text = text;
    }

    /** Sets timestamp */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /** Sets parent comment ID */
    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    /** Sets replied user ID */
    public void setReplyToEntrantId(String replyToEntrantId) {
        this.replyToEntrantId = replyToEntrantId;
    }

    /** Sets replied user name */
    public void setReplyToAuthorName(String replyToAuthorName) {
        this.replyToAuthorName = replyToAuthorName;
    }

    /** Sets mentioned usernames */
    public void setMentionedUserNames(List<String> mentionedUserNames) {
        this.mentionedUserNames = mentionedUserNames != null ? mentionedUserNames : new ArrayList<>();
    }

    /** Sets nesting depth */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /** Sets reactions map */
    public void setReactions(Map<String, List<String>> reactions) {
        this.reactions = reactions != null ? reactions : new HashMap<>();
    }

    /**
     * Checks if the comment is a reply.
     * @return true if it has a parent comment
     */
    public boolean isReply() {
        return parentCommentId != null && !parentCommentId.trim().isEmpty();
    }
}