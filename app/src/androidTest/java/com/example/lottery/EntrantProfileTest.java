package com.example.lottery;

import com.example.lottery.Entrant.Model.EntrantProfile;
import com.example.lottery.admin.AdminRemoveProfileFragment;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Unit tests for EntrantProfile model class.
 * Verifies constructors, getters, setters, and edge cases for profile data handling.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantProfileTest {

    /**
     * Tests that the parameterized constructor correctly initializes fields
     * and that all getter methods return the expected values.
     */
    @Test
    public void entrantProfile_constructorAndGetters_workCorrectly() {
        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("event1");
        eventIds.add("event2");

        EntrantProfile profile = new EntrantProfile(
                "u1",
                "Vidhi",
                "vidhi@email.com",
                "1234567890",
                eventIds
        );

        org.junit.Assert.assertEquals("u1", profile.getId());
        org.junit.Assert.assertEquals("Vidhi", profile.getName());
        org.junit.Assert.assertEquals("vidhi@email.com", profile.getEmail());
        org.junit.Assert.assertEquals("1234567890", profile.getPhone());
        org.junit.Assert.assertEquals(2, profile.getRegisteredEventIds().size());
    }

    /**
     * Tests that setter methods correctly update profile fields
     * and values can be retrieved accurately using getters.
     */
    @Test
    public void entrantProfile_setters_workCorrectly() {
        EntrantProfile profile = new EntrantProfile();

        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("event123");

        profile.setId("u2");
        profile.setName("Test User");
        profile.setEmail("test@email.com");
        profile.setPhone("9999999999");
        profile.setRegisteredEventIds(eventIds);

        org.junit.Assert.assertEquals("u2", profile.getId());
        org.junit.Assert.assertEquals("Test User", profile.getName());
        org.junit.Assert.assertEquals("test@email.com", profile.getEmail());
        org.junit.Assert.assertEquals("9999999999", profile.getPhone());
        org.junit.Assert.assertEquals("event123", profile.getRegisteredEventIds().get(0));
    }

    /**
     * Verifies that the empty constructor initializes all fields to null
     * ensuring default state is correctly handled.
     */
    @Test
    public void entrantProfile_emptyConstructor_hasDefaultValues() {
        EntrantProfile profile = new EntrantProfile();

        org.junit.Assert.assertNull(profile.getId());
        org.junit.Assert.assertNull(profile.getName());
        org.junit.Assert.assertNull(profile.getEmail());
        org.junit.Assert.assertNull(profile.getPhone());
        org.junit.Assert.assertNull(profile.getRegisteredEventIds());
    }

    /**
     * Ensures that an empty event list is stored properly
     * and does not cause null pointer issues.
     */
    @Test
    public void entrantProfile_emptyEventList_storesCorrectly() {
        ArrayList<String> eventIds = new ArrayList<>();

        EntrantProfile profile = new EntrantProfile(
                "u3",
                "Empty User",
                "empty@email.com",
                "1111111111",
                eventIds
        );

        org.junit.Assert.assertNotNull(profile.getRegisteredEventIds());
        org.junit.Assert.assertEquals(0, profile.getRegisteredEventIds().size());
    }

    /**
     * Tests that a null event list is handled correctly
     * and remains null when passed to the constructor.
     */
    @Test
    public void entrantProfile_nullEventList_worksCorrectly() {
        EntrantProfile profile = new EntrantProfile(
                "u4",
                "Null User",
                "null@email.com",
                "2222222222",
                null
        );

        org.junit.Assert.assertNull(profile.getRegisteredEventIds());
    }

    /**
     * Verifies that updating the event list replaces old values
     * rather than appending or merging with previous data.
     */
    @Test
    public void entrantProfile_updateRegisteredEvents_replacesOldValues() {
        EntrantProfile profile = new EntrantProfile();

        ArrayList<String> firstList = new ArrayList<>();
        firstList.add("event1");

        ArrayList<String> secondList = new ArrayList<>();
        secondList.add("event2");
        secondList.add("event3");

        profile.setRegisteredEventIds(firstList);
        profile.setRegisteredEventIds(secondList);

        org.junit.Assert.assertEquals(2, profile.getRegisteredEventIds().size());
        org.junit.Assert.assertEquals("event2", profile.getRegisteredEventIds().get(0));
    }

    /**
     * Tests that long string values are stored and retrieved correctly
     * without truncation or errors.
     */
    @Test
    public void entrantProfile_longStrings_storeCorrectly() {
        String longName = "VeryLongNameForTestingProfileStorage";

        EntrantProfile profile = new EntrantProfile();
        profile.setName(longName);

        org.junit.Assert.assertEquals(longName, profile.getName());
    }
}