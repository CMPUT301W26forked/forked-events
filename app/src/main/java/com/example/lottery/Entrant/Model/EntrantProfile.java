package com.example.lottery.Entrant.Model;

import java.util.ArrayList;

/**
 * entrant profile model
 */
public class EntrantProfile {
    private String id;
    private String name;
    private String email;
    private String phone;
    private ArrayList<String> registeredEventIds;


    public EntrantProfile() {

    }

    /**
     * full constructor for entrant profile
     * @param id entrant id
     * @param name entrant name
     * @param email entrant email
     * @param phone entrant phone number
     * @param registeredEventIds list of registered event ids
     */
    public EntrantProfile(String id, String name, String email, String phone, ArrayList<String> registeredEventIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registeredEventIds = registeredEventIds;
    }

    /**
     * gets entrant id
     * @return entrant id
     */
    public String getId() {
        return id;
    }

    /**
     * gets entrant name
     * @return entrant name
     */
    public String getName() {
        return name;
    }

    /**
     * gets entrant email
     * @return entrant email
     */
    public String getEmail() {
        return email;
    }

    /**
     * gets entrant phone number
     * @return entrant phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * gets list of registered event ids
     * @return list of event ids
     */
    public ArrayList<String> getRegisteredEventIds() {
        return registeredEventIds;
    }

    /**
     * sets entrant id
     * @param id entrant id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * sets entrant name
     * @param name entrant name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * sets entrant email
     * @param email entrant email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * sets entrant phone number
     * @param phone entrant phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * sets list of registered event ids
     * @param registeredEventIds list of event ids
     */
    public void setRegisteredEventIds(ArrayList<String> registeredEventIds) {
        this.registeredEventIds = registeredEventIds;
    }
}
