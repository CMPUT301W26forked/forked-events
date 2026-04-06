package com.example.lottery.organizer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for US 02.05.03 - Drawing a replacement applicant.
 */
public class ReplacementDrawTest {

    /**
     * US 02.05.03 - Test drawing a replacement when someone cancels.
     */
    @Test
    public void drawReplacement_selectsFromRemainingWaitlist() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        // Simulating waitlist after one entrant cancelled
        List<String> waitingEntrants = Arrays.asList("u3", "u4", "u5");
        String cancelledEntrantId = "u1";

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(waitingEntrants);
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).markEntrantSelected(anyString(), anyString(), any());

        service.runLottery("event123", "Test Event", 1, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).getWaitingEntrantIds(eq("event123"), any());
        verify(repo, times(1)).markEntrantSelected(eq("event123"), anyString(), any());
    }

    /**
     * US 02.05.03 - Test replacement draw when no one is on waitlist.
     */
    @Test
    public void drawReplacement_noWaitlistEntrants_returnsError() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(Arrays.asList()); // Empty waitlist
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        final Exception[] capturedError = new Exception[1];
        service.runLottery("event123", "Test Event", 1, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        assertNotNull(capturedError[0]);
        assertTrue(capturedError[0].getMessage().contains("No waiting entrants"));
    }

    /**
     * US 02.05.03 - Test replacement draw selects exactly one entrant.
     */
    @Test
    public void drawReplacement_selectsExactlyOne() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        List<String> waitingEntrants = Arrays.asList("u1", "u2", "u3", "u4", "u5");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(waitingEntrants);
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).markEntrantSelected(anyString(), anyString(), any());

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(4);
            cb.onSuccess(null);
            return null;
        }).when(repo).createNotification(anyString(), anyString(), anyString(), anyString(), any());

        service.runLottery("event123", "Test Event", 1, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        // Should mark exactly 1 entrant as selected
        verify(repo, times(1)).markEntrantSelected(eq("event123"), anyString(), any());
    }

    /**
     * US 02.05.03 - Test replacement draw sends notification to selected entrant.
     */
    @Test
    public void drawReplacement_sendsNotification() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        List<String> waitingEntrants = Arrays.asList("u1", "u2");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(waitingEntrants);
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).markEntrantSelected(anyString(), anyString(), any());

        service.runLottery("event123", "Test Event", 1, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).createNotification(eq("event123"), anyString(),
                eq("Test Event"), contains("selected"), any());
    }
}
