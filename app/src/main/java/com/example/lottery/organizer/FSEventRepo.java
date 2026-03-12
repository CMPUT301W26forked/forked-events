package com.example.lottery.organizer;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
public class FSEventRepo implements EventRepo{
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference ref(String eventId) {
        return db.collection("events").document(eventId);
    }

    /**
     * reads single event document by eventId
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
     * stores registration timestamps in event document
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

        // prevent overwriting
        ref(eventId).set(map, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    /**
     * stores poster URI in event document
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

    /**
     * retrieves IDs of entrants with status WAITING for event
     * @param eventId
     * @param cb
     */
    @Override
    public void getWaitingEntrantIds(String eventId, RepoCallback<List<String>> cb) {
        db.collection("events")
                .document(eventId)
                .collection("entrants")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> ids = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        if ("WAITING".equals(doc.getString("status"))) {
                            ids.add(doc.getId());
                        }
                    }
                    cb.onSuccess(ids);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     *marks selected entrats' status as SELECTED
     * @param eventId
     * @param entrantId
     * @param cb
     */
    @Override
    public void markEntrantSelected(String eventId, String entrantId, RepoCallback<Void> cb) {
        db.collection("events")
                .document(eventId)
                .collection("entrants")
                .document(entrantId)
                .update("status", "SELECTED")
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    /**
     * create notifications for entrants
     * @param eventId
     * @param entrantId
     * @param message
     * @param cb
     */
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
                    db.collection("entrants")
                            .document(entrantId)
                            .collection("notification")
                            .add(notification)
                            .addOnSuccessListener(subDoc -> cb.onSuccess(null))
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }
}
