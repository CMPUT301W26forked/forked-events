package com.example.lottery.admin;

public class ModerationItem {
    private String eventId;
    private String title;      // event name
    private String imageUrl;   // posterUri (Firebase Storage download URL)

    public ModerationItem(String eventId, String title, String imageUrl) {
        this.eventId = eventId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}
