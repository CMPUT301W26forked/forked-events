package com.example.lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.lottery.Entrant.Model.EntrantProfile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * unit tests for entrant profile model
 */
public class EntrantProfileTest {

    private EntrantProfile profile;

    /**
     * initializes a new entrant profile before each test
     */
    @Before
    public void setUp() {
        profile = new EntrantProfile();
    }

    /**
     * tests the default constructor initializes fields to null
     */
    @Test
    public void testDefaultConstructor() {
        assertNull(profile.getId());
        assertNull(profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getPhone());
        assertNull(profile.getRegisteredEventIds());
    }

    /**
     * tests the full constructor correctly initializes fields
     */
    @Test
    public void testFullConstructor() {
        String id = "user123";
        String name = "john doe";
        String email = "john@example.com";
        String phone = "1234567890";
        ArrayList<String> events = new ArrayList<>();
        events.add("event1");

        EntrantProfile fullProfile = new EntrantProfile(id, name, email, phone, events);

        assertEquals(id, fullProfile.getId());
        assertEquals(name, fullProfile.getName());
        assertEquals(email, fullProfile.getEmail());
        assertEquals(phone, fullProfile.getPhone());
        assertEquals(events, fullProfile.getRegisteredEventIds());
        assertEquals(1, fullProfile.getRegisteredEventIds().size());
    }

    /**
     * tests getters and setters
     */
    @Test
    public void testGettersAndSetters() {
        profile.setId("id1");
        assertEquals("id1", profile.getId());

        profile.setName("name1");
        assertEquals("name1", profile.getName());

        profile.setEmail("email1");
        assertEquals("email1", profile.getEmail());

        profile.setPhone("phone1");
        assertEquals("phone1", profile.getPhone());

        ArrayList<String> events = new ArrayList<>();
        events.add("e1");
        profile.setRegisteredEventIds(events);
        assertEquals(events, profile.getRegisteredEventIds());
        assertTrue(profile.getRegisteredEventIds().contains("e1"));
    }
}
