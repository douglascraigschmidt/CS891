package edu.vanderbilt.webcrawler.ui

import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import edu.vanderbilt.webcrawler.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewActivityTest {
    @JvmField
    @Rule
    var mActivityTestRule = ActivityTestRule(WebViewActivity::class.java, false, false)

    @Test
    fun webViewActivityTest() {
        val url = "https://www.vanderbilt.edu/"
        val intent = Intent()
        intent.putExtra(WebViewActivity.KEY_URL_LIST, url)
        mActivityTestRule.launchActivity(intent)

        // The search button should be displayed (search view iconified).
        val searchButton =
                onView(allOf(withId(R.id.search_button),
                        withContentDescription("Search"),
                        childAtPosition(
                                allOf(withId(R.id.search_bar),
                                        childAtPosition(
                                                withId(R.id.searchView),
                                                0)),
                                1),
                        isDisplayed()))

        // The the title should be displaying the url.
        matchToolbarTitle(url).check(matches(isDisplayed()))

        // Expand the search view.
        searchButton.perform(click())

        // The the title should no longer be displayed.
        onView(allOf(isAssignableFrom(TextView::class.java),
                childAtPosition(withId(R.id.toolbar), 1),
                not(isDisplayed())))

        // The search view should be expanded and showing the current url.
        onView(allOf(withId(R.id.search_src_text),
                withText(url),
                childAtPosition(
                        allOf(withId(R.id.search_plate),
                                childAtPosition(
                                        withId(R.id.search_edit_frame),
                                        1)),
                        0),
                isDisplayed()))

        // The close button should be visible.
        val closeButton =
                onView(allOf(withId(R.id.search_close_btn),
                        withContentDescription("Clear query"),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                1),
                        isDisplayed()))

        // Click on the close button to clear the search text.
        closeButton.perform(click())

        // The search text should be cleared and be showing the search hint.
        onView(allOf(withId(R.id.search_src_text),
                withText(isEmptyOrNullString()),
                withHint(R.string.search_view_hint),
                childAtPosition(
                        allOf(withId(R.id.search_plate),
                                childAtPosition(
                                        withId(R.id.search_edit_frame),
                                        1)),
                        0),
                isDisplayed()))

        // Click the close button again to iconify the search view.
        closeButton.perform(click())

        // The close button should not be visible.
        onView(allOf(withId(R.id.search_close_btn),
                withContentDescription("Clear query"),
                childAtPosition(
                        allOf(withId(R.id.search_plate),
                                childAtPosition(
                                        withId(R.id.search_edit_frame),
                                        1)),
                        1),
                not(isDisplayed())))

        // The search view button should now be displayed again (search view iconified).
        onView(allOf(withId(R.id.search_button),
                withContentDescription("Search"),
                childAtPosition(
                        allOf(withId(R.id.search_bar),
                                childAtPosition(
                                        withId(R.id.searchView),
                                        0)),
                        1),
                isDisplayed()))

        // Finally, the title be visible and showing the url again.
        matchToolbarTitle(url).check(matches(isDisplayed()))
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    private fun matchToolbarTitle(
            title: CharSequence): ViewInteraction {
        return onView(isAssignableFrom(Toolbar::class.java))
                .check(matches(withToolbarTitle(`is`(title))))
    }

    private fun withToolbarTitle(
            textMatcher: Matcher<CharSequence>): Matcher<Any> {
        return object : BoundedMatcher<Any, Toolbar>(Toolbar::class.java!!) {
            public override fun matchesSafely(toolbar: Toolbar): Boolean {
                return textMatcher.matches(toolbar.title)
            }

            override fun describeTo(description: Description) {
                description.appendText("with toolbar title: ")
                textMatcher.describeTo(description)
            }
        }
    }
}
