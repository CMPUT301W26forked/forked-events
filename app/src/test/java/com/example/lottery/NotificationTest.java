package com.example.lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * unit tests for the notification model
 */
public class NotificationTest {

    /**
     * tests that the full constructor correctly initializes fields
     */
    @Test
    public void testNotificationConstructor() {
        String title = "test title";
        String eventName = "test event";
        String eventId = "event123";
        String date = "2024-03-25";
        String status = "WAITLISTED";

        Notification notification = new Notification(title, eventName, eventId, date, status);

        assertEquals(title, notification.getTitle());
        assertEquals(eventName, notification.getEventName());
        assertEquals(eventId, notification.getEventId());
        assertEquals(date, notification.getDate());
        assertEquals(status, notification.getStatus());
        assertNull(notification.getMessage());
        assertNull(notification.getType());
        assertNull(notification.getCreatedAt());
    }


}
