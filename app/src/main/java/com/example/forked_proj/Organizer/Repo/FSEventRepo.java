package com.example.forked_proj.Organizer.Repo;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FSEventRepo implements EventRepo{
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
}
