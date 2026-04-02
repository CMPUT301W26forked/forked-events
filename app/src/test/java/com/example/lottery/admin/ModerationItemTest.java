package com.example.lottery.admin;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the ModerationItem model class.
 */
public class ModerationItemTest {

    private ModerationItem moderationItem;
    private static final String TEST_EVENT_ID = "event123";
    private static final String TEST_TITLE = "Summer Festival";
    private static final String TEST_IMAGE_URL = "https://example.com/poster.jpg";

    @Before
    public void setUp() {
        moderationItem = new ModerationItem(TEST_EVENT_ID, TEST_TITLE, TEST_IMAGE_URL);
    }

    /**
     * Tests that the event ID is correctly stored and retrieved.
     */
    @Test
    public void getEventIdReturnsCorrectValue() {
        assertEquals(TEST_EVENT_ID, moderationItem.getEventId());
    }

    /**
     * Tests that the title is correctly stored and retrieved.
     */
    @Test
    public void getTitleReturnsCorrectValue() {
        assertEquals(TEST_TITLE, moderationItem.getTitle());
    }

    /**
     * Tests that the image URL is correctly stored and retrieved.
     */
    @Test
    public void getImageUrlReturnsCorrectValue() {
        assertEquals(TEST_IMAGE_URL, moderationItem.getImageUrl());
    }

    /**
     * Tests that a ModerationItem with empty strings is created correctly.
     */
    @Test
    public void moderationItemWithEmptyStringsIsValid() {
        ModerationItem emptyItem = new ModerationItem("", "", "");
        assertEquals("", emptyItem.getEventId());
        assertEquals("", emptyItem.getTitle());
        assertEquals("", emptyItem.getImageUrl());
    }

    /**
     * Tests that different ModerationItem instances have independent values.
     */
    @Test
    public void multipleInstancesHaveIndependentValues() {
        ModerationItem item1 = new ModerationItem("id1", "Event 1", "url1");
        ModerationItem item2 = new ModerationItem("id2", "Event 2", "url2");

        assertEquals("id1", item1.getEventId());
        assertEquals("id2", item2.getEventId());
        assertEquals("Event 1", item1.getTitle());
        assertEquals("Event 2", item2.getTitle());
    }

    /**
     * Tests that ModerationItem can store special characters in strings.
     */
    @Test
    public void moderationItemHandlesSpecialCharacters() {
        String specialTitle = "Event #1 - Summer & Winter";
        String specialUrl = "https://example.com/poster?id=123&type=event";
        ModerationItem item = new ModerationItem("id1", specialTitle, specialUrl);

        assertEquals(specialTitle, item.getTitle());
        assertEquals(specialUrl, item.getImageUrl());
    }

    /**
     * Tests that ModerationItem can store long strings.
     */
    @Test
    public void moderationItemHandlesLongStrings() {
        String longTitle = "A Very Long Event Title That Contains Multiple Words And Goes On For Quite Some Time To Test Long String Handling";
        ModerationItem item = new ModerationItem("id1", longTitle, TEST_IMAGE_URL);

        assertEquals(longTitle, item.getTitle());
    }
}
