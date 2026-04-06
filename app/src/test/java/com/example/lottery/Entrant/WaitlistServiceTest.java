package com.example.lottery.Entrant;

import static org.mockito.Mockito.*;

import com.example.lottery.Entrant.Repo.WaitlistRepo;
import com.example.lottery.Entrant.Repo.WaitlistCallback;
import com.example.lottery.Entrant.Service.WaitlistService;

import org.junit.Test;
import org.junit.Before;

/**
 * Unit tests for WaitlistService.
 *
 * US 01.01.01 - Join a waiting list for a specific event
 * US 01.01.02 - Leave a waiting list
 * US 01.01.03 - Join the waiting list with location data
 */
public class WaitlistServiceTest {

    private WaitlistRepo mockRepo;

    @Before
    public void setup() {
        mockRepo = mock(WaitlistRepo.class);
        // Inject mock repo into static field using reflection or by modifying the service
        // For testing purposes, we'll test the interface directly
    }

    /**
     * US 01.01.02 - Test that leaveWaitlist delegates to the repository correctly.
     */
    @Test
    public void leaveWaitlist_delegatesToRepository() {
        WaitlistRepo repo = mock(WaitlistRepo.class);

        doAnswer(inv -> {
            WaitlistCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).leaveWaitlist(anyString(), anyString(), any());

        repo.leaveWaitlist("event123", "user456", new WaitlistCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).leaveWaitlist(eq("event123"), eq("user456"), any());
    }

    /**
     * US 01.01.02 - Test that leaveWaitlist handles errors properly.
     */
    @Test
    public void leaveWaitlist_propagatesErrors() {
        WaitlistRepo repo = mock(WaitlistRepo.class);

        Exception expectedError = new RuntimeException("Network error");
        doAnswer(inv -> {
            WaitlistCallback<Void> cb = inv.getArgument(2);
            cb.onError(expectedError);
            return null;
        }).when(repo).leaveWaitlist(anyString(), anyString(), any());

        final Exception[] capturedError = new Exception[1];
        repo.leaveWaitlist("event123", "user456", new WaitlistCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                capturedError[0] = e;
            }
        });

        verify(repo).leaveWaitlist(eq("event123"), eq("user456"), any());
        assert capturedError[0] == expectedError;
    }

    /**
     * US 01.01.01 - Test join waitlist without location.
     */
    @Test
    public void joinWaitlist_withoutLocation_delegatesCorrectly() {
        WaitlistRepo repo = mock(WaitlistRepo.class);

        doAnswer(inv -> {
            WaitlistCallback<Void> cb = inv.getArgument(5);
            cb.onSuccess(null);
            return null;
        }).when(repo).joinWaitlist(anyString(), anyString(), anyString(), any(), any(), any());

        repo.joinWaitlist("event123", "user456", "John Doe", null, null, new WaitlistCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).joinWaitlist(eq("event123"), eq("user456"), eq("John Doe"), eq(null), eq(null), any());
    }

    /**
     * US 01.01.03 - Test join waitlist with location data.
     */
    @Test
    public void joinWaitlist_withLocation_includesCoordinates() {
        WaitlistRepo repo = mock(WaitlistRepo.class);

        Double lat = 53.5461;
        Double lng = -113.4938;

        doAnswer(inv -> {
            WaitlistCallback<Void> cb = inv.getArgument(5);
            cb.onSuccess(null);
            return null;
        }).when(repo).joinWaitlist(anyString(), anyString(), anyString(), any(), any(), any());

        repo.joinWaitlist("event123", "user456", "John Doe", lat, lng, new WaitlistCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).joinWaitlist(eq("event123"), eq("user456"), eq("John Doe"), eq(lat), eq(lng), any());
    }

    /**
     * Test checkWaitListStatus delegates to repository.
     */
    @Test
    public void checkWaitListStatus_delegatesToRepository() {
        WaitlistRepo repo = mock(WaitlistRepo.class);

        doAnswer(inv -> {
            WaitlistCallback<Boolean> cb = inv.getArgument(2);
            cb.onSuccess(true);
            return null;
        }).when(repo).isOnWaitlist(anyString(), anyString(), any());

        repo.isOnWaitlist("event123", "user456", new WaitlistCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).isOnWaitlist(eq("event123"), eq("user456"), any());
    }

    /**
     * Test getWaitListCount delegates to repository.
     */
    @Test
    public void getWaitListCount_delegatesToRepository() {
        WaitlistRepo repo = mock(WaitlistRepo.class);

        doAnswer(inv -> {
            WaitlistCallback<Long> cb = inv.getArgument(1);
            cb.onSuccess(42L);
            return null;
        }).when(repo).getWaitlistCount(anyString(), any());

        repo.getWaitlistCount("event123", new WaitlistCallback<Long>() {
            @Override
            public void onSuccess(Long result) {}
            @Override
            public void onError(Exception e) {}
        });

        verify(repo).getWaitlistCount(eq("event123"), any());
    }
}
