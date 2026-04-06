package com.example.lottery.organizer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Date;

/**
 * Unit tests for US 02.01.04 - Setting registration period.
 */
public class RegistrationPeriodTest {

    /**
     * US 02.01.04 - Test setting valid registration period.
     */
    @Test
    public void setRegPeriod_validPeriod_savesCorrectly() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Timestamp start = new Timestamp(new Date(1700000000000L));
        Timestamp end = new Timestamp(new Date(1701000000000L));

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(3);
            cb.onSuccess(null);
            return null;
        }).when(repo).setRegStartPeriod(anyString(), any(), any(), any());

        service.setRegPeriod("event123", start, end, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).setRegStartPeriod(eq("event123"), eq(start), eq(end), any());
    }

    /**
     * US 02.01.04 - Test setting invalid registration period (start after end) fails.
     */
    @Test
    public void setRegPeriod_startAfterEnd_returnsError() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Timestamp start = new Timestamp(new Date(1701000000000L));
        Timestamp end = new Timestamp(new Date(1700000000000L));

        final Exception[] capturedError = new Exception[1];
        service.setRegPeriod("event123", start, end, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        assertNotNull(capturedError[0]);
        assertTrue(capturedError[0].getMessage().contains("start/end time"));
    }

    /**
     * US 02.01.04 - Test setting registration period with null start fails.
     */
    @Test
    public void setRegPeriod_nullStart_returnsError() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Timestamp end = new Timestamp(new Date(1701000000000L));

        final Exception[] capturedError = new Exception[1];
        service.setRegPeriod("event123", null, end, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        assertNotNull(capturedError[0]);
        assertTrue(capturedError[0].getMessage().contains("start/end required"));
    }

    /**
     * US 02.01.04 - Test setting registration period with null end fails.
     */
    @Test
    public void setRegPeriod_nullEnd_returnsError() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Timestamp start = new Timestamp(new Date(1700000000000L));

        final Exception[] capturedError = new Exception[1];
        service.setRegPeriod("event123", start, null, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        assertNotNull(capturedError[0]);
        assertTrue(capturedError[0].getMessage().contains("start/end required"));
    }

    /**
     * US 02.01.04 - Test equal start and end times fail.
     */
    @Test
    public void setRegPeriod_equalStartAndEnd_returnsError() {
        EventRepo repo = mock(EventRepo.class);
        PosterStorageService posterService = mock(PosterStorageService.class);
        EventService service = new EventService(repo, posterService);

        Timestamp timestamp = new Timestamp(new Date(1700000000000L));

        final Exception[] capturedError = new Exception[1];
        service.setRegPeriod("event123", timestamp, timestamp, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        assertNotNull(capturedError[0]);
    }
}
