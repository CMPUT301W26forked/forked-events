package com.example.lottery.Entrant.Repo;


public interface WaitlistRepo {
    /***
     * Adds user to waitlist
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    void joinWaitlist(String eventId, String userId, WaitlistCallback<Void> cb);

    /***
     * Removes user from waitlist
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    void leaveWaitlist(String eventId, String userId, WaitlistCallback<Void> cb);

    /***
     * Checks if user is on waitlist.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    void isOnWaitlist(String eventId, String userId, WaitlistCallback<Boolean> cb);

    /***
     * Returns total number of entrants on waiting list.
     * @param eventId ID of event.
     * @param cb Callback that is invoked on success or failure.
     */
    void getWaitlistCount(String eventId, WaitlistCallback<Long> cb);

    /***
     * Checks if user wants to remain on wait list.
     * @param eventId ID of event.
     * @param userId ID of device (used to identify user).
     * @param cb Callback that is invoked on success or failure.
     */
    void stayInList(String eventId, String userId, WaitlistCallback<Void> cb);
}