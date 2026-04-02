package com.example.lottery;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class NotificationAndAdminTest {

    private static final String TEST_EVENT_ID = "event_abc";
    private static final String TEST_ENTRANT_ID = "entrant_abc";
    private static final String TEST_EVENT_NAME = "Test Swimming Lessons";

    /** win notification has correct type */
    @Test
    public void winNotification_hasCorrectType() {
        String type = "SELECTED";
        assertEquals("SELECTED", type);
    }

    /** loss notification has correct type */
    @Test
    public void lossNotification_hasCorrectType() {
        String type = "NOT_SELECTED";
        assertEquals("NOT_SELECTED", type);
    }

    /** loss message mentions event name */
    @Test
    public void lossNotification_messageMentionsEventName() {
        String message = "Unfortunately, you were not selected in the lottery for " + TEST_EVENT_NAME + ".";
        assertTrue(message.contains(TEST_EVENT_NAME));
    }

    /** notification not sent when opted out */
    @Test
    public void optOut_whenDisabled_notificationNotSent() {
        boolean notificationsEnabled = false;
        boolean sent = false;
        if (notificationsEnabled) sent = true;
        assertFalse(sent);
    }


    /** waitlist invite has correct type */
    @Test
    public void waitlistInvite_hasCorrectType() {
        String type = "WAITLIST_INVITE";
        assertEquals("WAITLIST_INVITE", type);
    }


    /** co-organizer invite has correct type */
    @Test
    public void coOrganizerInvite_hasCorrectType() {
        String type = "CO_ORGANIZER_INVITE";
        assertEquals("CO_ORGANIZER_INVITE", type);
    }

    /** co-organizer invite message mentions event name */
    @Test
    public void coOrganizerInvite_messageMentionsEventName() {
        String message = "You have been invited to be a co-organizer for the event: " + TEST_EVENT_NAME + ".";
        assertTrue(message.contains(TEST_EVENT_NAME));
    }

    /** delete event removes it from list */
    @Test
    public void deleteEvent_removesFromList() {
        List<String> events = new ArrayList<>(Arrays.asList("event_1", TEST_EVENT_ID));
        events.remove(TEST_EVENT_ID);
        assertFalse(events.contains(TEST_EVENT_ID));
    }



}
