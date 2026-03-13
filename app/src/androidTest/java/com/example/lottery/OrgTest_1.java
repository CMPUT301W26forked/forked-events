package com.example.lottery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrgTest_1 {
    // test
    @Test
    public void organizerFragment_RecyclerViewAndButton() {
        FragmentScenario<OrganizerFragment> scenario = FragmentScenario.launchInContainer(
                OrganizerFragment.class,
                null,
                R.style.Theme_Lottery
        );

        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView());
            assertNotNull(fragment.getView().findViewById(R.id.btnCreateEvent));

            RecyclerView recyclerView = fragment.getView().findViewById(R.id.rvOrganizerEvents);
            assertNotNull(recyclerView);
            assertTrue(recyclerView.getAdapter() instanceof OrganizerAdapter);
            assertNotNull(recyclerView.getLayoutManager());
        });
    }

    @Test
    public void notificationsFragment_RecyclerViewAndButton() {
        FragmentScenario<NotificationsFragment> scenario = FragmentScenario.launchInContainer(
                NotificationsFragment.class,
                null,
                R.style.Theme_Lottery
        );

        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView());
            assertNotNull(fragment.getView().findViewById(R.id.btnBack));

            RecyclerView recyclerView = fragment.getView().findViewById(R.id.rvNotifications);
            assertNotNull(recyclerView);
            assertTrue(recyclerView.getAdapter() instanceof NotificationsAdapter);
            assertNotNull(recyclerView.getLayoutManager());
        });
    }

}