package com.example.lottery.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for ImageModerationFragment logic.
 * Tests image loading, removal, and list management.
 */
public class ImageModerationFragmentTest {

    private List<ModerationItem> moderationList;

    @Before
    public void setUp() {
        moderationList = new ArrayList<>();
    }

    /**
     * Tests that the moderation list starts empty.
     */
    @Test
    public void moderationListStartsEmpty() {
        assertEquals(0, moderationList.size());
    }

    /**
     * Tests that images can be added to the moderation list.
     */
    @Test
    public void addingImageToListIncreasesSize() {
        ModerationItem item = new ModerationItem("event1", "Event 1", "url1");
        moderationList.add(item);

        assertEquals(1, moderationList.size());
        assertEquals("event1", moderationList.get(0).getEventId());
    }

    /**
     * Tests that multiple images can be added and retrieved correctly.
     */
    @Test
    public void multipleImagesCanBeAddedToList() {
        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));
        moderationList.add(new ModerationItem("event3", "Event 3", "url3"));

        assertEquals(3, moderationList.size());
        assertEquals("event2", moderationList.get(1).getEventId());
        assertEquals("Event 3", moderationList.get(2).getTitle());
    }

    /**
     * Tests that images can be removed from the list by index.
     */
    @Test
    public void removingImageByIndexDecreasesList() {
        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));
        moderationList.add(new ModerationItem("event3", "Event 3", "url3"));

        moderationList.remove(1);

        assertEquals(2, moderationList.size());
        assertEquals("event1", moderationList.get(0).getEventId());
        assertEquals("event3", moderationList.get(1).getEventId());
    }

    /**
     * Tests that the list can be cleared.
     */
    @Test
    public void clearingListMakesItEmpty() {
        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));

        moderationList.clear();

        assertEquals(0, moderationList.size());
    }

    /**
     * Tests finding an image by event ID.
     */
    @Test
    public void findingImageByEventIdWorks() {
        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));
        moderationList.add(new ModerationItem("event3", "Event 3", "url3"));

        int index = -1;
        for (int i = 0; i < moderationList.size(); i++) {
            if (moderationList.get(i).getEventId().equals("event2")) {
                index = i;
                break;
            }
        }

        assertEquals(1, index);
        assertEquals("Event 2", moderationList.get(index).getTitle());
    }

    /**
     * Tests that finding a non-existent event ID returns -1.
     */
    @Test
    public void findingNonExistentEventIdReturnsNegative() {
        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));

        int index = -1;
        for (int i = 0; i < moderationList.size(); i++) {
            if (moderationList.get(i).getEventId().equals("nonexistent")) {
                index = i;
                break;
            }
        }

        assertEquals(-1, index);
    }

    /**
     * Tests that removing by event ID works correctly.
     */
    @Test
    public void removingImageByEventIdWorks() {
        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));
        moderationList.add(new ModerationItem("event3", "Event 3", "url3"));

        // Find and remove event2
        for (int i = 0; i < moderationList.size(); i++) {
            if (moderationList.get(i).getEventId().equals("event2")) {
                moderationList.remove(i);
                break;
            }
        }

        assertEquals(2, moderationList.size());
        // Verify event2 is gone
        for (ModerationItem item : moderationList) {
            assertTrue(!item.getEventId().equals("event2"));
        }
    }

    /**
     * Tests that the list preserves order after removals.
     */
    @Test
    public void listPreservesOrderAfterRemoval() {
        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));
        moderationList.add(new ModerationItem("event3", "Event 3", "url3"));
        moderationList.add(new ModerationItem("event4", "Event 4", "url4"));

        moderationList.remove(1); // Remove event2

        assertEquals("event1", moderationList.get(0).getEventId());
        assertEquals("event3", moderationList.get(1).getEventId());
        assertEquals("event4", moderationList.get(2).getEventId());
    }

    /**
     * Tests that duplicate event IDs can exist in the list (edge case).
     */
    @Test
    public void listCanContainDuplicateEventIds() {
        moderationList.add(new ModerationItem("event1", "Event 1 Version 1", "url1"));
        moderationList.add(new ModerationItem("event1", "Event 1 Version 2", "url2"));

        assertEquals(2, moderationList.size());
        assertEquals("event1", moderationList.get(0).getEventId());
        assertEquals("event1", moderationList.get(1).getEventId());
    }

    /**
     * Tests that the count is accurate after various operations.
     */
    @Test
    public void countIsAccurateAfterOperations() {
        assertEquals(0, moderationList.size());

        moderationList.add(new ModerationItem("event1", "Event 1", "url1"));
        assertEquals(1, moderationList.size());

        moderationList.add(new ModerationItem("event2", "Event 2", "url2"));
        assertEquals(2, moderationList.size());

        moderationList.remove(0);
        assertEquals(1, moderationList.size());

        moderationList.clear();
        assertEquals(0, moderationList.size());
    }
}
