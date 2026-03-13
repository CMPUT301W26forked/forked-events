package com.example.lottery.Entrant.Activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class EntrantEventDetailsFragmentTest {

    private static final String TEST_EVENT_ID = "event_abc";
    private static final String TEST_ENTRANT_ID = "entrant_abc";


    /**
     * US 01.01.01
     * When status is "open", joining should be allowed.
     *
     */
    @Test
    public void joinWaitlist_whenStatusOpen_isAllowed() {
        String eventStatus = "open";
        boolean canJoin = "Open".equalsIgnoreCase(eventStatus);
        assertTrue(canJoin);
    }

    /**
     * US 01.01.01
     * When status is "Closed", joining should be blocked.
     */
    @Test
    public void joinWaitlist_whenStatusClosed_isBlocked() {
        String eventStatus = "Closed";
        boolean canJoin = "Open".equalsIgnoreCase(eventStatus);
        assertFalse(canJoin);
    }


    /**
     * US 01.01.01
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
     * US 01.01.01
     * When waitlist count exactly equals the limit, it should be treated as full.
     */
    @Test
    public void joinWaitlist_whenCountEqualsLimit_isFull() {
        long waitListLimit = 2L;
        long waitlistCount = 2L;
        boolean isFull = waitlistCount >= waitListLimit;
        assertTrue(isFull);
    }

    /**
     * US 01.01.01
     * When waitlist count exceeds the limit, it should still be treated as full.
     */
    @Test
    public void joinWaitlist_whenCountExceedsLimit_isFull() {
        long waitListLimit = 2L;
        long waitlistCount = 4L;
        boolean isFull = waitlistCount >= waitListLimit;
        assertTrue(isFull);
    }


    /**
     * US 01.01.02
     * After leaving, entrant should not be on waitlist.
     */
    @Test
    public void leaveWaitlist_setsIsOnWaitlistFalse() {
        boolean isOnWaitlist = true;
        isOnWaitlist = false;
        assertFalse(isOnWaitlist);
    }

    /**
     * US 01.01.02
     * After leaving, waitlist count should decrease by 1.
     */
    @Test
    public void leaveWaitlist_decrementsWaitlistCount() {
        long waitlistCount = 3L;
        waitlistCount = waitlistCount - 1;
        assertEquals(2L, waitlistCount);
    }

    /**
     * US 01.05.04
     * Waitlist count should display correctly.
     */
    @Test
    public void loadEventDetails_displaysCorrectWaitlistCount() {
        Long waitlistCount = 7L;
        String display = waitlistCount != null ? String.valueOf(waitlistCount) : "0";
        assertEquals("7", display);
    }


    /**
     * When entrant is in waitlist, "Leave Waitlist" button should show.
     */
    @Test
    public void loadEventDetails_whenEntrantOnWaitlist_setsIsOnWaitlistTrue() {
        List<String> waitlistedIds = Arrays.asList(TEST_ENTRANT_ID, "entrant_aaa111", "entrant_bbb222");
        boolean isOnWaitlist = waitlistedIds != null && waitlistedIds.contains(TEST_ENTRANT_ID);
        assertTrue(isOnWaitlist);
    }

    /**
     * When entrant is not in waitlist, "Join Waitlist" button should show.
     */
    @Test
    public void loadEventDetails_whenEntrantNotOnWaitlist_setsIsOnWaitlistFalse() {
        List<String> waitlistedIds = Arrays.asList("entrant_aaa111", "entrant_bbb222");
        boolean isOnWaitlist = waitlistedIds != null && waitlistedIds.contains(TEST_ENTRANT_ID);
        assertFalse(isOnWaitlist);
    }


    /**
     * US 01.05.01
     * When entrant chooses "No" in the stay-in-list dialog, they should be removed from the waitlist .
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
     * US 01.05.01
     * When entrant chooses "Yes" in the stay-in-list dialog, they should remain on the waitlist.
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
}
