package edu.vanderbilt.webcrawler.ui

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import edu.vanderbilt.webcrawler.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Rule
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {
        val floatingActionButton = onView(
                allOf(withId(R.id.searchFab),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()))
        floatingActionButton.perform(click())

        val appCompatImageView = onView(
                allOf(withId(R.id.search_button), withContentDescription("Search"),
                        childAtPosition(
                                allOf(withId(R.id.search_bar),
                                        childAtPosition(
                                                withId(R.id.searchView),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageView.perform(click())

        val searchAutoComplete = onView(
                allOf(withId(R.id.search_src_text),
                        withText(
                                "https://news.vanderbilt"
                                        + ".edu/vanderbiltmagazine/mogul-in-the-making-charles-d"
                                        + "-kings-entertainment-career-is-turning-out-just-the-way"
                                        + "-he-scripted-it/"),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()))
        searchAutoComplete.perform(replaceText("cats"))

        val searchAutoComplete2 = onView(
                allOf(withId(R.id.search_src_text), withText("cats"),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()))
        searchAutoComplete2.perform(closeSoftKeyboard())

        val searchAutoComplete3 = onView(
                allOf(withId(R.id.search_src_text),
                        withText(
                                "https://news.vanderbilt"
                                        + ".edu/vanderbiltmagazine/mogul-in-the-making-charles-d"
                                        + "-kings-entertainment-career-is-turning-out-just-the-way"
                                        + "-he-scripted-it/"),
                        childAtPosition(
                                allOf(withId(R.id.search_plate),
                                        childAtPosition(
                                                withId(R.id.search_edit_frame),
                                                1)),
                                0),
                        isDisplayed()))

        val appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`(
                                                        "android.support.design.widget" + ".AppBarLayout")),
                                                0)),
                                2),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val floatingActionButton2 = onView(
                allOf(withId(R.id.copyFab),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()))
        floatingActionButton2.perform(click())

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
}
