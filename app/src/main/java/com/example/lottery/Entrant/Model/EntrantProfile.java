package com.example.lottery.Entrant.Model;

import java.util.ArrayList;

/**
 * Model class representing an entrant's profile.
 * <p>
 * Stores personal information and a list of event IDs the entrant
 * has registered for. The no-argument constructor is required for
 * Firestore deserialization.
 * </p>
 */
public class EntrantProfile {
    private String id;
    private String name;
    private String email;
    private String phone;
    private ArrayList<String> registeredEventIds;

    public EntrantProfile() {
        // Needed for Firestore
    }

    /**
     * Constructs a fully populated EntrantProfile.
     *
     * @param id                 the unique Firebase UID of the entrant
     * @param name               the display name of the entrant
     * @param email              the email address of the entrant
     * @param phone              the phone number of the entrant
     * @param registeredEventIds the list of event IDs the entrant has registered for
     */
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

    /**
     * Sets the list of event IDs the entrant has registered for.
     *
     * @param registeredEventIds the list of event IDs to assign
     */
    public void setRegisteredEventIds(ArrayList<String> registeredEventIds) {
        this.registeredEventIds = registeredEventIds;
    }
}
