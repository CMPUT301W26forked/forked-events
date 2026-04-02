package com.example.lottery.admin;

/**
 * Represents a user profile item displayed in the admin moderation interface.
 * This class holds all relevant user information for admin review.
 */
public class AdminProfileItem {
    private final String uid;
    private final String name;
    private final String email;
    private final String phone;
    private final String role;
    private final String profilePictureUri;

    /**
     * Creates a new AdminProfileItem with the specified user details.
     *
     * @param uid               Unique identifier for the user
     * @param name              Display name of the user
     * @param email             Email address of the user
     * @param phone             Phone number of the user
     * @param role              Role assigned to the user
     * @param profilePictureUri URI string pointing to the user's profile picture
     */
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