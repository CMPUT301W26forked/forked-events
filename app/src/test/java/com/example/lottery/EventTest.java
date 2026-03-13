package com.example.lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * unit tests for the event model
 */
public class EventTest {

    private Event event;

    /**
     * initializes a new event instance before each test
     */
    @Before
    public void setUp() {
        event = new Event();
    }

    /**
     * tests that getTitle prioritizes name over title
     */
    @Test
    public void testGetTitle() {
        event.setTitle("backup title");
        assertEquals("backup title", event.getTitle());

        event.setName("primary name");
        assertEquals("primary name", event.getTitle());
    }

    /**
     * tests that getDate correctly formats registration timestamps
     */
    @Test
    public void testGetDateFromTimestamps() {
        Date start = new Date(124, 2, 10); 
        Date end = new Date(124, 2, 20);
        
        event.setRegistrationStart(new Timestamp(start));
        event.setRegistrationEnd(new Timestamp(end));
        
        String result = event.getDate();
        assertTrue(result.contains("Mar 10, 2024"));
        assertTrue(result.contains("Mar 20, 2024"));
    }

    /**
     * tests that getSpots correctly formats the availability string
     */
    @Test
    public void testGetSpots() {
        event.setSpots("no spots");
        assertEquals("no spots", event.getSpots());

        event.setTotalSpots(25);
        assertEquals("25 spots available", event.getSpots());
    }

    /**
     * tests that getJoinedCount correctly formats waitlist count
     */
    @Test
    public void testGetJoinedCount() {
        event.setJoinedCount("0 joined");
        assertEquals("0 joined", event.getJoinedCount());

        event.setWaitListCount(10);
        assertEquals("10 Joined", event.getJoinedCount());
    }
}
