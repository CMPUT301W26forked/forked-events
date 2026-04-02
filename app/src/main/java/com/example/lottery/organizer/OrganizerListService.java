package com.example.lottery.organizer;

import java.util.List;

/**
 * Service for organizer to retrieve entrant lists for an event.
 * Covers waiting list, pending (invited) list, and final enrolled list.
 */
public class OrganizerListService {
    private final EventRepo repo;

    public OrganizerListService(EventRepo repo) {
        this.repo = repo;
    }

    /**
     * US 02.02.01 - Get entrants currently on the waiting list.
     */
    public void getWaitlist(String eventId, RepoCallback<List<String>> cb) {
        repo.getWaitingEntrantIds(eventId, cb);
    }

    /**
     * US 02.06.01 - Get entrants on the pending (invited) list.
     */
    public void getPendingList(String eventId, RepoCallback<List<String>> cb) {
        repo.getPendingEntrantIds(eventId, cb);
    }

    /**
     * US 02.06.03 - Get entrants who have enrolled (accepted invitation).
     */
    public void getEnrolledList(String eventId, RepoCallback<List<String>> cb) {
        repo.getRegisteredEntrantIds(eventId, cb);
    }

    /**
     * US 02.06.04 - Get entrants who were cancelled (did not sign up for the event).
     */
    public void getCancelledList(String eventId, RepoCallback<List<String>> cb) {
        repo.getCancelledEntrantIds(eventId, cb);
    }
}
