package com.example.lottery.organizer;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FS implementation of EventRepo
 * handles event interactions with FS
 */
public class FSEventRepo implements EventRepo {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentReference ref(String eventId) {
        return db.collection("events").document(eventId);
    }

    @Override
    public void getEvent(String eventId, RepoCallback<DocumentSnapshot> cb) {
        ref(eventId).get()
                .addOnSuccessListener(cb::onSuccess)
                .addOnFailureListener(cb::onError);
    }

    @Override
    public void setRegStartPeriod(String eventId, Timestamp start, Timestamp end, RepoCallback<Void> cb) {
        Map<String, Object> map = new HashMap<>();
        map.put("registrationStart", start);
        map.put("registrationEnd", end);

        ref(eventId).set(map, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    @Override
    public void setPosterUrl(String eventId, String posterUri, RepoCallback<Void> cb) {
        Map<String, Object> map = new HashMap<>();
        map.put("posterUri", posterUri);

        ref(eventId).set(map, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    @Override
    public void saveEvent(String eventId, Map<String, Object> eventData, RepoCallback<Void> cb) {
        ref(eventId).set(eventData, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    @Override
    public void linkEventToOrganizer(String userId, String eventId, String eventName, RepoCallback<Void> cb) {
        // No longer storing event links in the user document
        cb.onSuccess(null);
    }

    @Override
    public void getWaitingEntrantIds(String eventId, RepoCallback<List<String>> cb) {
        // Get IDs directly from the waitlistedEntrantIds array to ensure consistency with manual testing
        ref(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> ids = (List<String>) doc.get("waitlistedEntrantIds");
                cb.onSuccess(ids != null ? ids : new ArrayList<>());
            } else {
                cb.onSuccess(new ArrayList<>());
            }
        }).addOnFailureListener(cb::onError);
    }

    @Override
    public void markEntrantSelected(String eventId, String entrantId, RepoCallback<Void> cb) {
        // 1. Update status in subcollection to SELECTED
        db.collection("events")
                .document(eventId)
                .collection("entrants")
                .document(entrantId)
                .update("status", "SELECTED")
                .addOnSuccessListener(v -> {
                    // 2. Transition ID from waitlisted array to pendingEntrantIds array
                    ref(eventId).update(
                            "waitlistedEntrantIds", FieldValue.arrayRemove(entrantId),
                            "pendingEntrantIds", FieldValue.arrayUnion(entrantId),
                            "waitlistCount", FieldValue.increment(-1)
                    ).addOnSuccessListener(unused -> cb.onSuccess(null))
                     .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(e -> {
                    // Even if subcollection update fails, try updating the arrays
                    ref(eventId).update(
                            "waitlistedEntrantIds", FieldValue.arrayRemove(entrantId),
                            "pendingEntrantIds", FieldValue.arrayUnion(entrantId),
                            "waitlistCount", FieldValue.increment(-1)
                    ).addOnSuccessListener(unused -> cb.onSuccess(null))
                     .addOnFailureListener(cb::onError);
                });
    }

    @Override
    public void createNotification(String eventId, String entrantId, String message, RepoCallback<Void> cb) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("entrantId", entrantId);
        notification.put("message", message);
        notification.put("type", "SELECTED");
        notification.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(doc -> {
                    db.collection("users")
                            .document(entrantId)
                            .collection("notification")
                            .add(notification)
                            .addOnSuccessListener(subDoc -> cb.onSuccess(null))
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }
}
