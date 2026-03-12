package com.example.lottery.organizer;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

/**
 * repo interface
 */
public interface EventRepo {
    void getEvent(String eventId, RepoCallback<DocumentSnapshot> cb);
    void setRegStartPeriod(String eventId, Timestamp start, Timestamp end, RepoCallback<Void> cb);
    void setPosterUrl(String eventId, String posterUrl, RepoCallback<Void> cb);
    void getWaitingEntrantIds(String eventId, RepoCallback<List<String>> cb);
    void markEntrantSelected(String eventId, String entrantId, RepoCallback<Void> cb);
    void createNotification(String eventId, String entrantId, String message, RepoCallback<Void> cb);
}
