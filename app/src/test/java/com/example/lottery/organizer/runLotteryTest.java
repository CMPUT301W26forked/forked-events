package com.example.lottery.organizer;

import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * runlottery service test
 */
public class runLotteryTest {
    @Test
    public void runlottery_markAndNotification() {
        EventRepo repo = mock(EventRepo.class);
        EventService service = new EventService(repo, null);

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(Arrays.asList("e1", "e2"));
            return null;
        }).when(repo).getWaitingEntrantIds(anyString(), any());

        doAnswer(inv -> {
            RepoCallback<Void> cb = inv.getArgument(2);
            cb.onSuccess(null);
            return null;
        }).when(repo).markEntrantSelected(anyString(), anyString(), any());

        service.runLottery("666e", "test", 2, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {

            }

            @Override
            public void onError(Exception e) {

            }
        });

        verify(repo, times(2)).markEntrantSelected(eq("666e"), anyString(), any());
        verify(repo, times(2)).createNotification(eq("666e"), anyString(), eq("test"), anyString(), any());
    }
}
