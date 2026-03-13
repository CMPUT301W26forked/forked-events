package com.example.lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lottery.Entrant.Activity.QrDisplayFragment;
import com.example.lottery.Entrant.Activity.QrScannerFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QrBasicTest {

    @Test
    public void testQrDisplayFragmentLoads() {
        Bundle args = new Bundle();
        args.putString("eventId", "test_event_001");

        FragmentScenario<QrDisplayFragment> scenario =
                FragmentScenario.launchInContainer(
                        QrDisplayFragment.class,
                        args
                );

        onView(withId(R.id.ivQrCode))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testQrScannerFragmentLoads() {
        FragmentScenario<QrScannerFragment> scenario =
                FragmentScenario.launchInContainer(
                        QrScannerFragment.class,
                        null,
                        com.example.lottery.R.style.Theme_Lottery,
                        null
                );

        onView(withId(R.id.barcodeScanner))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnStartScanning))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testQrDisplayBackButtonVisible() {
        Bundle args = new Bundle();
        args.putString("eventId", "test_event_001");

        FragmentScenario<QrDisplayFragment> scenario =
                FragmentScenario.launchInContainer(
                        QrDisplayFragment.class,
                        args
                );

        onView(withId(R.id.btnBack))
                .check(matches(isDisplayed()));
    }
}