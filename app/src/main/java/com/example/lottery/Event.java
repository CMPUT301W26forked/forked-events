package com.example.lottery;

public class Event {
    private String title;
    private String status;
    private String description;
    private String location;
    private String date;
    private String spots;
    private String waitlistInfo;
    private String joinedCount;

    public Event(String title, String status, String description, String location, String date, String spots, String waitlistInfo, String joinedCount) {
        this.title = title;
        this.status = status;
        this.description = description;
        this.location = location;
        this.date = date;
        this.spots = spots;
        this.waitlistInfo = waitlistInfo;
        this.joinedCount = joinedCount;
    }

    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getSpots() { return spots; }
    public String getWaitlistInfo() { return waitlistInfo; }
    public String getJoinedCount() { return joinedCount; }
}