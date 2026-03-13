package com.example.lottery.Entrant.Service;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * service for handling entrant-related firestore operations
 */
public class EntrantService {

    private final FirebaseFirestore db;

    /**
     * constructs a new entrant service and initializes firestore
     */
    public EntrantService() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * signs up an entrant for a specific event
     * @param entrantId unique identifier for the entrant
     * @param eventId unique identifier for the event
     */
    public void signUpForEvent(String entrantId, String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("registeredEventIds", FieldValue.arrayUnion(eventId));

        db.collection("users")
                .document(entrantId)
                .set(data, SetOptions.merge());
    }

    /**
     * accepts an event invitation for an entrant
     * @param eventId unique identifier for the event
     * @param entrantId unique identifier for the entrant
     */
    public void acceptInvitation(String eventId, String entrantId) {
        db.collection("events")
                .document(eventId)
                .update("confirmedEntrantIds", FieldValue.arrayUnion(entrantId));
    }

    /**
     * declines an event invitation for an entrant
     * @param eventId unique identifier for the event
     * @param entrantId unique identifier for the entrant
     */
    public void declineInvitation(String eventId, String entrantId) {
        db.collection("events")
                .document(eventId)
                .update("invitedEntrantIds", FieldValue.arrayRemove(entrantId));
    }
}
