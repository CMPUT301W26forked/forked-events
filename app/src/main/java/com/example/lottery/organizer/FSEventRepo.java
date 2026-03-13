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

    /**
     * get reference from FS
     * @param eventId
     * @return
     */
    private DocumentReference ref(String eventId) {
        return db.collection("events").document(eventId);
    }

    /**
     * get event from FS
     * @param eventId
     * @param cb
     */
    @Override
    public void getEvent(String eventId, RepoCallback<DocumentSnapshot> cb) {
        ref(eventId).get()
                .addOnSuccessListener(cb::onSuccess)
                .addOnFailureListener(cb::onError);
    }

    /**
     * set RegistrationPeriod to FS
     * @param eventId
     * @param start
     * @param end
     * @param cb
     */
    @Override
    public void setRegStartPeriod(String eventId, Timestamp start, Timestamp end, RepoCallback<Void> cb) {
        Map<String, Object> map = new HashMap<>();
        map.put("registrationStart", start);
        map.put("registrationEnd", end);

        ref(eventId).set(map, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    /**
     * set poster url to FS
     * @param eventId
     * @param posterUri
     * @param cb
     */
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

    /**
     * get waiting entrants from waitlist on FS
     * @param eventId
     * @param cb
     */
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

    /**
     * remove selected entrant from waitlist to pending list
     * @param eventId
     * @param entrantId
     * @param cb
     */
    @Override
    public void markEntrantSelected(String eventId, String entrantId, RepoCallback<Void> cb) {
        // Transition ID from waitlisted array to pendingEntrantIds array
        ref(eventId).update(
                "waitlistedEntrantIds", FieldValue.arrayRemove(entrantId),
                "pendingEntrantIds", FieldValue.arrayUnion(entrantId),
                "waitlistCount", FieldValue.increment(-1)
        ).addOnSuccessListener(unused -> cb.onSuccess(null))
         .addOnFailureListener(cb::onError);
    }

    /**
     * create notification to selected/specified entrant
     * @param eventId
     * @param entrantId
     * @param eventName
     * @param message
     * @param cb
     */
    @Override
    public void createNotification(String eventId, String entrantId, String eventName, String message, RepoCallback<Void> cb) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("eventId", eventId);
        notification.put("eventName", eventName);
        notification.put("entrantId", entrantId);
        notification.put("message", message);
        notification.put("type", "SELECTED");
        notification.put("createdAt", com.google.firebase.Timestamp.now());
        notification.put("isRead", false);

        db.collection("users")
                    .document(entrantId)
                    .collection("notification")
                    .add(notification)
                    .addOnSuccessListener(subDoc -> cb.onSuccess(null))
                    .addOnFailureListener(cb::onError);
    }

    /**
     * get all pending entrant ids
     * @param eventId
     * @param cb
     */
    public void getPendingEntrantIds(String eventId, RepoCallback<List<String>> cb) {
        ref(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> ids = (List<String>) doc.get("pendingEntrantIds");
                        cb.onSuccess(ids != null ? ids : new ArrayList<>());
                    } else {
                        cb.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(cb::onError);

    }

    /**
     * send message to pending.
     * @param eventId
     * @param userIds
     * @param message
     * @param cb
     */
    public void sendMessageToPending(String eventId, List<String> userIds, String message, RepoCallback<Void> cb) {
        if (userIds == null || userIds.isEmpty()) {
            cb.onError(new IllegalArgumentException("No selected entrants"));
            return;
        }

        final int total = userIds.size();
        final int[] done = {0};
        final boolean[] failed = {false};

        for (String id : userIds) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("eventId", eventId);
            notification.put("recipientUid", id);
            notification.put("message", message);
            notification.put("type", "MESSAGE");
            notification.put("createdAt", Timestamp.now());
            notification.put("isRead", false);
            db.collection("users")
                    .document(id)
                    .collection("notification")
                    .add(notification)
                    .addOnSuccessListener(ref -> {
                        if (failed[0]) return;
                        done[0]++;
                        if (done[0] == total) {
                            cb.onSuccess(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (failed[0]) return;
                        failed[0] = true;
                        cb.onError(e);
                    });

        }
    }

}
