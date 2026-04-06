package com.example.lottery.admin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

/**
 * Unit tests for US 03.09.01 - Admin multi-role capability.
 */
public class AdminMultiRoleTest {

    /**
     * US 03.09.01 - Test admin can act as an entrant.
     */
    @Test
    public void adminCanActAsEntrant_entrantActionsAllowed() {
        UserRoleService service = new UserRoleService();
        String adminUserId = "admin123";

        boolean canJoinWaitlist = service.canPerformAsEntrant(adminUserId);

        assertTrue(canJoinWaitlist);
    }

    /**
     * US 03.09.01 - Test admin can act as an organizer.
     */
    @Test
    public void adminCanActAsOrganizer_organizerActionsAllowed() {
        UserRoleService service = new UserRoleService();
        String adminUserId = "admin123";

        boolean canCreateEvent = service.canPerformAsOrganizer(adminUserId);

        assertTrue(canCreateEvent);
    }

    /**
     * US 03.09.01 - Test admin retains admin privileges.
     */
    @Test
    public void adminRetainsPrivileges_adminActionsAllowed() {
        UserRoleService service = new UserRoleService();
        String adminUserId = "admin123";

        boolean canDeleteEvents = service.canPerformAsAdmin(adminUserId);

        assertTrue(canDeleteEvents);
    }


    /**
     * Mock service for user role checks
     */
    static class UserRoleService {
        public boolean canPerformAsEntrant(String userId) {
            // Admin and regular users can act as entrants
            return true;
        }

        public boolean canPerformAsOrganizer(String userId) {
            // Only admin and organizers
            return userId.startsWith("admin") || userId.startsWith("organizer");
        }

        public boolean canPerformAsAdmin(String userId) {
            // Only admin
            return userId.startsWith("admin");
        }
    }
}
