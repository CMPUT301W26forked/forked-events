package com.example.lottery.Repo;

import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/***
 * Handles direct Firebase operations for waitlist data.
 * Outstanding issues:
 * -joinWaitList does not store any timestamp or geolocation data
 */
public class FSWaitlistRepo implements WaitlistRepo {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentReference ref(String eventId, String userId) {
        return db.collection("events").document(eventId).collection("waitlist").document(userId);
    }

    /***
     * Adds user to waiting list (by creating a document)
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user)
     * @param cb Callback that is invoked on success or failure.
     */
    @Override
    public void joinWaitlist(String eventId, String userId, WaitlistCallback<Void> cb) {
        ref(eventId, userId).set(new HashMap<>()).addOnSuccessListener(v -> cb
                        .onSuccess(null))
                        .addOnFailureListener(cb::onError);
    }

    /***
     * Removes user from waiting list by deleting the document.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */

    @Override
    public void leaveWaitlist(String eventId, String userId, WaitlistCallback<Void> cb) {
        ref(eventId, userId).delete()
                        .addOnSuccessListener(v -> cb.onSuccess(null))
                        .addOnFailureListener(cb::onError);
    }

    /***
     * Checks if the user's waiting list document exists.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    @Override
    public void isOnWaitlist(String eventId, String userId, WaitlistCallback<Boolean> cb) {
        ref(eventId, userId).get()
                       .addOnSuccessListener(doc -> cb.onSuccess(doc.exists()))
                        .addOnFailureListener(cb::onError);
    }

    /***
     * Gets total number of entrants on waiting list.
     * @param eventId ID of event.
     * @param cb Callback that is invoked on success or failure.
     */
    @Override
    public void getWaitlistCount(String eventId, WaitlistCallback<Long> cb) {
        db.collection("events").document(eventId).collection("waitlist")
                        .count()
                        .get(AggregateSource.SERVER)
                        .addOnSuccessListener(snapshot -> cb.onSuccess(snapshot.getCount()))
                        .addOnFailureListener(cb::onError);
    }


    /***
     * Updates user's document to indicate that they want to remain on waiting list.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    @Override
    public void stayInList(String eventId, String userId, WaitlistCallback<Void> cb) {
        ref(eventId, userId).update("stayInList", true)
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

}
