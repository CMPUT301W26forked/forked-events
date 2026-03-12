package com.example.lottery.Entrant.Model;

import java.util.ArrayList;

public class EntrantProfile {
    private String id;
    private String name;
    private String email;
    private String phone;
    private ArrayList<String> registeredEventIds;

    public EntrantProfile() {
        // Needed for Firestore
    }

    public EntrantProfile(String id, String name, String email, String phone, ArrayList<String> registeredEventIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registeredEventIds = registeredEventIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public ArrayList<String> getRegisteredEventIds() {
        return registeredEventIds;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRegisteredEventIds(ArrayList<String> registeredEventIds) {
        this.registeredEventIds = registeredEventIds;
    }
}
