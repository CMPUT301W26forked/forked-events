package com.example.lottery.Entrant.Repo;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * repository for handling entrant data in firestore
 */
public class EntrantRepository {

    private final FirebaseFirestore db;

    /**
     * constructs repository and initializes firestore
     */
    public EntrantRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * gets firestore instance
     * @return firestore database
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * creates new entrant profile if it does not already exist
     * @param entrantId unique identifier for the entrant
     */
    public void createEntrantIfNotExists(String entrantId) {
        db.collection("users")
                .document(entrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> entrant = new HashMap<>();
                        entrant.put("uid", entrantId);
                        entrant.put("name", "");
                        entrant.put("email", "");
                        entrant.put("phone", "");
                        entrant.put("registeredEventIds", new ArrayList<String>());
                        entrant.put("role", "entrant");
                        entrant.put("isGuest", false);

                        db.collection("users").document(entrantId).set(entrant);
                    }
                });
    }
}
