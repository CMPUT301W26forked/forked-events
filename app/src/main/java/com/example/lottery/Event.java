package com.example.lottery;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Event {

    private String eventId;
    private String title;
    private String status;
    private String description;
    private String location;
    private String date;
    private String spots;
    private String waitlistInfo;
    private String joinedCount;

    // Firestore fields
    private String name;
    private long totalSpots;
    private long waitlistCount;
    private Timestamp registrationStart;
    private Timestamp registrationEnd;

    public Event() {
        // Required empty constructor for Firestore
    }

    public Event(String title, String status, String description, String location,
                 String date, String spots, String waitlistInfo, String joinedCount) {
        this.title = title;
        this.status = status;
        this.description = description;
        this.location = location;
        this.date = date;
        this.spots = spots;
        this.waitlistInfo = waitlistInfo;
        this.joinedCount = joinedCount;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Exclude
    public String getTitle() {
        return name != null ? name : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Exclude
    public String getDate() {
        if (registrationStart != null && registrationEnd != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return sdf.format(registrationStart.toDate()) + " - " + sdf.format(registrationEnd.toDate());
        }
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Exclude
    public String getSpots() {
        if (totalSpots > 0) {
            return totalSpots + " spots available";
        }
        return spots;
    }

    public void setSpots(String spots) {
        this.spots = spots;
    }

    @Exclude
    public String getWaitlistInfo() {
        if (registrationEnd != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return "Waitlist Open\ncloses " + sdf.format(registrationEnd.toDate());
        }
        return waitlistInfo;
    }

    public void setWaitlistInfo(String waitlistInfo) {
        this.waitlistInfo = waitlistInfo;
    }

    @Exclude
    public String getJoinedCount() {
        return waitlistCount + " Joined";
    }

    public void setJoinedCount(String joinedCount) {
        this.joinedCount = joinedCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalSpots() {
        return totalSpots;
    }

    public void setTotalSpots(long totalSpots) {
        this.totalSpots = totalSpots;
    }

    public long getWaitlistCount() {
        return waitlistCount;
    }

    public void setWaitlistCount(long waitlistCount) {
        this.waitlistCount = waitlistCount;
    }

    public Timestamp getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(Timestamp registrationStart) {
        this.registrationStart = registrationStart;
    }

    public Timestamp getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(Timestamp registrationEnd) {
        this.registrationEnd = registrationEnd;
    }
}