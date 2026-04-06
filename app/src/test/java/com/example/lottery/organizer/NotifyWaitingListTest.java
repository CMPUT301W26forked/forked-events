package com.example.lottery.organizer;

import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for US 02.07.01 - Sending notifications to waiting list.
 */
public class NotifyWaitingListTest {

    /**
     * US 02.07.01 - Test sending notification to all waiting list entrants.
     */
    @Test
    public void notifyWaitingList_sendsToAllEntrants() {
        EventRepo repo = mock(EventRepo.class);
        NotificationService service = new NotificationService(repo);

        List<String> waitingEntrantIds = Arrays.asList("u1", "u2", "u3", "u4", "u5");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(waitingEntrantIds);
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(4);
            cb.onSuccess(null);
            return null;
        }).when(repo).createNotification(anyString(), anyString(), anyString(), anyString(), any());

        service.notifyWaitingList("event123", "Important Update", "The event time has changed.",
                new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}
                    @Override
                    public void onError(Exception e) {}
                });

        // Verify notification sent to each waiting entrant
        verify(repo, times(5)).createNotification(eq("event123"), anyString(),
                eq("event123"), eq("Important Update: The event time has changed."), any());
    }

    /**
     * US 02.07.01 - Test empty waiting list sends no notifications.
     */
    @Test
    public void notifyWaitingList_emptyList_sendsNone() {
        EventRepo repo = mock(EventRepo.class);
        NotificationService service = new NotificationService(repo);

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(Arrays.asList());
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        service.notifyWaitingList("event123", "Update", "Test message",
                new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}
                    @Override
                    public void onError(Exception e) {}
                });

        verify(repo, never()).createNotification(anyString(), anyString(),
                anyString(), anyString(), any());
    }

    /**
     * US 02.07.01 - Test notification includes event name.
     */
    @Test
    public void notifyWaitingList_includesEventName() {
        EventRepo repo = mock(EventRepo.class);
        NotificationService service = new NotificationService(repo);

        List<String> waitingEntrantIds = Arrays.asList("u1");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(waitingEntrantIds);
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        service.notifyWaitingList("event123", "Reminder", "Don't forget!",
                new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}
                    @Override
                    public void onError(Exception e) {}
                });

        verify(repo).createNotification(anyString(), anyString(),
                eq("event123"), contains("Reminder"), any());
    }

    /**
     * Service class for notifications
     */
    static class NotificationService {
        private final EventRepo repo;

        public NotificationService(EventRepo repo) {
            this.repo = repo;
        }

        public void notifyWaitingList(String eventId, String title, String message,
                                      RepoCallback<Void> callback) {
            repo.getWaitingEntrantIds(eventId, new RepoCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> entrantIds) {
                    if (entrantIds.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    String fullMessage = title + ": " + message;
                    for (String entrantId : entrantIds) {
                        repo.createNotification(eventId, entrantId, eventId, fullMessage,
                                new RepoCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {}
                                    @Override
                                    public void onError(Exception e) {}
                                });
                    }
                    callback.onSuccess(null);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        }
    }
}
