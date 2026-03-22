package com.example.lottery.organizer;

import com.example.lottery.EventComment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Map;

/**
 * repository interface for event data operations
 */
public interface EventRepo {
    /**
     * fetches a single event document
     *
     * @param eventId unique event id
     * @param cb      callback for success or error
     */
    void getEvent(String eventId, RepoCallback<DocumentSnapshot> cb);

    /**
     * updates registration dates for an event
     *
     * @param eventId unique event id
     * @param start   registration start timestamp
     * @param end     registration end timestamp
     * @param cb      callback for success or error
     */
    void setRegStartPeriod(String eventId, Timestamp start, Timestamp end, RepoCallback<Void> cb);

    /**
     * updates the poster image url for an event
     *
     * @param eventId   unique event id
     * @param posterUrl public url for the poster
     * @param cb        callback for success or error
     */
    void setPosterUrl(String eventId, String posterUrl, RepoCallback<Void> cb);

    /**
     * saves or updates all event metadata
     *
     * @param eventId   unique event id
     * @param eventData map containing event fields
     * @param cb        callback for success or error
     */
    void saveEvent(String eventId, Map<String, Object> eventData, RepoCallback<Void> cb);

    /**
     * links an event to an organizer profile
     *
     * @param userId    unique user id
     * @param eventId   unique event id
     * @param eventName name of the event
     * @param cb        callback for success or error
     */
    void linkEventToOrganizer(String userId, String eventId, String eventName, RepoCallback<Void> cb);

    /**
     * retrieves a list of entrant ids currently on the waitlist
     *
     * @param eventId unique event id
     * @param cb      callback returning a list of ids
     */
    void getWaitingEntrantIds(String eventId, RepoCallback<List<String>> cb);

    /**
     * updates an entrant status to selected
     *
     * @param eventId   unique event id
     * @param entrantId unique user id
     * @param cb        callback for success or error
     */
    void markEntrantSelected(String eventId, String entrantId, RepoCallback<Void> cb);

    /**
     * creates a notification for an entrant
     *
     * @param eventId   unique event id
     * @param entrantId unique user id
     * @param eventName name of the event
     * @param message   notification message
     * @param cb        callback for success or error
     */
    void createNotification(String eventId, String entrantId, String eventName, String message, RepoCallback<Void> cb);

    /**
     * retrieves a list of entrant ids on the pending (invited) list
     *
     * @param eventId unique event id
     * @param cb      callback returning a list of ids
     */
    void getPendingEntrantIds(String eventId, RepoCallback<List<String>> cb);

    /**
     * retrieves a list of entrant ids who have enrolled (accepted invitation)
     *
     * @param eventId unique event id
     * @param cb      callback returning a list of ids
     */
    void getRegisteredEntrantIds(String eventId, RepoCallback<List<String>> cb);

    /**
     * retrieves a list of canceled entrant ids
     *
     * @param eventId unique event id
     * @param cb      callback returning a list of ids
     */
    void getCancelledEntrantIds(String eventId, RepoCallback<List<String>> cb);

    /**
     * get comments of an event
     * @param eventId
     * @param cb
     */
    void getEventComments(String eventId, RepoCallback<List<EventComment>> cb);

    /**
     * delete a comment
     * @param eventId
     * @param commentId
     * @param cb
     */
    void deleteEventComment(String eventId, String commentId, RepoCallback<Void> cb);
}
