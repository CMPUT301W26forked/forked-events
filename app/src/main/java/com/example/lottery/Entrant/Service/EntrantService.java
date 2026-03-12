package com.example.lottery.Entrant.Service;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EntrantService {

    private final FirebaseFirestore db;

    public EntrantService() {
        db = FirebaseFirestore.getInstance();
    }

    public void signUpForEvent(String entrantId, String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("registeredEventIds", FieldValue.arrayUnion(eventId));

        db.collection("users")
                .document(entrantId)
                .set(data, SetOptions.merge());
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