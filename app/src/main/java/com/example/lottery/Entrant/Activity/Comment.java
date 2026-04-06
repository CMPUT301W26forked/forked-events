package com.example.lottery.Entrant.Activity;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comment {
    private String commentId;
    private String userId;
    private String userName;
    private String text;
    private Timestamp timestamp;

    private String parentCommentId;
    private String replyToEntrantId;
    private String replyToAuthorName;
    private List<String> mentionedUserNames;

    private int depth;

    private Map<String, List<String>> reactions;

    public Comment() {
        this.mentionedUserNames = new ArrayList<>();
        this.depth = 0;
        this.reactions = new HashMap<>();
    }

    public Comment(String userId, String userName, String text, Timestamp timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
        this.mentionedUserNames = new ArrayList<>();
        this.depth = 0;
        this.reactions = new HashMap<>();
    }

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

    public String getCommentId() {
        return commentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public String getReplyToEntrantId() {
        return replyToEntrantId;
    }

    public String getReplyToAuthorName() {
        return replyToAuthorName;
    }

    public List<String> getMentionedUserNames() {
        return mentionedUserNames;
    }

    public int getDepth() {
        return depth;
    }

    public Map<String, List<String>> getReactions() {
        return reactions;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public void setReplyToEntrantId(String replyToEntrantId) {
        this.replyToEntrantId = replyToEntrantId;
    }

    public void setReplyToAuthorName(String replyToAuthorName) {
        this.replyToAuthorName = replyToAuthorName;
    }

    public void setMentionedUserNames(List<String> mentionedUserNames) {
        this.mentionedUserNames = mentionedUserNames != null ? mentionedUserNames : new ArrayList<>();
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setReactions(Map<String, List<String>> reactions) {
        this.reactions = reactions != null ? reactions : new HashMap<>();
    }

    public boolean isReply() {
        return parentCommentId != null && !parentCommentId.trim().isEmpty();
    }
}