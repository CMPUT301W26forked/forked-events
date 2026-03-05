package com.example.forked_proj.Organizer.Repo;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;


public interface EventRepo {
    void getEvent(String eventId, RepoCallback<DocumentSnapshot> cb);
    void setRegStartPeriod(String eventId, Timestamp start, Timestamp end, RepoCallback<Void> cb);
    void setPosterUrl(String eventId, String posterUrl, RepoCallback<Void> cb);
}
