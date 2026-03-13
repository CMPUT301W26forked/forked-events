package com.example.lottery.Entrant.Repo;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository class that manages entrant data in Firestore.
 * <p>
 * Acts as a data access layer for entrant-related operations,
 * including creating new entrant profiles if they do not already
 * exist in the "users" collection.
 * </p>
 */
public class EntrantRepository {

    private final FirebaseFirestore db;

    public EntrantRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Creates a new entrant profile in Firestore if one does not already exist.
     * <p>
     * Checks the "users" collection for a document matching the given entrant ID.
     * If no document is found, a new profile is created with default empty values
     * for name, email, and phone, an empty list of registered event IDs, and the
     * role set to "entrant".
     * </p>
     *
     * @param entrantId the Firebase UID of the entrant to check or create
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