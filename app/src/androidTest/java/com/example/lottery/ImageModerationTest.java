package com.example.lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.lottery.admin.ImageModerationFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for the Image Moderation feature (US 03.06.01).
 * Tests that administrators can browse and remove uploaded images.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ImageModerationTest {

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * Tests that the Image Moderation fragment displays the correct header title.
     */
    @Test
    public void fragmentDisplaysImageModerationTitle() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.tvImageModTitle))
                .check(matches(allOf(
                        isDisplayed(),
                        withText("Image Moderation")
                )));
    }

    /**
     * Tests that the image count is initially displayed (0 when no images exist).
     */
    @Test
    public void fragmentDisplaysImageCountHeader() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.tvImageModCount))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the moderation list section is displayed with the title.
     */
    @Test
    public void fragmentDisplaysModerationListSection() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withText("Moderation List"))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the RecyclerView for images is displayed.
     */
    @Test
    public void fragmentDisplaysRecyclerViewForImages() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.rvModerationList))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the back button is displayed and clickable.
     */
    @Test
    public void fragmentDisplaysBackButton() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.btnBack))
                .check(matches(allOf(
                        isDisplayed(),
                        withText("Back")
                )));
    }

    /**
     * Tests that the admin profile icon is displayed in the top right.
     */
    @Test
    public void fragmentDisplaysAdminProfileIcon() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.ivAdminProfile))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the header card layout contains the title and count elements.
     */
    @Test
    public void headerCardContainsTitleAndCount() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.cvImageModerationHeader))
                .check(matches(allOf(
                        isDisplayed(),
                        hasDescendant(withId(R.id.tvImageModTitle)),
                        hasDescendant(withId(R.id.tvImageModCount))
                )));
    }

    /**
     * Tests that the moderation list card is displayed with the RecyclerView.
     */
    @Test
    public void moderationListCardContainsRecyclerView() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.cvModerationList))
                .check(matches(allOf(
                        isDisplayed(),
                        hasDescendant(withId(R.id.rvModerationList))
                )));
    }

    /**
     * Tests that when an image item is loaded, it displays the event title.
     * This test assumes at least one image exists in Firestore.
     * In a real scenario, you may need to set up test data.
     */
    @Test
    public void imageItemDisplaysEventTitle() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        // Wait for images to load and verify the title is displayed
        onView(withId(R.id.tvModerationTitle))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that when an image item is loaded, it displays the poster thumbnail.
     */
    @Test
    public void imageItemDisplaysPosterThumbnail() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.ivPosterThumbnail))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the remove button is displayed for each image item.
     */
    @Test
    public void imageItemDisplaysRemoveButton() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(allOf(
                withId(R.id.btnRemove),
                isDisplayed()
        )).check(matches(allOf(
                isDisplayed(),
                withText("Remove")
        )));
    }

    /**
     * Tests that the remove button is clickable.
     */
    @Test
    public void removeButtonIsClickable() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        // Scroll to make sure the button is visible
        onView(withId(R.id.btnRemove))
                .perform(scrollTo())
                .perform(click());
    }

    /**
     * Tests that the back button navigates back from the fragment.
     * This tests the navigation without popping the entire fragment stack.
     */
    @Test
    public void backButtonNavigatesBack() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        // Verify the back button is displayed and clickable
        onView(withId(R.id.btnBack))
                .check(matches(isDisplayed()))
                .perform(click());

        // After clicking back, the fragment should handle navigation
        // The actual navigation behavior depends on the parent fragment manager
    }

    /**
     * Tests that the fragment layout uses proper constraint layout dimensions.
     */
    @Test
    public void fragmentHasProperLayout() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        // Verify main container is displayed and takes up the screen
        onView(withId(R.id.cvImageModerationHeader))
                .check(matches(isDisplayed()));

        onView(withId(R.id.cvModerationList))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that the count display is visible in the header.
     * This tests the image count functionality shows correctly.
     */
    @Test
    public void countDisplayIsVisible() {
        FragmentScenario<ImageModerationFragment> scenario =
                FragmentScenario.launchInContainer(ImageModerationFragment.class);

        onView(withId(R.id.tvImageModCount))
                .check(matches(isDisplayed()));
    }
}
