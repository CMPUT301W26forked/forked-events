package com.example.lottery.organizer;

import org.junit.Test;
import android.widget.EditText;
import android.text.Editable;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

import com.example.lottery.SendNotificationsFragment;
import com.example.lottery.organizer.FSEventRepo;
import com.example.lottery.organizer.RepoCallback;


/**
 * sendNotification method test
 */
public class sendNotificationTest {
    @Test
    public void sendMessage_trimAndPass2Repo() {
        FSEventRepo repo = mock(FSEventRepo.class);
        EditText etmsg = mock(EditText.class);

        SendNotificationsFragment fragment = new SendNotificationsFragment();
        fragment.setRepo(repo);
        fragment.setEtmsg(etmsg);

        List<String> ids = Arrays.asList("u1", "u2");

        Editable editable = mock(Editable.class);
        when(etmsg.getText()).thenReturn(editable);
        when(editable.toString()).thenReturn("hello ");

        doAnswer(inv -> {
            RepoCallback<List<String>> cb = inv.getArgument(1);
            cb.onSuccess(ids);
            return null;
        }).when(repo).getPendingEntrantIds(any(),any());

        fragment.sendMessage();

        verify(repo).getPendingEntrantIds(any(),any());
        verify(repo).sendMessageToPending(any(), eq(ids), eq("hello"), any());
    }
}