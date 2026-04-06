package com.example.lottery.Entrant;

import static org.junit.Assert.*;

import com.example.lottery.Entrant.Model.EntrantProfile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Unit tests for Entrant Profile functionality.
 *
 * US 01.02.03 - View registered event history
 * US 01.02.04 - Delete my own profile
 */
public class EntrantProfileServiceTest {

    /**
     * US 01.02.03 - Test retrieving registered event history from profile.
     */
    @Test
    public void getRegisteredEventHistory_returnsEventList() {
        ArrayList<String> eventIds = new ArrayList<>(Arrays.asList("event1", "event2", "event3"));
        EntrantProfile profile = new EntrantProfile("u1", "John", "john@test.com", "1234567890", eventIds);

        ArrayList<String> registeredEvents = profile.getRegisteredEventIds();

        assertNotNull(registeredEvents);
        assertEquals(3, registeredEvents.size());
        assertEquals("event1", registeredEvents.get(0));
        assertEquals("event2", registeredEvents.get(1));
        assertEquals("event3", registeredEvents.get(2));
    }

    /**
     * US 01.02.03 - Test history with empty registered events.
     */
    @Test
    public void getRegisteredEventHistory_emptyList_returnsEmpty() {
        EntrantProfile profile = new EntrantProfile("u1", "John", "john@test.com", "1234567890", new ArrayList<>());

        ArrayList<String> registeredEvents = profile.getRegisteredEventIds();

        assertNotNull(registeredEvents);
        assertEquals(0, registeredEvents.size());
    }

    /**
     * US 01.02.04 - Test profile can be marked for deletion.
     */
    @Test
    public void profileCanBeDeleted() {
        EntrantProfile profile = new EntrantProfile("u1", "John", "john@test.com", "1234567890", new ArrayList<>());

        assertNotNull(profile);
        assertEquals("u1", profile.getId());

        // Simulate deletion by clearing data
        profile.setRegisteredEventIds(null);
        assertNull(profile.getRegisteredEventIds());
    }

    /**
     * US 01.02.04 - Test profile data is accessible before deletion.
     */
    @Test
    public void profileDataAccessible_beforeDeletion() {
        EntrantProfile profile = new EntrantProfile("u1", "John", "john@test.com", "1234567890",
                new ArrayList<>(Arrays.asList("event1")));

        assertEquals("u1", profile.getId());
        assertEquals("John", profile.getName());
        assertEquals("john@test.com", profile.getEmail());
        assertEquals("1234567890", profile.getPhone());
        assertEquals(1, profile.getRegisteredEventIds().size());
    }

    /**
     * US 01.05.07 - Test accepting private invitation updates registered events.
     */
    @Test
    public void acceptPrivateInvitation_addsToRegisteredEvents() {
        ArrayList<String> eventIds = new ArrayList<>();
        EntrantProfile profile = new EntrantProfile("u1", "John", "john@test.com", "1234567890", eventIds);

        // Simulate accepting invitation by adding event
        eventIds.add("private_event_123");
        profile.setRegisteredEventIds(eventIds);

        assertEquals(1, profile.getRegisteredEventIds().size());
        assertEquals("private_event_123", profile.getRegisteredEventIds().get(0));
    }

    /**
     * US 01.05.07 - Test declining private invitation does not add to registered events.
     */
    @Test
    public void declinePrivateInvitation_doesNotAddToRegisteredEvents() {
        ArrayList<String> eventIds = new ArrayList<>();
        EntrantProfile profile = new EntrantProfile("u1", "John", "john@test.com", "1234567890", eventIds);

        // Simulate declining invitation - events list remains unchanged
        assertEquals(0, profile.getRegisteredEventIds().size());
    }
}
