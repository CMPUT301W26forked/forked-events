package com.example.lottery;

public class Notification {
    private String title;
    private String eventName;
    private String date;
    private String status;

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
}