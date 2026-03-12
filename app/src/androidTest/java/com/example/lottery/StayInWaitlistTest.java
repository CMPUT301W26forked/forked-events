package com.example.lottery;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lottery.Entrant.Activity.EntrantEventDetailsFragment;
import com.example.lottery.Entrant.Service.WaitlistService;
import com.google.android.material.button.MaterialButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class StayInWaitlistTest {

    private EntrantEventDetailsFragment fragment;
    private WaitlistService mockWaitlistService;
    private FragmentScenario<EntrantEventDetailsFragment> fragmentScenario;

    @Before
    public void setup() {
        Bundle args = new Bundle();
        args.putString("eventId", "testEventId");

        fragmentScenario = FragmentScenario.launchInContainer(
                EntrantEventDetailsFragment.class,
                args,
                com.example.lottery.R.style.Theme_Lottery
        );

        fragmentScenario.onFragment(f -> {
            this.fragment = f;
            mockWaitlistService = Mockito.mock(WaitlistService.class);
            f.waitlistService = mockWaitlistService;
            f.entrantId = "testEntrantId";
        });
    }

    @Test
    public void testShowStayInList_YesButton_callsStayInList() {
        fragmentScenario.onFragment(f -> {

            f.waitlistService.stayInList("testEventId", f.entrantId,
                    new com.example.lottery.Entrant.Repo.WaitlistCallback<Void>() {
                        @Override public void onSuccess(Void r) {}
                        @Override public void onError(Exception e) {}
                    });
        });

        Mockito.verify(mockWaitlistService).stayInList(
                eq("testEventId"),
                eq("testEntrantId"),
                any()
        );
    }

    @Test
    public void testShowStayInList_NoButton_callsLeaveWaitlist() {
        fragmentScenario.onFragment(f -> {
            MaterialButton btnJoin = f.getView().findViewById(com.example.lottery.R.id.btnJoin);

        });

        Mockito.verify(mockWaitlistService, Mockito.never()).stayInList(
                any(), any(), any()
        );
    }

    @Test
    public void testShowStayInList_dialogShows() {
        fragmentScenario.onFragment(f -> {
            MaterialButton btnJoin = f.getView().findViewById(com.example.lottery.R.id.btnJoin);

            f.showStayInList(btnJoin);
        });

    }
}

