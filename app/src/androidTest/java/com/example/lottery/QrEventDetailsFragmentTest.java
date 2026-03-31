package com.example.lottery;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lottery.Entrant.Activity.QrEventDetailsFragment;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for QrEventDetailsFragment.
 * Verifies UI correctly displays event data and handles user interactions.
 */
@RunWith(AndroidJUnit4.class)
public class QrEventDetailsFragmentTest {

    /**
     * Tests that all event details passed via Bundle arguments
     * are correctly displayed in the corresponding UI TextViews.
     */
    @Test
    public void qrEventDetails_displaysPassedEventData() {
        Bundle args = new Bundle();
        args.putString("eventId", "event123");
        args.putString("title", "Swimming Lessons");
        args.putString("status", "Open");
        args.putString("description", "Fun swimming event for kids");
        args.putString("location", "West Pool");
        args.putString("date", "March 14, 2026");
        args.putString("spots", "20 spots available");
        args.putString("lotteryInfo", "Lottery closes soon");
        args.putString("joinedInfo", "47 Joined");

        FragmentScenario<QrEventDetailsFragment> scenario =
                FragmentScenario.launchInContainer(
                        QrEventDetailsFragment.class,
                        args,
                        R.style.Theme_Lottery
                );

        scenario.onFragment(fragment -> {
            TextView tvEventName = fragment.requireView().findViewById(R.id.tvEventName);
            TextView tvStatusTag = fragment.requireView().findViewById(R.id.tvStatusTag);
            TextView tvDescription = fragment.requireView().findViewById(R.id.tvDescription);
            TextView tvLocation = fragment.requireView().findViewById(R.id.tvLocation);
            TextView tvEventDates = fragment.requireView().findViewById(R.id.tvEventDates);
            TextView tvTotalSpots = fragment.requireView().findViewById(R.id.tvTotalSpots);
            TextView tvWaitlist = fragment.requireView().findViewById(R.id.tvWaitlist);

            Assert.assertEquals("Swimming Lessons", tvEventName.getText().toString());
            Assert.assertEquals("Open", tvStatusTag.getText().toString());
            Assert.assertEquals("Fun swimming event for kids", tvDescription.getText().toString());
            Assert.assertEquals("West Pool", tvLocation.getText().toString());
            Assert.assertEquals("March 14, 2026", tvEventDates.getText().toString());
            Assert.assertEquals("20", tvTotalSpots.getText().toString());
            Assert.assertEquals("47", tvWaitlist.getText().toString());
        });
    }

    /**
     * Verifies that the "Show QR" button is hidden when the fragment is launched
     * in the QR scan context.
     */
    @Test
    public void qrEventDetails_hidesShowQrButton() {
        Bundle args = new Bundle();
        args.putString("eventId", "event123");
        args.putString("title", "Swimming Lessons");

        FragmentScenario<QrEventDetailsFragment> scenario =
                FragmentScenario.launchInContainer(
                        QrEventDetailsFragment.class,
                        args,
                        R.style.Theme_Lottery
                );

        scenario.onFragment(fragment -> {
            View btnShowQr = fragment.requireView().findViewById(R.id.btnShowQr);
            Assert.assertEquals(View.GONE, btnShowQr.getVisibility());
        });
    }

    /**
     * Tests that clicking the "Join Waitlist" button updates its text to "Joined"
     * and disables further interaction.
     */
    @Test
    public void qrEventDetails_joinButtonChangesAfterClick() {
        Bundle args = new Bundle();
        args.putString("eventId", "event123");
        args.putString("title", "Swimming Lessons");

        FragmentScenario<QrEventDetailsFragment> scenario =
                FragmentScenario.launchInContainer(
                        QrEventDetailsFragment.class,
                        args,
                        R.style.Theme_Lottery
                );

        scenario.onFragment(fragment -> {
            com.google.android.material.button.MaterialButton btnJoin =
                    fragment.requireView().findViewById(R.id.btnJoin);

            Assert.assertEquals("Join Waitlist", btnJoin.getText().toString());

            btnJoin.performClick();

            Assert.assertEquals("Joined", btnJoin.getText().toString());
            Assert.assertFalse(btnJoin.isEnabled());
        });
    }
}