package com.example.lottery.Entrant.Repo;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository class that provides access to the Firestore database
 * for entrant event operations.
 * <p>
 * Acts as a data access layer between the entrant event UI components
 * and Firestore, centralizing database initialization and access.
 * </p>
 */
public class EntrantEventRepository {

    private final FirebaseFirestore db;

    public EntrantEventRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
