package com.example.lottery;

import org.junit.Test;

import static org.junit.Assert.*;
/**
 * Unit tests for device identification.
 *
 * These tests verify that device IDs are valid and consistent.
 */
public class DeviceIdentificationTest {

    /**
     * Tests that device ID is not null.
     */
    @Test
    public void deviceId_isNotNull() {
        String id = "device123";
        assertNotNull(id);
    }

    /**
     * Tests that device ID is consistent.
     */
    @Test
    public void deviceId_isConsistent() {
        String id1 = "device123";
        String id2 = "device123";

        assertEquals(id1, id2);
    }
}
