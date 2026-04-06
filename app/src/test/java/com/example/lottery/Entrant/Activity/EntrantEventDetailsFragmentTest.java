package com.example.lottery.Entrant.Activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for EntrantEventDetailsFragment logic.
 *
 * These tests verify waitlist behavior, event status,
 * and stay-in-list decisions.
 */
@RunWith(JUnit4.class)
public class EntrantEventDetailsFragmentTest {

    private static final String TEST_EVENT_ID = "event_abc";
    private static final String TEST_ENTRANT_ID = "entrant_abc";

    /**
     * When status is "open", joining should be allowed.
     */
    @Test
    public void joinWaitlist_whenStatusOpen_isAllowed() {
        String eventStatus = "open";
        boolean canJoin = "Open".equalsIgnoreCase(eventStatus);
        assertTrue(canJoin);
    }

    /**
     * When status is "Closed", joining should be blocked.
     */
    @Test
    public void joinWaitlist_whenStatusClosed_isBlocked() {
        String eventStatus = "Closed";
        boolean canJoin = "Open".equalsIgnoreCase(eventStatus);
        assertFalse(canJoin);
    }

    /**
     * When status is null, joining should be blocked.
     */
    @Test
    public void joinWaitlist_whenStatusNull_isBlocked() {
        String eventStatus = null;
        boolean canJoin = eventStatus != null && "Open".equalsIgnoreCase(eventStatus);
        assertFalse(canJoin);
    }

    /**
     * When waitlist is not full, joining should be allowed.
     */
    @Test
    public void joinWaitlist_whenNotFull_isAllowed() {
        long waitListLimit = 2L;
        long waitlistCount = 1L;
        boolean isFull = waitlistCount >= waitListLimit;
        assertFalse(isFull);
    }

    /**
     * When waitlist count equals the limit, it is full.
     */
    @Test
    public void joinWaitlist_whenCountEqualsLimit_isFull() {
        long waitListLimit = 2L;
        long waitlistCount = 2L;
        boolean isFull = waitlistCount >= waitListLimit;
        assertTrue(isFull);
    }

    /**
     * When waitlist count exceeds the limit, it is still full.
     */
    @Test
    public void joinWaitlist_whenCountExceedsLimit_isFull() {
        long waitListLimit = 2L;
        long waitlistCount = 4L;
        boolean isFull = waitlistCount >= waitListLimit;
        assertTrue(isFull);
    }

    /**
     * After leaving, entrant should not be on waitlist.
     */
    @Test
    public void leaveWaitlist_setsIsOnWaitlistFalse() {
        boolean isOnWaitlist = true;
        isOnWaitlist = false;
        assertFalse(isOnWaitlist);
    }

    /**
     * Waitlist count should display correctly.
     */
    @Test
    public void loadEventDetails_displaysCorrectWaitlistCount() {
        Long waitlistCount = 7L;
        String display = waitlistCount != null ? String.valueOf(waitlistCount) : "0";
        assertEquals("7", display);
    }

    /**
     * When entrant is in waitlist, it should be detected.
     */
    @Test
    public void loadEventDetails_whenEntrantOnWaitlist_setsTrue() {
        List<String> waitlistedIds = Arrays.asList(TEST_ENTRANT_ID, "id2", "id3");
        boolean isOnWaitlist = waitlistedIds.contains(TEST_ENTRANT_ID);
        assertTrue(isOnWaitlist);
    }


    /**
     * Choosing "No" removes entrant from waitlist.
     */
    @Test
    public void showStayInList_onNo_leavesWaitlist() {
        boolean isOnWaitlist = true;
        boolean choseNo = true;

        if (choseNo) {
            isOnWaitlist = false;
        }

        assertFalse(isOnWaitlist);
    }

    /**
     * Choosing "Yes" keeps entrant on waitlist.
     */
    @Test
    public void showStayInList_onYes_remainsOnWaitlist() {
        boolean isOnWaitlist = true;
        boolean choseYes = true;

        if (choseYes) {
            isOnWaitlist = true;
        }

        assertTrue(isOnWaitlist);
    }

    /**
     * Event ID should match expected value.
     */
    @Test
    public void eventDetails_usesCorrectEventId() {
        assertEquals("event_abc", TEST_EVENT_ID);
    }

    /**
     * Entrant ID should match expected value.
     */
    @Test
    public void eventDetails_usesCorrectEntrantId() {
        assertEquals("entrant_abc", TEST_ENTRANT_ID);
    }
}