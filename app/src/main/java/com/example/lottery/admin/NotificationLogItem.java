package com.example.lottery.admin;

import com.google.firebase.Timestamp;

/**
 * model for organizer notification logs for admin view
 */
public class NotificationLogItem {
    private String eventName;
    private String eventId;
    private String type;
    private String message;
    private Timestamp createdAt;
    private int recipientCount;
    private String audience;

    public NotificationLogItem(String eventName, String eventId, String type, String message, Timestamp createdAt, int recipientCount, String audience) {
        this.eventName = eventName;
        this.eventId = eventId;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
        this.recipientCount = recipientCount;
        this.audience = audience;
    }


    public String getEventName() {
        return eventName;
    }

    public String getEventId() {
        return eventId;
    }


    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public String getAudience() {
        return audience;
    }
}
