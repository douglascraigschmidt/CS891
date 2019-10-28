package common

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.common.truth.Truth.assertThat
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.platform.AndroidPlatform
import edu.vanderbilt.crawler.ui.screens.main.MainActivity
import edu.vanderbilt.crawler.ui.screens.settings.Settings
import edu.vanderbilt.crawler.viewmodels.MainViewModel.CrawlState.*
import edu.vanderbilt.imagecrawler.transforms.Transform
import kotlinx.coroutines.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
open class InstrumentedTests {
    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    /**
     * Note that the Settings.crawlerType is set by invoking Suite class
     */
    @Before
    fun before() {
        AndroidPlatform.cache.clear()

        with(Settings) {
            transformTypes = Transform.Type.values().toList()
            crawlDepth = 3
            crawlSpeed = DEFAULT_CRAWL_SPEED
            viewScale = 10
            showProgress = true
            showState = true
            showSize = true
            showThread = true
            localCrawl = true
        }
    }

    @Test(timeout = 180_000)
    @Throws(Throwable::class)
    fun normalTest() {
        Settings.crawlSpeed = Settings.DEFAULT_CRAWL_SPEED - 1
        val viewModel = (currentActivity as MainActivity).viewModel
        val floatingActionButton = ensureActionButtonShowing()

        // Force config change.
        setOrientationPortrait(CONFIG_TIMEOUT)

        // Force config change.
        floatingActionButton.perform(click())

        runBlocking {
            withContext(Dispatchers.Default) {
                toggleOrientationMonkey(6)
            }

            Settings.crawlSpeed = Settings.DEFAULT_CRAWL_SPEED

            while (viewModel.state == RUNNING) {
                yield()
            }

            // Wait for recycler view to finish processing all posted image events.
            delay(2000)
        }

        // View model should have completed normally.
        assertThat(viewModel.state).isEqualTo(COMPLETED)

        // There should be 112 images.
        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(EXPECTED_IMAGE_COUNT))

        println("normalTest was successful")
    }

    @Test(timeout = 180_000)
    @Throws(Throwable::class)
    fun startStopTest() {
        val viewModel = (currentActivity as MainActivity).viewModel

        // Force config change.
        setOrientationPortrait(CONFIG_TIMEOUT)

        IdlingRegistry.getInstance().register(object : IdlingResource {
            override fun isIdleNow() = true
            override fun getName() = "MyTestIdlingResource"
            override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            }
        })

        runBlocking {
            startStopCrawlMonkey(10)

            assertThat(viewModel.state).isEqualTo(COMPLETED)

            // Ensures that recycler view becomes idle.
            delay(4000)
        }

        // View model should have completed normally.
        assertThat(viewModel.state).isEqualTo(COMPLETED)


        // There should be 112 images.
        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(EXPECTED_IMAGE_COUNT))
    }

    private suspend fun toggleOrientationMonkey(times: Int, waitTime: Long = 1000) {
        val viewModel = (currentActivity as MainActivity).viewModel
        repeat(times) {
            println("toggle iteration $it started")
            if (!viewModel.isCrawlRunning) {
                println("toggle orientation returning")
                return@repeat
            }
            delay(waitTime)
            toggleOrientation(0)
            println("toggle iteration $it completed")
        }
    }

    private suspend fun startStopCrawlMonkey(times: Int, waitTime: Long = 2000) {
        var actualWaitTime = waitTime

        activityTestRule.activity?.apply {
            repeat(times) {
                val startState = withContext(Dispatchers.Main) {
                    startCrawl()
                    assertThat(viewModel.state).isEqualTo(RUNNING)
                    viewModel.state
                }

                assertThat(startState).isAnyOf(RUNNING, COMPLETED)

                if (it < times - 1) {
                    delay(waitTime)
                    val stopState = withContext(Dispatchers.Main) {
                        stopCrawl()
                        viewModel.state
                    }

                    // If crawl completed, then reduce then the wait time is
                    // too long so reduce it by 20% for the next iteration.
                    if (stopState == COMPLETED) {
                        actualWaitTime -= (actualWaitTime * .2f).toLong()
                    } else {
                        assertThat(stopState).isAnyOf(CANCELLING, CANCELLED)

                        while (viewModel.state != CANCELLED) {
                            yield()
                        }

                        assertThat(viewModel.state).isEqualTo(CANCELLED)

                        delay(2000)
                    }
                } else {
                    while (viewModel.state != COMPLETED) {
                        yield()
                    }
                }
            }
            assertThat(viewModel.state).isEqualTo(COMPLETED)
        }
    }

    private fun ensureActionButtonShowing() =
            onView(allOf(
                    withId(R.id.actionFab),
                    childAtPosition(
                            allOf(
                                    withId(R.id.progressFab),
                                    childAtPosition(
                                            withClassName(`is`("androidx.coordinatorlayout.widget.CoordinatorLayout")),
                                            3)
                            ),
                            0),
                    isDisplayed())
            )

    private fun setOrientationLandscape(wait: Int) {
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, wait)
    }

    private fun setOrientationPortrait(wait: Int) {
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, wait)
    }

    private fun setOrientation(orientation: Int, wait: Int) {
        try {
            currentActivity.requestedOrientation = orientation
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        // Give the system app to settle.
        if (wait > 0) {
            SystemClock.sleep(wait.toLong())
        }
    }

    private val currentActivity: Activity
        get() {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val activity = arrayOfNulls<Activity>(1)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val activities = ActivityLifecycleMonitorRegistry
                        .getInstance().getActivitiesInStage(Stage.RESUMED)
                activity[0] = Iterables.getOnlyElement(activities)
            }
            return activity[0]!!
        }

    private fun toggleOrientation(wait: Int) {
        try {
            when (currentActivity.requestedOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> setOrientationLandscape(wait)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> setOrientationPortrait(wait)
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        println("toggleOrientation call completed")
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

    class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
        override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
            if (noViewFoundException != null) {
                throw noViewFoundException
            }

            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter
            assertThat(adapter!!.itemCount, `is`(expectedCount))
        }
    }

    companion object {
        /**
         * Wait time constants.
         */
        private const val CONFIG_TIMEOUT = 4000
        private const val EXPECTED_IMAGE_COUNT = 112
    }
}
