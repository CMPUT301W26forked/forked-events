/**
 * Unit tests for EventComment.java
 * These tests verify creation of comments and replies,
 */
package com.example.lottery;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class EventCommentTest {
    /**
     * Tests that a parent comment is created correctly.
     */

    @Test
    public void constructor_withoutReplyFields_createsParentCommentCorrectly() {
        Timestamp timestamp = Timestamp.now();

        EventComment comment = new EventComment(
                "c1",
                "Alice",
                "entrant1",
                "This is a parent comment",
                timestamp
        );

        assertEquals("c1", comment.getCommentId());
        assertEquals("Alice", comment.getAuthorName());
        assertEquals("entrant1", comment.getEntrantId());
        assertEquals("This is a parent comment", comment.getText());
        assertEquals(timestamp, comment.getCreatedAt());

        assertNull(comment.getParentCommentId());
        assertNull(comment.getReplyToEntrantId());
        assertNull(comment.getReplyToAuthorName());
        assertNotNull(comment.getMentionedUserNames());
        assertTrue(comment.getMentionedUserNames().isEmpty());
        assertEquals(0, comment.getDepth());
        assertFalse(comment.isReply());
    }

    /**
     * Tests that a reply comment is created with correct parent linkage.
     */
    @Test
    public void constructor_withReplyFields_createsReplyCorrectly() {
        Timestamp timestamp = Timestamp.now();
        List<String> mentions = Arrays.asList("Alice", "Bob");

        EventComment reply = new EventComment(
                "c2",
                "Charlie",
                "entrant2",
                "This is a reply",
                timestamp,
                "c1",
                "entrant1",
                "Alice",
                mentions
        );

        assertEquals("c2", reply.getCommentId());
        assertEquals("Charlie", reply.getAuthorName());
        assertEquals("entrant2", reply.getEntrantId());
        assertEquals("This is a reply", reply.getText());
        assertEquals(timestamp, reply.getCreatedAt());

        assertEquals("c1", reply.getParentCommentId());
        assertEquals("entrant1", reply.getReplyToEntrantId());
        assertEquals("Alice", reply.getReplyToAuthorName());
        assertEquals(mentions, reply.getMentionedUserNames());
        assertTrue(reply.isReply());
    }

    /**
     * Tests that setter methods correctly update fields.
     */
    @Test
    public void setters_updateFieldsCorrectly() {
        EventComment comment = new EventComment();
        Timestamp timestamp = Timestamp.now();
        List<String> mentions = new ArrayList<>();
        mentions.add("David");

        comment.setCommentId("c3");
        comment.setAuthorName("Eva");
        comment.setEntrantId("entrant3");
        comment.setText("Updated text");
        comment.setCreatedAt(timestamp);
        comment.setParentCommentId("parent123");
        comment.setReplyToEntrantId("entrant1");
        comment.setReplyToAuthorName("Alice");
        comment.setMentionedUserNames(mentions);
        comment.setDepth(2);

        assertEquals("c3", comment.getCommentId());
        assertEquals("Eva", comment.getAuthorName());
        assertEquals("entrant3", comment.getEntrantId());
        assertEquals("Updated text", comment.getText());
        assertEquals(timestamp, comment.getCreatedAt());
        assertEquals("parent123", comment.getParentCommentId());
        assertEquals("entrant1", comment.getReplyToEntrantId());
        assertEquals("Alice", comment.getReplyToAuthorName());
        assertEquals(mentions, comment.getMentionedUserNames());
        assertEquals(2, comment.getDepth());
    }

    /**
     * Tests that default values are initialized correctly.
     */
    @Test
    public void defaultConstructor_initializesSafeDefaults() {
        EventComment comment = new EventComment();

        assertNotNull(comment.getMentionedUserNames());
        assertTrue(comment.getMentionedUserNames().isEmpty());
        assertEquals(0, comment.getDepth());
        assertFalse(comment.isReply());
    }

    /**
     * Tests that null mentioned users are handled safely.
     */
    @Test
    public void setMentionedUserNames_withNull_createsEmptyList() {
        EventComment comment = new EventComment();

        comment.setMentionedUserNames(null);

        assertNotNull(comment.getMentionedUserNames());
        assertTrue(comment.getMentionedUserNames().isEmpty());
    }
    /**
     * Tests that isReply() returns false when no parent ID exists.
     */
    @Test
    public void isReply_returnsFalse_whenParentCommentIdIsNull() {
        EventComment comment = new EventComment();
        comment.setParentCommentId(null);

        assertFalse(comment.isReply());
    }

    /**
     * Tests that isReply() returns false when parent ID is blank.
     */
    @Test
    public void isReply_returnsFalse_whenParentCommentIdIsBlank() {
        EventComment comment = new EventComment();
        comment.setParentCommentId("   ");

        assertFalse(comment.isReply());
    }

    /**
     * Tests that isReply() returns true when parent ID is set.
     */
    @Test
    public void isReply_returnsTrue_whenParentCommentIdExists() {
        EventComment comment = new EventComment();
        comment.setParentCommentId("parent001");

        assertTrue(comment.isReply());
    }
}
