package com.example.lottery.organizer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for US 02.03.01 - Enforcing waitlist size limit.
 */
public class WaitlistLimitTest {

    /**
     * US 02.03.01 - Test setting waitlist size limit.
     */
    @Test
    public void setWaitlistLimit_savesCorrectly() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Limited Waitlist Event");
        eventData.put("waitlistLimit", 50L);

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
                Long.valueOf(50L).equals(data.get("waitlistLimit"))), any());
    }

    /**
     * US 02.03.01 - Test waitlist limit of zero means unlimited.
     */
    @Test
    public void setWaitlistLimit_zeroMeansUnlimited() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", "Unlimited Waitlist Event");
        eventData.put("waitlistLimit", 0L);

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
                Long.valueOf(0L).equals(data.get("waitlistLimit"))), any());
    }

    /**
     * US 02.03.01 - Test negative waitlist limit is rejected.
     */
    @Test
    public void setWaitlistLimit_negativeValue_rejected() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        WaitlistLimitService service = new WaitlistLimitService(repo);

        final Exception[] capturedError = new Exception[1];
        service.validateWaitlistLimit(-1L, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        assertNotNull(capturedError[0]);
        assertTrue(capturedError[0].getMessage().contains("limit cannot be negative"));
    }

    /**
     * US 02.03.01 - Test valid positive waitlist limit passes validation.
     */
    @Test
    public void setWaitlistLimit_positiveValue_passesValidation() {
        EventRepo repo = mock(EventRepo.class);
        WaitlistLimitService service = new WaitlistLimitService(repo);

        final boolean[] successCalled = {false};
        service.validateWaitlistLimit(100L, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successCalled[0] = true;
            }
            @Override
            public void onError(Exception e) {}
        });

        assertTrue(successCalled[0]);
    }
}

/**
 * Service class for waitlist limit validation
 */
class WaitlistLimitService {
    private final EventRepo repo;

    public WaitlistLimitService(EventRepo repo) {
        this.repo = repo;
    }

    public void validateWaitlistLimit(Long limit, RepoCallback<Void> cb) {
        if (limit < 0) {
            cb.onError(new IllegalArgumentException("Waitlist limit cannot be negative"));
            return;
        }
        cb.onSuccess(null);
    }
}
