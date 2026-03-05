package com.example.forked_proj.model;

import java.time.Instant;

public class Event {
    private final String id;
    private String name;

    private Instant regStart;
    private Instant regENd;
    private String posterURL;

    public Event(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }
}


