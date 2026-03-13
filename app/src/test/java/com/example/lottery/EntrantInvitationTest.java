package com.example.lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.lottery.Entrant.Model.EntrantInvitation;

import org.junit.Test;

/**
 * unit tests for entrant invitation model
 */
public class EntrantInvitationTest {

    /**
     * tests that the default constructor initializes fields to null
     */
    @Test
    public void testDefaultConstructor() {
        EntrantInvitation invitation = new EntrantInvitation();
        assertNull(invitation.getInvitationId());
        assertNull(invitation.getEventId());
        assertNull(invitation.getEntrantId());
        assertNull(invitation.getStatus());
    }

    /**
     * tests that the full constructor correctly initializes fields
     */
    @Test
    public void testFullConstructor() {
        String invitationId = "yee";
        String eventId = "haw";
        String entrantId = "123";
        String status = "PENDING";

        EntrantInvitation invitation = new EntrantInvitation(invitationId, eventId, entrantId, status);

        assertEquals(invitationId, invitation.getInvitationId());
        assertEquals(eventId, invitation.getEventId());
        assertEquals(entrantId, invitation.getEntrantId());
        assertEquals(status, invitation.getStatus());
    }

    /**
     * tests getters and setters
     */
    @Test
    public void testGettersAndSetters() {
        EntrantInvitation invitation = new EntrantInvitation();

        invitation.setInvitationId("id1");
        assertEquals("id1", invitation.getInvitationId());

        invitation.setEventId("e1");
        assertEquals("e1", invitation.getEventId());

        invitation.setEntrantId("u1");
        assertEquals("u1", invitation.getEntrantId());

        invitation.setStatus("ACCEPTED");
        assertEquals("ACCEPTED", invitation.getStatus());
    }
}
