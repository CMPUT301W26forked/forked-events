package com.example.lottery.Entrant.Service;

import com.example.lottery.Entrant.Repo.FSWaitlistRepo;
import com.example.lottery.Entrant.Repo.WaitlistCallback;
import com.example.lottery.Entrant.Repo.WaitlistRepo;

/***
 * Service layer for waitlist.
 */
public class WaitlistService {
    private static WaitlistRepo repo;

    /***
     * Creates a WaitlistService using the FireStore repo.
     */
    public WaitlistService(){
        this.repo = new FSWaitlistRepo();
    }

    /***
     * Adds user to waitlist for an event.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    public void joinWaitList(String eventId, String userId, WaitlistCallback<Void> cb) {
        repo.joinWaitlist(eventId, userId, cb);
    }

    /***
     * Removes user from waitlist of an event.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    public void leaveWaitList(String eventId, String userId, WaitlistCallback<Void> cb) {
        repo.leaveWaitlist(eventId, userId, cb);
    }

    /***
     * Checks if user is currently on waitlist for an event.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    public void checkWaitListStatus(String eventId, String userId, WaitlistCallback<Boolean> cb) {
        repo.isOnWaitlist(eventId, userId, cb);
    }

    /***
     * Counts total number of entrants on waitlist for an event.
     * @param eventId ID of event.
     * @param cb Callback that is invoked on success or failure.
     */
    public void getWaitListCount(String eventId, WaitlistCallback<Long> cb) {
        repo.getWaitlistCount(eventId, cb);
    }

    /***
     * Checks users who want to remain on waiting list.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    public void stayInList(String eventId, String userId, WaitlistCallback<Void> cb) {
        repo.stayInList(eventId, userId, cb);
    }



}