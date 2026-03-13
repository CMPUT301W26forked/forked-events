package com.example.lottery;

import com.google.firebase.Timestamp;

/**
 * notification model
 */
public class Notification {
    private String title;
    private String eventName;
    private String eventId;
    private String date;
    private String status;
    private String message;
    private String type;
    private Timestamp createdAt;


    public Notification() {
    }

    /**
     * full constructor for notification
     * @param title notification title
     * @param eventName event name
     * @param eventId event id
     * @param date date string
     * @param status current status
     */
    public Notification(String title, String eventName, String eventId, String date, String status) {
        this.title = title;
        this.eventName = eventName;
        this.eventId = eventId;
        this.date = date;
        this.status = status;
    }

    /**
     * gets notification title
     * @return notification title
     */
    public String getTitle() { return title; }

    /**
     * gets event name
     * @return event name
     */
    public String getEventName() { return eventName; }

    /**
     * gets event id
     * @return event id
     */
    public String getEventId() { return eventId; }

    /**
     * gets date string
     * @return date string
     */
    public String getDate() { return date; }

    /**
     * gets current status
     * @return current status
     */
    public String getStatus() { return status; }

    /**
     * gets detailed message
     * @return notification message
     */
    public String getMessage() {return message;}

    /**
     * gets notification type
     * @return notification type
     */
    public String getType() {return type;}

    /**
     * gets creation timestamp
     * @return creation timestamp
     */
    public Timestamp getCreatedAt() {return createdAt;}
}
