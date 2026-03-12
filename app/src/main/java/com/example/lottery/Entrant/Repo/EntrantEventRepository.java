package com.example.lottery.Entrant.Repo;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantEventRepository {

    private final FirebaseFirestore db;

    public EntrantEventRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
