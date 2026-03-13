package com.example.lottery.Entrant.Service;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class that handles entrant actions related to lottery events.
 * <p>
 * Provides methods for signing up for events, accepting invitations,
 * and declining invitations. All operations interact directly with
 * Firestore and are performed asynchronously.
 * </p>
 */
public class EntrantService {

    private final FirebaseFirestore db;

    public EntrantService() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Registers an entrant for an event by adding the event ID to their
     * list of registered events in Firestore.
     * <p>
     * Uses Firestore's arrayUnion to avoid duplicate entries, and merges
     * with the existing document to avoid overwriting other fields.
     * </p>
     *
     * @param entrantId the Firebase UID of the entrant signing up
     * @param eventId   the ID of the event to register for
     */
    public void signUpForEvent(String entrantId, String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("registeredEventIds", FieldValue.arrayUnion(eventId));

        db.collection("users")
                .document(entrantId)
                .set(data, SetOptions.merge());
    }
    /**
     * Accepts a lottery invitation by adding the entrant to the event's
     * list of confirmed entrants in Firestore.
     * <p>
     * Uses Firestore's arrayUnion to avoid duplicate entries.
     * </p>
     *
     * @param eventId   the ID of the event the invitation is for
     * @param entrantId the Firebase UID of the entrant accepting the invitation
     */
    public void acceptInvitation(String eventId, String entrantId) {
        db.collection("events")
                .document(eventId)
                .update("confirmedEntrantIds", FieldValue.arrayUnion(entrantId));
    }

    /**
     * Declines a lottery invitation by removing the entrant from the event's
     * list of invited entrants in Firestore.
     * <p>
     * Uses Firestore's arrayRemove to remove the entrant ID.
     * </p>
     *
     * @param eventId   the ID of the event the invitation is for
     * @param entrantId the Firebase UID of the entrant declining the invitation
     */
    public void declineInvitation(String eventId, String entrantId) {
        db.collection("events")
                .document(eventId)
                .update("invitedEntrantIds", FieldValue.arrayRemove(entrantId));
    }
}