package com.example.lottery;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lottery.R;
import com.example.lottery.admin.AdminRemoveProfileFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Basic UI tests for admin profile deletion flow.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminProfileDeleteTest {

    /**
     * Helper method to create fragment arguments.
     */
    private Bundle createArgs() {
        Bundle args = new Bundle();
        args.putString("user_id", "testUser123");
        args.putString("name", "Vidhi");
        args.putString("email", "vidhi@example.com");
        args.putString("phone", "1234567890");
        args.putString("role", "entrant");
        args.putString("profile_picture_uri", "");
        return args;
    }

    /**
     * Tests that the fragment displays the passed profile information correctly.
     */
    @Test
    public void fragmentDisplaysPassedProfileData() {
        FragmentScenario<AdminRemoveProfileFragment> scenario =
                launchInContainer(AdminRemoveProfileFragment.class, createArgs(), R.style.Theme_Lottery, (FragmentFactory) null);

        onView(withId(R.id.tvProfileName)).check(matches(withText("Vidhi")));
        onView(withId(R.id.tvProfileUid)).check(matches(withText("testUser123")));
        onView(withId(R.id.tvProfileEmail)).check(matches(withText("vidhi@example.com")));
        onView(withId(R.id.tvProfilePhone)).check(matches(withText("1234567890")));
        onView(withId(R.id.tvProfileRole)).check(matches(withText("entrant")));
    }

    /**
     * Tests that default fallback text is shown when profile fields are empty.
     */
    @Test
    public void fragmentDisplaysFallbackTextWhenDataMissing() {
        Bundle args = new Bundle();
        args.putString("user_id", "");
        args.putString("name", "");
        args.putString("email", "");
        args.putString("phone", "");
        args.putString("role", "");
        args.putString("profile_picture_uri", "");

        FragmentScenario<AdminRemoveProfileFragment> scenario =
                launchInContainer(AdminRemoveProfileFragment.class, args, R.style.Theme_Lottery, (FragmentFactory) null);

        onView(withId(R.id.tvProfileName)).check(matches(withText("Unnamed User")));
        onView(withId(R.id.tvProfileUid)).check(matches(withText("Unknown ID")));
        onView(withId(R.id.tvProfileEmail)).check(matches(withText("No email")));
        onView(withId(R.id.tvProfilePhone)).check(matches(withText("No phone")));
        onView(withId(R.id.tvProfileRole)).check(matches(withText("entrant")));
    }

    /**
     * Tests that clicking the remove button opens the confirmation dialog.
     */
    @Test
    public void clickingRemoveButtonShowsConfirmationDialog() {
        FragmentScenario<AdminRemoveProfileFragment> scenario =
                launchInContainer(AdminRemoveProfileFragment.class, createArgs(), R.style.Theme_Lottery, (FragmentFactory) null);

        onView(withId(R.id.btnConfirmRemoveProfile)).perform(click());

        onView(withText("Remove profile"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText("This will permanently delete the profile."))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the cancel button on the dialog is visible after opening it.
     */
    @Test
    public void confirmationDialogShowsCancelButton() {
        FragmentScenario<AdminRemoveProfileFragment> scenario =
                launchInContainer(AdminRemoveProfileFragment.class, createArgs(), R.style.Theme_Lottery, (FragmentFactory) null);

        onView(withId(R.id.btnConfirmRemoveProfile)).perform(click());

        onView(withText("Cancel"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText("Confirm"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }
}
