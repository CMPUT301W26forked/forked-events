package com.example.lottery.Entrant.Service;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantService {

    private final FirebaseFirestore db;

    public EntrantService() {
        db = FirebaseFirestore.getInstance();
    }

    public void signUpForEvent(String entrantId, String eventId) {
        db.collection("users")
                .document(entrantId)
                .update("registeredEventIds", FieldValue.arrayUnion(eventId));
    }

    public void acceptInvitation(String eventId, String entrantId) {
        db.collection("events")
                .document(eventId)
                .update("confirmedEntrantIds", FieldValue.arrayUnion(entrantId));
    }

    public void declineInvitation(String eventId, String entrantId) {
        db.collection("events")
                .document(eventId)
                .update("invitedEntrantIds", FieldValue.arrayRemove(entrantId));
    }
}
