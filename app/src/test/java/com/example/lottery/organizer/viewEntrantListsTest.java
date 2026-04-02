package com.example.lottery.organizer;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for organizer viewing entrant lists.
 *
 * US 02.02.01 - View List of Entrants Who Joined Organizers Waiting List
 * US 02.06.01 - View a List of All Invited (Pending List)
 * US 02.06.03 - View a Final List of Entrants Who Enrolled for the Event
 * US 02.06.04 - View a List of Entrants Who Did Not Sign Up (Cancelled List)
 */
public class viewEntrantListsTest {

    /**
     * US 02.02.01 - Organizer requests the waiting list and receives the correct entrant IDs.
     */
    @Test
    public void getWaitlist_returnsWaitlistedEntrantIds() {
        EventRepo repo = mock(EventRepo.class);
        OrganizerListService service = new OrganizerListService(repo);

        List<String> expected = Arrays.asList("u1", "u2", "u3");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(expected);
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        final List<String>[] result = new List[1];
        service.getWaitlist("event42", new RepoCallback<List<String>>() {
            @Override public void onSuccess(List<String> ids) { result[0] = ids; }
            @Override public void onError(Exception e) {}
        });

        verify(repo).getWaitingEntrantIds(eq("event42"), any());
        assertEquals(expected, result[0]);
    }

    /**
     * US 02.06.01 - Organizer requests the pending (invited) list and receives the correct entrant IDs.
     */
    @Test
    public void getPendingList_returnsInvitedEntrantIds() {
        EventRepo repo = mock(EventRepo.class);
        OrganizerListService service = new OrganizerListService(repo);

        List<String> expected = Arrays.asList("u4", "u5");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(expected);
            return null;
        }).when(repo).getPendingEntrantIds(anyString(), any());

        final List<String>[] result = new List[1];
        service.getPendingList("event42", new RepoCallback<List<String>>() {
            @Override public void onSuccess(List<String> ids) { result[0] = ids; }
            @Override public void onError(Exception e) {}
        });

        verify(repo).getPendingEntrantIds(eq("event42"), any());
        assertEquals(expected, result[0]);
    }

    /**
     * US 02.06.03 - Organizer requests the final enrolled list and receives the correct entrant IDs.
     */
    @Test
    public void getEnrolledList_returnsFinalEnrolledEntrantIds() {
        EventRepo repo = mock(EventRepo.class);
        OrganizerListService service = new OrganizerListService(repo);

        List<String> expected = Arrays.asList("u6", "u7", "u8");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(expected);
            return null;
        }).when(repo).getRegisteredEntrantIds(anyString(), any());

        final List<String>[] result = new List[1];
        service.getEnrolledList("event42", new RepoCallback<List<String>>() {
            @Override public void onSuccess(List<String> ids) { result[0] = ids; }
            @Override public void onError(Exception e) {}
        });

        verify(repo).getRegisteredEntrantIds(eq("event42"), any());
        assertEquals(expected, result[0]);
    }

    /**
     * US 02.06.04 - Organizer requests the cancelled list and receives the correct entrant IDs.
     */
    @Test
    public void getCancelledList_returnsCancelledEntrantIds() {
        EventRepo repo = mock(EventRepo.class);
        OrganizerListService service = new OrganizerListService(repo);

        List<String> expected = Arrays.asList("u9", "u10");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(expected);
            return null;
        }).when(repo).getCancelledEntrantIds(anyString(), any());

        final List<String>[] result = new List[1];
        service.getCancelledList("event42", new RepoCallback<List<String>>() {
            @Override public void onSuccess(List<String> ids) { result[0] = ids; }
            @Override public void onError(Exception e) {}
        });

        verify(repo).getCancelledEntrantIds(eq("event42"), any());
        assertEquals(expected, result[0]);
    }
}
