package com.example.lottery.Entrant.Repo;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EntrantRepository {

    private final FirebaseFirestore db;

    public EntrantRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void createEntrantIfNotExists(String entrantId) {
        db.collection("entrants")
                .document(entrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> entrant = new HashMap<>();
                        entrant.put("id", entrantId);
                        entrant.put("name", "");
                        entrant.put("email", "");
                        entrant.put("phone", "");
                        entrant.put("registeredEventIds", new ArrayList<String>());

                        db.collection("entrants").document(entrantId).set(entrant);
                    }
                });
    }
}
