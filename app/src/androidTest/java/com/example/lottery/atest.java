package com.example.lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lottery.Entrant.Activity.QrEventDetailsFragment;
import com.example.lottery.Entrant.Activity.QrScannerFragment;
import com.example.lottery.Entrant.Model.EntrantProfile;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class atest {

    //@Test
    //public void qrEventDetails_displaysPassedBundleData() {
        //Bundle bundle = new Bundle();
        //bundle.putString("title", "Swimming Lessons - Kids");
        //bundle.putString("status", "Open");
        //bundle.putString("description", "Fun swimming lessons");
        //bundle.putString("location", "West Side Pool");
        //bundle.putString("date", "3/14/2026 - 5/14/2026");
        //bundle.putString("spots", "20 spots available");
        //bundle.putString("lotteryInfo", "Waitlist Open");
        //bundle.putString("joinedInfo", "47 Joined");

        //FragmentScenario<QrEventDetailsFragment> scenario =
                //FragmentScenario.launchInContainer(QrEventDetailsFragment.class);

        //onView(withId(R.id.tvTitle)).check(matches(withText("Swimming Lessons - Kids")));
        //onView(withId(R.id.tvStatusTag)).check(matches(withText("Open")));
       //onView(withId(R.id.tvDescription)).check(matches(withText("Fun swimming lessons")));
        //onView(withId(R.id.tvLocation)).check(matches(withText("West Side Pool")));
        //onView(withId(R.id.tvEventDates)).check(matches(withText("3/14/2026 - 5/14/2026")));
        //onView(withId(R.id.tvTotalSpots)).check(matches(withText("20 spots available")));
        //onView(withId(R.id.lotterySection)).check(matches(withText("Waitlist Open")));
        //onView(withId(R.id.btnJoin)).check(matches(withText("47 Joined")));
    //}

    //@Test
    //public void qrScanner_initialViews_displayCorrectly() {
        //FragmentScenario.launchInContainer(QrScannerFragment.class);

        //onView(withId(R.id.tvScanTitle)).check(matches(withText("Scan Event QR")));
        //onView(withId(R.id.btnStartScanning)).check(matches(isDisplayed()));
        //onView(withId(R.id.barcodeScanner))
                //.check(matches(withEffectiveVisibility(GONE)));
    //}

    @Test
    public void entrantProfile_constructorAndGetters_workCorrectly() {
        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("event1");
        eventIds.add("event2");

        EntrantProfile profile = new EntrantProfile(
                "u1",
                "Vidhi",
                "vidhi@email.com",
                "1234567890",
                eventIds
        );

        org.junit.Assert.assertEquals("u1", profile.getId());
        org.junit.Assert.assertEquals("Vidhi", profile.getName());
        org.junit.Assert.assertEquals("vidhi@email.com", profile.getEmail());
        org.junit.Assert.assertEquals("1234567890", profile.getPhone());
        org.junit.Assert.assertEquals(2, profile.getRegisteredEventIds().size());
    }

    @Test
    public void entrantProfile_setters_workCorrectly() {
        EntrantProfile profile = new EntrantProfile();

        ArrayList<String> eventIds = new ArrayList<>();
        eventIds.add("event123");

        profile.setId("u2");
        profile.setName("Test User");
        profile.setEmail("test@email.com");
        profile.setPhone("9999999999");
        profile.setRegisteredEventIds(eventIds);

        org.junit.Assert.assertEquals("u2", profile.getId());
        org.junit.Assert.assertEquals("Test User", profile.getName());
        org.junit.Assert.assertEquals("test@email.com", profile.getEmail());
        org.junit.Assert.assertEquals("9999999999", profile.getPhone());
        org.junit.Assert.assertEquals("event123", profile.getRegisteredEventIds().get(0));
    }
}