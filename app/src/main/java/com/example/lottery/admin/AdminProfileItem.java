package com.example.lottery.admin;

public class AdminProfileItem {
    private final String uid;
    private final String name;
    private final String email;
    private final String phone;
    private final String role;
    private final String profilePictureUri;

    public AdminProfileItem(String uid, String name, String email, String phone, String role, String profilePictureUri) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.profilePictureUri = profilePictureUri;
    }

    public String getUid() {
        return uid;
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

    public String getRole() {
        return role;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }
}