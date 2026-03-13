package com.example.lottery;

import com.google.firebase.Timestamp;

public class Notification {
    private String title;
    private String eventName;
    private String date;
    private String status;
    private String message;
    private String type;
    private Timestamp createdAt;

    public Notification() {
        // for FS
    }

    public Notification(String title, String eventName, String date, String status) {
        this.title = title;
        this.eventName = eventName;
        this.date = date;
        this.status = status;

    }

    public String getTitle() { return title; }
    public String getEventName() { return eventName; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getMessage() {return message;}
    public String getType() {return type;}
    public Timestamp getCreatedAt() {return createdAt;}
}