package edu.vandy.app.ui.screens.main

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import edu.vandy.R
import edu.vandy.app.ui.screens.settings.Settings.animationSpeed
import edu.vandy.app.ui.screens.settings.Settings.beingCount
import edu.vandy.app.ui.screens.settings.Settings.beingManagerType
import edu.vandy.app.ui.screens.settings.Settings.gazingDuration
import edu.vandy.app.ui.screens.settings.Settings.gazingIterations
import edu.vandy.app.ui.screens.settings.Settings.palantirCount
import edu.vandy.app.ui.screens.settings.Settings.palantirManagerType
import edu.vandy.app.utils.Range
import edu.vandy.simulator.managers.beings.BeingManager
import edu.vandy.simulator.managers.palantiri.PalantiriManager
import edu.vandy.simulator.model.implementation.components.SimulatorComponent.State.*
import edu.vandy.simulator.model.implementation.snapshots.ModelSnapshot
import edu.vandy.simulator.model.interfaces.ModelObserver
import org.hamcrest.*
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test

abstract class InstrumentedTests {
    /**
     * Wait time constants.
     */
    private val CONFIG_TIMEOUT = 10000
    private val PALANTIRI = 6
    private val BEINGS = 10
    private val ITERATIONS = 10
    private val SPEED = 100
    private val GAZING = Range(0, 1)

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    abstract val beingManager: BeingManager.Factory.Type
    abstract val palantirManager: PalantiriManager.Factory.Type

    private var finalState = UNDEFINED

    @Test(timeout = 50000)
    @Throws(Throwable::class)
    fun normalTest() {
        // Sleep to wait for app to start.
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val actionButton = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.progressFab),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.drawerLayout),
                                        0),
                                2),
                        ViewMatchers.isDisplayed()))

        // Force config change.
        setOrientationPortrait(CONFIG_TIMEOUT)

        beingManagerType = beingManager
        palantirManagerType = palantirManager
        beingCount = BEINGS
        palantirCount = PALANTIRI
        gazingIterations = ITERATIONS
        animationSpeed = SPEED
        gazingDuration = GAZING

        assertThat(beingManagerType, equalTo(beingManager))
        assertThat(palantirManagerType, equalTo(palantirManager))
        assertThat(beingCount, equalTo(BEINGS))
        assertThat(palantirCount, equalTo(PALANTIRI))
        assertThat(gazingIterations, equalTo(ITERATIONS))
        assertThat(animationSpeed, equalTo(SPEED))

        val activity = currentActivity as MainActivity

        finalState = UNDEFINED
        val desiredState = COMPLETED

        // Has to be declared as a variable or else it
        // will be garbage collected after the addObserver()
        // call.
        val observer = ModelObserver { snapshot: ModelSnapshot ->
            val state = snapshot.simulator.state
            when (state) {
                IDLE, RUNNING, CANCELLING -> Unit
                CANCELLED, ERROR -> {
                    assertThat(state, equalTo(desiredState))
                    if (finalState == UNDEFINED) {
                        finalState = snapshot.simulator.state
                    }
                }
                COMPLETED -> if (finalState == UNDEFINED) {
                    finalState = snapshot.simulator.state
                }
                else -> throw Exception("Invalid state $state")
            }
        }

        // Add this test as a snapshot observer.
        activity.viewModel.simulator!!.addObserver(observer, false)

        // Force config change.
        actionButton.perform(ViewActions.click())
        val maxSleeps = 30 // 40 seconds max
        val sleepTime: Long = 1000

        for (i in 1..maxSleeps) {
            Thread.sleep(sleepTime)
            if (i % 4 == 0) {
                toggleOrientation(CONFIG_TIMEOUT)
            }
            if (finalState != UNDEFINED) {
                break
            }
        }

        // Force a config change.
        setOrientationPortrait(CONFIG_TIMEOUT)
        assertThat(finalState, equalTo(COMPLETED))

        // Success!
        Log.d(TAG, "The test was successful!")
    }

    @Test(timeout = 50000)
    @Throws(Throwable::class)
    fun startStopStressTest() {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val actionButton = Espresso.onView(
                Matchers.allOf(ViewMatchers.withId(R.id.progressFab),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withId(R.id.drawerLayout),
                                        0),
                                2),
                        ViewMatchers.isDisplayed()))

        // Force config change.
        setOrientationPortrait(CONFIG_TIMEOUT)

        beingManagerType = beingManager
        palantirManagerType = palantirManager
        beingCount = BEINGS
        palantirCount = PALANTIRI
        gazingIterations = ITERATIONS
        animationSpeed = 0
        gazingDuration = Range(0, 0)

        assertThat(beingManagerType, equalTo(beingManager))
        assertThat(palantirManagerType, equalTo(palantirManager))
        assertThat(beingCount, equalTo(BEINGS))
        assertThat(palantirCount, equalTo(PALANTIRI))
        assertThat(gazingIterations, equalTo(ITERATIONS))
        assertThat(animationSpeed, equalTo(0))

        val activity = currentActivity as MainActivity

        // Has to be declared as a variable or else it will be
        // garbage collected after the addObserver() call.
        finalState = UNDEFINED

        val observer = ModelObserver { snapshot: ModelSnapshot ->
            finalState = snapshot.simulator.state
            assertThat(finalState, CoreMatchers.not(equalTo(ERROR)))
        }

        // Add this test as a snapshot observer.
        activity.viewModel.simulator!!.addObserver(observer, false)
        for (i in 1..19) {
            actionButton.perform(ViewActions.click())
            Thread.sleep(1)
        }
        Thread.sleep(50)
        assertThat(
                finalState,
                Matchers.isOneOf(COMPLETED,
                        CANCELLED))
        // Success!
        Log.d(TAG, "The test was successful!")
    }

    fun setOrientationLandscape(wait: Int) {
        Log.d(TAG, "palantiriActivityTest: setting orientation to LANDSCAPE")
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, wait)
    }

    fun setOrientationPortrait(wait: Int) {
        Log.d(TAG, "palantiriActivityTest: setting orientation to PORTRAIT")
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, wait)
    }

    fun setOrientation(orientation: Int, wait: Int) {
        try {
            currentActivity!!.requestedOrientation = orientation
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        // Give the system app to settle.
        SystemClock.sleep(wait.toLong())
    }

    fun toggleOrientation(wait: Int) {
        try {
            when (currentActivity!!.requestedOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> setOrientationLandscape(wait)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> setOrientationPortrait(wait)
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    private val currentActivity: Activity?
        get() {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val activity = arrayOfNulls<Activity>(1)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val activities = ActivityLifecycleMonitorRegistry
                        .getInstance().getActivitiesInStage(Stage.RESUMED)
                activity[0] = Iterables.getOnlyElement(activities)
            }
            return activity[0]
        }

    companion object {
        /**
         * Logging tag.
         */
        private const val TAG = "PalantiriActivityTest"

        private fun childAtPosition(
                parentMatcher: Matcher<View>, position: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description) {
                    description.appendText("Child at position $position in parent ")
                    parentMatcher.describeTo(description)
                }

                public override fun matchesSafely(view: View): Boolean {
                    val parent = view.parent
                    return (parent is ViewGroup && parentMatcher.matches(parent)
                            && view == parent.getChildAt(position))
                }
            }
        }
    }
}