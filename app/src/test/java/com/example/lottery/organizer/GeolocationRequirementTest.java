package com.example.lottery.organizer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for US 02.02.03 - Enabling/disabling geolocation requirements.
 */
public class GeolocationRequirementTest {

    /**
     * US 02.02.03 - Test enabling geolocation requirement.
     */
    @Test
    public void enableGeolocationRequirement_savesCorrectly() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Event with Geo");
        eventData.put("requiresGeolocation", true);

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

        verify(repo).saveEvent(eq("event123"), argThat(data ->
                Boolean.TRUE.equals(data.get("requiresGeolocation"))), any());
    }

    /**
     * US 02.02.03 - Test disabling geolocation requirement.
     */
    @Test
    public void disableGeolocationRequirement_savesCorrectly() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Event without Geo");
        eventData.put("requiresGeolocation", false);

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
                Boolean.FALSE.equals(data.get("requiresGeolocation"))), any());
    }

    /**
     * US 02.02.03 - Test geolocation flag is included in event data.
     */
    @Test
    public void saveEvent_includesGeolocationFlag() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Test Event");
        eventData.put("requiresGeolocation", true);
        eventData.put("geolocationRadius", 100); // meters

        final boolean[] callbackCalled = {false};
        service.saveEvent("event789", eventData, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callbackCalled[0] = true;
            }
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).saveEvent(eq("event789"), argThat(data -> {
            return Boolean.TRUE.equals(data.get("requiresGeolocation")) &&
                   Integer.valueOf(100).equals(data.get("geolocationRadius"));
        }), any());
    }
}
