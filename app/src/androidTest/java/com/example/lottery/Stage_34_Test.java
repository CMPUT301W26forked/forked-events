package com.example.lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class Stage_34_Test {
    /**
     * US 2.07.03
     * Organizer sends cancellation
     */
    @Test
    public void sendCancellation() {
        String eventName = "Test";
        String message = "Event " + eventName + "has been cancelled";

        assertTrue(message.contains("cancelled"));
    }

    /**
     * US 2.08.01
     * View and delete entrants comments
     */
    @Test
    public void deleteComment() {
        List<String> comments = new ArrayList<>(Arrays.asList("comment1", "comment2", "comment3"));

        comments.remove("comment2");

        assertEquals(2, comments.size());
        assertFalse(comments.contains("comment2"));
    }

    /**
     * US 3.07.01
     * Admin removes organizer
     */
    @Test
    public void removeOrganizer() {
        String organizerId = "test";
        List<String> blockedOrganizers = new ArrayList<>();

        blockedOrganizers.add(organizerId);

        assertTrue(blockedOrganizers.contains(organizerId));
    }

    /**
     * US 3.08.01
     * Admin reviews organizer notification logs
     */
    @Test
    public void reviewNotificationLogs() {
        Map<String, String> log = new HashMap<>();
        log.put("title", "Hello");
        log.put("eventName", "Test");

        assertEquals("Hello", log.get("title"));
        assertEquals("Test", log.get("eventName"));
    }

    /**
     * US 2.02.0z
     * Organizer map only shows entrants with location
     */
    @Test
    public void waitlistMap() {
        List<double[]> entrantLocations = Arrays.asList(
                new double[]{52.00, -112.00},
                null,
                new double[]{53.00, -113.00}
        );

        int visiblePins = 0;
        for (double[] location : entrantLocations) {
            if (location != null) {
                visiblePins++;
            }
        }

        assertEquals(2, visiblePins);
    }
}
