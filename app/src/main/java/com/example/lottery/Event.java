package com.example.lottery;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * event model for firestore and app display
 */
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
    private boolean isPrivate;

    // fields matching firestore
    private String name;
    private long totalSpots;
    private long waitListCount = -1;
    private Timestamp registrationStart;
    private Timestamp registrationEnd;


    public Event() {

    }

    /**
     * constructor for event without id
     * @param title event title
     * @param status event status
     * @param description event description
     * @param location event location
     * @param date event date
     * @param spots available spots
     * @param waitlistInfo waitlist information
     * @param joinedCount count of joined entrants
     */
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

    /**
     * constructor for event with id
     * @param eventId unique event identifier
     * @param title event title
     * @param status event status
     * @param description event description
     * @param location event location
     * @param date event date
     * @param spots available spots
     * @param waitlistInfo waitlist information
     * @param joinedCount count of joined entrants
     */
    public Event(String eventId, String title, String status, String description, String location,
                 String date, String spots, String waitlistInfo, String joinedCount) {
        this.eventId = eventId;
        this.title = title;
        this.status = status;
        this.description = description;
        this.location = location;
        this.date = date;
        this.spots = spots;
        this.waitlistInfo = waitlistInfo;
        this.joinedCount = joinedCount;
    }

    /**
     * gets event id
     * @return event id string
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * sets event id
     * @param eventId event id string
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * gets event title prioritizing firestore name
     * @return event title string
     */
    @Exclude
    public String getTitle() {
        return name != null ? name : title;
    }

    /**
     * sets event title
     * @param title event title string
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * gets event status
     * @return event status string
     */
    public String getStatus() {
        return status != null ? status : "";
    }

    /**
     * sets event status
     * @param status event status string
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * gets event description
     * @return event description string
     */
    public String getDescription() {
        return description != null ? description : "";
    }

    /**
     * sets event description
     * @param description event description string
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * gets event location
     * @return event location string
     */
    public String getLocation() {
        return location != null ? location : "";
    }

    /**
     * sets event location
     * @param location event location string
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * gets formatted date range from registration timestamps
     * @return formatted date string
     */
    @Exclude
    public String getDate() {
        if (registrationStart != null && registrationEnd != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return sdf.format(registrationStart.toDate()) + " - " + sdf.format(registrationEnd.toDate());
        }
        return date != null ? date : "";
    }

    /**
     * sets event date string
     * @param date date string
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * gets formatted spot availability
     * @return spots available string
     */
    @Exclude
    public String getSpots() {
        if (totalSpots > 0) {
            return totalSpots + " spots available";
        }
        return spots != null ? spots : "";
    }

    /**
     * sets spots availability string
     * @param spots spots string
     */
    public void setSpots(String spots) {
        this.spots = spots;
    }

    /**
     * gets formatted waitlist closing info
     * @return waitlist info string
     */
    @Exclude
    public String getWaitlistInfo() {
        if (registrationEnd != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return "Waitlist Open\ncloses " + sdf.format(registrationEnd.toDate());
        }
        return waitlistInfo != null ? waitlistInfo : "";
    }

    /**
     * sets waitlist information string
     * @param waitlistInfo waitlist info string
     */
    public void setWaitlistInfo(String waitlistInfo) {
        this.waitlistInfo = waitlistInfo;
    }

    /**
     * gets formatted joined entrant count
     * @return joined count string
     */
    @Exclude
    public String getJoinedCount() {
        if (waitListCount >= 0) {
            return waitListCount + " Joined";
        }
        return joinedCount != null ? joinedCount : "";
    }

    /**
     * sets joined count string
     * @param joinedCount joined count string
     */
    public void setJoinedCount(String joinedCount) {
        this.joinedCount = joinedCount;
    }

    /**
     * gets firestore name field
     * @return event name string
     */
    public String getName() {
        return name != null ? name : "";
    }

    /**
     * sets firestore name field
     * @param name event name string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * gets total spots limit
     * @return total spots count
     */
    public long getTotalSpots() {
        return totalSpots;
    }

    /**
     * sets total spots limit
     * @param totalSpots total spots count
     */
    public void setTotalSpots(long totalSpots) {
        this.totalSpots = totalSpots;
    }

    /**
     * gets current waitlist count
     * @return waitlist count
     */
    public long getWaitListCount() {
        return waitListCount;
    }

    /**
     * sets current waitlist count
     * @param waitListCount waitlist count
     */
    public void setWaitListCount(long waitListCount) {
        this.waitListCount = waitListCount;
    }

    /**
     * gets registration start timestamp
     * @return start timestamp
     */
    public Timestamp getRegistrationStart() {
        return registrationStart;
    }

    /**
     * sets registration start timestamp
     * @param registrationStart start timestamp
     */
    public void setRegistrationStart(Timestamp registrationStart) {
        this.registrationStart = registrationStart;
    }

    /**
     * gets registration end timestamp
     * @return end timestamp
     */
    public Timestamp getRegistrationEnd() {
        return registrationEnd;
    }

    /**
     * sets registration end timestamp
     * @param registrationEnd end timestamp
     */
    public void setRegistrationEnd(Timestamp registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    /**
     * gets whether event is private
     * @return true if private
     */
    @PropertyName("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * sets whether event is private
     * @param isPrivate privacy status
     */
    @PropertyName("isPrivate")
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
