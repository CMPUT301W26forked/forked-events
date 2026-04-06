package com.example.lottery.organizer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.net.Uri;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for creating events.
 *
 * US 02.01.01 - Create an event with event details
 * US 02.01.02 - Create a public event
 * US 02.01.03 - Create a private event with invited entrants
 */
public class CreateEventTest {

    /**
     * US 02.01.01 - Test creating an event with all required details.
     */
    @Test
    public void createEvent_withDetails_savesCorrectly() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Test Event");
        eventData.put("description", "A test event description");
        eventData.put("location", "Edmonton");
        eventData.put("totalSpots", 100L);

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).saveEvent(anyString(), any(), any());

        service.saveEvent("event123", eventData, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).saveEvent(eq("event123"), eq(eventData), any());
    }

    /**
     * US 02.01.02 - Test creating a public event (isPrivate = false).
     */
    @Test
    public void createEvent_publicEvent_savesWithCorrectPrivacy() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Public Event");
        eventData.put("isPrivate", false);

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).saveEvent(anyString(), any(), any());

        service.saveEvent("event456", eventData, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).saveEvent(eq("event456"), argThat(data ->
                Boolean.FALSE.equals(data.get("isPrivate"))), any());
    }

    /**
     * US 02.01.03 - Test creating a private event (isPrivate = true).
     */
    @Test
    public void createEvent_privateEvent_savesWithCorrectPrivacy() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Private Event");
        eventData.put("isPrivate", true);
        eventData.put("invitedEntrants", java.util.Arrays.asList("u1", "u2", "u3"));

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).saveEvent(anyString(), any(), any());

        service.saveEvent("event789", eventData, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).saveEvent(eq("event789"), argThat(data ->
                Boolean.TRUE.equals(data.get("isPrivate"))),
                any());
    }

    /**
     * US 02.01.03 - Test linking event to organizer after creation.
     */
    @Test
    public void createEventWithOrganizer_linksEventToUser() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Linked Event");

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).saveEvent(anyString(), any(), any());

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(3);
            cb.onSuccess(null);
            return null;
        }).when(repo).linkEventToOrganizer(anyString(), anyString(), anyString(), any());

        service.saveEventWithOrganizer("organizer1", "event101", eventData,
                new RepoCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}
                    @Override
                    public void onError(Exception e) {}
                });

        verify(repo).linkEventToOrganizer(eq("organizer1"), eq("event101"),
                eq("Linked Event"), any());
    }
}
