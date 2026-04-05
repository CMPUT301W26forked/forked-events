package com.example.lottery.organizer;

import com.example.lottery.EventComment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
public class FSEventRepo implements EventRepo {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * get reference from FS
     * @param eventId
     * @return
     */
    private DocumentReference ref(String eventId) {
        return db.collection("events").document(eventId);
    }

    /**
     * get event from FS
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
     * set RegistrationPeriod to FS
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

        ref(eventId).set(map, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    /**
     * set poster url to FS
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

    @Override
    public void removePosterUrl(String eventId, RepoCallback<Void> cb) {
        ref(eventId).update("posterUri", FieldValue.delete())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    @Override
    public void saveEvent(String eventId, Map<String, Object> eventData, RepoCallback<Void> cb) {
        ref(eventId).set(eventData, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    @Override
    public void linkEventToOrganizer(String userId, String eventId, String eventName, RepoCallback<Void> cb) {
        // No longer storing event links in the user document
        cb.onSuccess(null);
    }

    /**
     * get waiting entrants from waitlist on FS
     * @param eventId
     * @param cb
     */
    @Override
    public void getWaitingEntrantIds(String eventId, RepoCallback<List<String>> cb) {
        // Get IDs directly from the waitlistedEntrantIds array to ensure consistency with manual testing
        ref(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> ids = (List<String>) doc.get("waitlistedEntrantIds");
                cb.onSuccess(ids != null ? ids : new ArrayList<>());
            } else {
                cb.onSuccess(new ArrayList<>());
            }
        }).addOnFailureListener(cb::onError);
    }

    /**
     * remove selected entrant from waitlist to pending list
     * @param eventId
     * @param entrantId
     * @param cb
     */
    @Override
    public void markEntrantSelected(String eventId, String entrantId, RepoCallback<Void> cb) {
        // Transition ID from waitlisted array to pendingEntrantIds array
        ref(eventId).update(
                "waitlistedEntrantIds", FieldValue.arrayRemove(entrantId),
                "pendingEntrantIds", FieldValue.arrayUnion(entrantId),
                "waitlistCount", FieldValue.increment(-1)
        ).addOnSuccessListener(unused -> cb.onSuccess(null))
         .addOnFailureListener(cb::onError);
    }

    /**
     * create notification to selected/specified entrant
     * @param eventId
     * @param entrantId
     * @param eventName
     * @param message
     * @param cb
     */
    @Override
    public void createNotification(String eventId, String entrantId, String eventName, String message, RepoCallback<Void> cb) {
        checkNotificationsEnabled(entrantId, enabled -> {
            if (!enabled) {
                cb.onSuccess(null);
                return;
            }
            Map<String, Object> notification = new HashMap<>();
            notification.put("eventId", eventId);
            notification.put("eventName", eventName);
            notification.put("entrantId", entrantId);
            notification.put("message", message);
            notification.put("type", "SELECTED");
            notification.put("createdAt", com.google.firebase.Timestamp.now());
            notification.put("isRead", false);

            db.collection("users")
                    .document(entrantId)
                    .collection("notification")
                    .add(notification)
                    .addOnSuccessListener(subDoc -> cb.onSuccess(null))
                    .addOnFailureListener(cb::onError);
        });
    }

    /**
     * get all pending entrant ids
     * @param eventId
     * @param cb
     */
    @Override
    public void getPendingEntrantIds(String eventId, RepoCallback<List<String>> cb) {
        ref(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> ids = (List<String>) doc.get("pendingEntrantIds");
                        cb.onSuccess(ids != null ? ids : new ArrayList<>());
                    } else {
                        cb.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(cb::onError);

    }

    /**
     * send message to pending
     * @param eventId
     * @param cb
     */
    @Override
    public void getRegisteredEntrantIds(String eventId, RepoCallback<List<String>> cb) {
        ref(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> ids = (List<String>) doc.get("registeredEntrantIds");
                cb.onSuccess(ids != null ? ids : new ArrayList<>());
            } else {
                cb.onSuccess(new ArrayList<>());
            }
        }).addOnFailureListener(cb::onError);
    }

    /**
     * send message to entrants
     * @param eventId
     * @param eventName
     * @param userIds
     * @param message
     * @param cb
     */
    public void sendMessageToEntrant(String eventId, String eventName, List<String> userIds, String message, String audience, RepoCallback<Void> cb) {
        if (userIds == null || userIds.isEmpty()) {
            cb.onError(new IllegalArgumentException("No entrants found"));
            return;
        }

        final int total = userIds.size();
        final int[] done = {0};
        final boolean[] failed = {false};
        Timestamp createdAt = Timestamp.now();

        for (String id : userIds) {
            checkNotificationsEnabled(id, enabled -> {
                if (enabled) {
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("eventId", eventId);
                    notification.put("recipientUid", id);
                    notification.put("eventName", eventName);
            notification.put("message", message);
            notification.put("type", "MESSAGE");
            notification.put("audience", audience);
            notification.put("createdAt", createdAt);
            notification.put("isRead", false);
            db.collection("users")
                    .document(id)
                    .collection("notification")
                    .add(notification)
                    .addOnSuccessListener(ref -> {
                        if (failed[0]) return;
                        done[0]++;
                        if (done[0] == total) {
                            cb.onSuccess(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (failed[0]) return;
                        failed[0] = true;
                        cb.onError(e);
                    });}
                else {
                    done[0]++;
                    if (!failed[0] && done[0] == total) cb.onSuccess(null);
                }
            });

        }
    }

    private void checkNotificationsEnabled(String entrantId, NotificationEnabledCallback cb) {
        db.collection("users")
                .document(entrantId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean enabled = doc.getBoolean("notificationsEnabled");
                        cb.onResult(enabled == null || enabled);
                    }
                    else {
                        cb.onResult(true);
                    }
                })
                .addOnFailureListener(e -> cb.onResult(true));

    }

    private interface NotificationEnabledCallback {
        void onResult(boolean enabled);
    }

    /***
     * US 01.04.02, notifies entrant they were not selected
     * @param eventId event
     * @param entrantId entrant to be notified
     * @param eventName name of event
     * @param cb callback on success/failure
     */
    public void createLossNotification(String eventId, String entrantId, String eventName, RepoCallback<Void> cb) {
        checkNotificationsEnabled(entrantId, enabled -> {
            if (!enabled ) {
                cb.onSuccess(null);
                return;
            }
            Map<String, Object> notification = new HashMap<>();
            notification.put("eventId", eventId);
            notification.put("eventName", eventName);
            notification.put("entrantId", entrantId);
            notification.put("message", "Unfortunately, you were not selected in the lottery for " + eventName + ". You still have a chance to be selected in future rounds!");
            notification.put("type", "NOT_SELECTED");
            notification.put("createdAt", com.google.firebase.Timestamp.now());
            notification.put("isRead", false);

            db.collection("users")
                    .document(entrantId)
                    .collection("notification")
                    .add(notification)
                    .addOnSuccessListener(subDoc -> cb.onSuccess(null))
                    .addOnFailureListener(cb::onError);

        });
    }

    /***
     * US 01.05.06 notify entrant they have been invited to join private event waiting list
     * @param eventId
     * @param entrantId
     * @param eventName
     * @param cb
     */
    public void createWaitListInviteNotifications(String eventId, String entrantId, String eventName, RepoCallback<Void> cb) {
        checkNotificationsEnabled(entrantId, enabled -> {
            if (!enabled) {
                cb.onSuccess(null);
                return;
            }
            Map<String, Object> notification = new HashMap<>();
            notification.put("eventId", eventId);
            notification.put("eventName", eventName);
            notification.put("entrantId", entrantId);
            notification.put("message", "You have been invited to join waiting list for the private event: " + eventName + ".");
            notification.put("type", "WAITLIST_INVITE");
            notification.put("createdAt", com.google.firebase.Timestamp.now());
            notification.put("isRead", false);

            db.collection("users")
                    .document(entrantId)
                    .collection("notification")
                    .add(notification)
                    .addOnSuccessListener(subDoc -> cb.onSuccess(null))
                    .addOnFailureListener(cb::onError);

        });
    }

    /***
     * US 01.09.01 notify entrant that they have been invited to be a co-organizer for an event
     * @param eventId
     * @param entrantId
     * @param eventName
     * @param cb
     */
    public void createCoOrganizerInviteNotification(String eventId, String entrantId, String eventName, RepoCallback<Void> cb) {
        checkNotificationsEnabled(entrantId, enabled -> {
            if (!enabled) {
                cb.onSuccess(null);
                return;
            }
            Map<String, Object> notification = new HashMap<>();
            notification.put("eventId", eventId);
            notification.put("eventName", eventName);
            notification.put("entrantId", entrantId);
            notification.put("message", "You have been invited to be a co-organizer for the event: " + eventName + ".");
            notification.put("type", "CO_ORGANIZER_INVITE");
            notification.put("createdAt", com.google.firebase.Timestamp.now());
            notification.put("isRead", false);

            db.collection("users")
                    .document(entrantId)
                    .collection("notification")
                    .add(notification)
                    .addOnSuccessListener(subDoc -> cb.onSuccess(null))
                    .addOnFailureListener(cb::onError);

        });
    }

    /**
     * get cancelled entrants id
     * @param eventId unique event id
     * @param cb      callback returning a list of ids
     */
    @Override
    public void getCancelledEntrantIds(String eventId, RepoCallback<List<String>> cb) {
        ref(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> ids = (List<String>) doc.get("cancelledEntrantIds");
                        cb.onSuccess(ids != null ? ids: new ArrayList<>());
                    } else {
                        cb.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * get comments of an event from fs
     * @param eventId
     * @param cb
     */
    @Override
    public void getEventComments(String eventId, RepoCallback<List<EventComment>> cb) {
        ref(eventId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    List<EventComment> comments = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : qs) {
                        String authorName = doc.getString("userName");
                        String entrantId = doc.getString("userId");
                        Timestamp createdAt = doc.getTimestamp("timestamp");
                        String text = doc.getString("text");

                        if (authorName == null || authorName.isEmpty()) {
                            authorName = doc.getString("name");
                        }
                        if ((authorName == null || authorName.isEmpty()) && entrantId != null) {
                            authorName = entrantId;
                        }
                        if (text == null || text.isEmpty()) {
                            text = doc.getString("comment");
                        }

                        String parentCommentId = doc.getString("parentCommentId");
                        String replyToEntrantId = doc.getString("replyToEntrantId");
                        String replyToAuthorName = doc.getString("replyToAuthorName");
                        List<String> mentionedUserNames = (List<String>) doc.get("mentionedUserNames");

                        comments.add(new EventComment(
                                doc.getId(),
                                authorName != null ? authorName : "Unknown User",
                                entrantId,
                                text != null ? text : "(empty comment)",
                                createdAt,
                                parentCommentId,
                                replyToEntrantId,
                                replyToAuthorName,
                                mentionedUserNames
                        ));
                    }
                    cb.onSuccess(comments);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * delete a comment from fs
     * @param eventId
     * @param commentId
     * @param cb
     */
    @Override
    public void deleteEventComment(String eventId, String commentId, RepoCallback<Void> cb) {
        ref(eventId)
                .collection("comments")
                .document(commentId)
                .delete()
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);
    }

    /***
     * US 03.01.01 deletes an event
     * @param eventId event id
     * @param cb callback for success or error
     */
    @Override
    public void deleteEvent(String eventId, RepoCallback<Void> cb) {
        ref(eventId).delete()
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onError);

    }
}
