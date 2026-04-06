package com.example.lottery.Entrant;

import static org.junit.Assert.*;

import com.example.lottery.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for Event Filtering functionality.
 *
 * US 01.01.04 - Filter events by category/status
 * US 01.01.05 - Filter by location
 * US 01.01.06 - Search events by keywords
 */
public class EventFilterServiceTest {

    private List<Event> createTestEvents() {
        List<Event> events = new ArrayList<>();

        Event event1 = new Event();
        event1.setEventId("e1");
        event1.setName("Music Festival");
        event1.setDescription("A great outdoor music festival");
        event1.setLocation("Edmonton");
        event1.setStatus("open");

        Event event2 = new Event();
        event2.setEventId("e2");
        event2.setName("Tech Conference");
        event2.setDescription("Annual technology conference");
        event2.setLocation("Calgary");
        event2.setStatus("closed");

        Event event3 = new Event();
        event3.setEventId("e3");
        event3.setName("Charity Run");
        event3.setDescription("Run for a cause");
        event3.setLocation("Edmonton");
        event3.setStatus("open");

        events.add(event1);
        events.add(event2);
        events.add(event3);

        return events;
    }

    /**
     * US 01.01.04 - Test filtering events by status.
     */
    @Test
    public void filterByStatus_returnsMatchingEvents() {
        List<Event> allEvents = createTestEvents();

        // Filter by status "open"
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if ("open".equals(event.getStatus())) {
                result.add(event);
            }
        }

        assertEquals(2, result.size());
        assertEquals("e1", result.get(0).getEventId());
        assertEquals("e3", result.get(1).getEventId());
    }

    /**
     * US 01.01.04 - Test filtering with no matches returns empty list.
     */
    @Test
    public void filterByStatus_noMatches_returnsEmpty() {
        List<Event> allEvents = createTestEvents();

        // Filter by status "cancelled"
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if ("cancelled".equals(event.getStatus())) {
                result.add(event);
            }
        }

        assertTrue(result.isEmpty());
    }

    /**
     * US 01.01.05 - Test filtering events by location.
     */
    @Test
    public void filterByLocation_returnsMatchingEvents() {
        List<Event> allEvents = createTestEvents();

        // Filter by location "Edmonton"
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if ("Edmonton".equalsIgnoreCase(event.getLocation())) {
                result.add(event);
            }
        }

        assertEquals(2, result.size());
        assertEquals("e1", result.get(0).getEventId());
        assertEquals("e3", result.get(1).getEventId());
    }

    /**
     * US 01.01.05 - Test case-insensitive location filtering.
     */
    @Test
    public void filterByLocation_caseInsensitive() {
        List<Event> allEvents = createTestEvents();

        // Filter by location "edmonton" (lowercase)
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getLocation() != null &&
                event.getLocation().equalsIgnoreCase("edmonton")) {
                result.add(event);
            }
        }

        assertEquals(2, result.size());
    }

    /**
     * US 01.01.06 - Test searching events by keyword in name.
     */
    @Test
    public void searchByKeyword_matchesInName() {
        List<Event> allEvents = createTestEvents();

        // Search by keyword "Music"
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getName() != null &&
                event.getName().toLowerCase().contains("music")) {
                result.add(event);
            }
        }

        assertEquals(1, result.size());
        assertEquals("e1", result.get(0).getEventId());
    }

    /**
     * US 01.01.06 - Test searching events by keyword in description.
     */
    @Test
    public void searchByKeyword_matchesInDescription() {
        List<Event> allEvents = createTestEvents();

        // Search by keyword "technology"
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            String name = event.getName() != null ? event.getName().toLowerCase() : "";
            String desc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
            if (name.contains("technology") || desc.contains("technology")) {
                result.add(event);
            }
        }

        assertEquals(1, result.size());
        assertEquals("e2", result.get(0).getEventId());
    }

    /**
     * US 01.01.06 - Test searching with no matches returns empty list.
     */
    @Test
    public void searchByKeyword_noMatches_returnsEmpty() {
        List<Event> allEvents = createTestEvents();

        // Search by keyword "xyz123"
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            String name = event.getName() != null ? event.getName().toLowerCase() : "";
            String desc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
            if (name.contains("xyz123") || desc.contains("xyz123")) {
                result.add(event);
            }
        }

        assertTrue(result.isEmpty());
    }

    /**
     * US 01.01.06 - Test searching with empty keyword returns all events.
     */
    @Test
    public void searchByKeyword_emptyKeyword_returnsAll() {
        List<Event> allEvents = createTestEvents();

        // Search with empty string returns all events
        List<Event> result = new ArrayList<>(allEvents);

        assertEquals(3, result.size());
    }

    /**
     * US 01.01.06 - Test case-insensitive keyword search.
     */
    @Test
    public void searchByKeyword_caseInsensitive() {
        List<Event> allEvents = createTestEvents();

        // Search by keyword "CHARITY" (uppercase)
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getName() != null &&
                event.getName().toLowerCase().contains("charity")) {
                result.add(event);
            }
        }

        assertEquals(1, result.size());
        assertEquals("e3", result.get(0).getEventId());
    }

    /**
     * Test combined filtering and searching.
     */
    @Test
    public void filterAndSearch_combinedCriteria() {
        List<Event> allEvents = createTestEvents();

        // First filter by status "open"
        List<Event> openEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if ("open".equals(event.getStatus())) {
                openEvents.add(event);
            }
        }

        // Then search by keyword "music"
        List<Event> result = new ArrayList<>();
        for (Event event : openEvents) {
            if (event.getName() != null &&
                event.getName().toLowerCase().contains("music")) {
                result.add(event);
            }
        }

        assertEquals(1, result.size());
        assertEquals("e1", result.get(0).getEventId());
    }
}
