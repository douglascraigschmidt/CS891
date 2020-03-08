package edu.vandy.simulator;

import java.security.InvalidParameterException;
import java.util.Random;

/**
 * A static class containing volatile static fields that are used to
 * control various aspects of the simulation.
 */
public class Controller {
    /**
     * Random number generator used by pauseThread() that
     * use a min/max pause range.
     */
    private static Random mRandom = new Random();

    /**
     * A scaling factor [0..1] that is applied to all pauseThread
     * operations that are used to provide the application time to
     * perform a UI feedback animation.
     */
    private static float mSimulationSpeed = 1f;

    /**
     * The min/max gazing timeout interval (randomly selected).
     */
    private static int mGazingMinTime = 1000;
    private static int mGazingMaxTime = 4000;

    /**
     * Flat indicating if logging output should be displayed.
     */
    private static boolean mLogging = false;

    /**
     * Flag used to disable all artificial simulation delays
     * so that different model strategies can be compared for
     * performance.
     */
    private static boolean mPerformanceMode = false;

    /**
     * Prevent construction of this class.
     */
    private Controller() {
        // Utility class.
    }

    public static float getSimulationSpeed() {
        return mPerformanceMode ? 0 : mSimulationSpeed;
    }

    /**
     * Sets the simulation speed to a scaling factor in the
     * range [0..1]. All millisecond values passed to the pauseThread
     * pauseThread method will be scaled by this value.
     * <p>
     * NOTE: When the simulation speed is set to 1, the default
     * duration values used throughout the model classes will be
     * used. Reducing the speed to a value < 1 will slow down the
     * simulation. Therefore, it makes sense to design all duration
     * values to run the simulation as fast as possible.
     * <p>
     * The UI layer presents the speed value as a percent [0..100]
     * where 100% will run the model at the default duration values,
     * and 0% will freeze the model simulation for analysis.
     *
     * @param scalingFactor A Float value in the range [0..1].
     */
    public static void setSimulationSpeed(float scalingFactor) {
        if (0f <= scalingFactor && scalingFactor <= 1f) {
            Controller.mSimulationSpeed = scalingFactor;
        } else {
            throw new InvalidParameterException(
                    "simulation scaling factor must be in the range [0..1]");
        }
    }

    /**
     * @return {@code true} if logging is enabled,
     * {@code false} if not.
     */
    public static boolean getLogging() {
        return mLogging;
    }

    /**
     * Enables or disables logging output.
     *
     * @param enable boolean flag to enable or disable logging.
     */
    public static void setLogging(boolean enable) {
        mLogging = enable;
    }

    /**
     * @return The current gazing range minimum bound.
     */
    public static int getGazingMinTime() {
        return mPerformanceMode ? 0 : mGazingMinTime;
    }

    /**
     * @return The current gazing range maximum bound.
     */
    public static int getGazingMaxTime() {
        return mPerformanceMode ? 0 : mGazingMaxTime;
    }

    /**
     * Sets the being gazing time range from which a random value
     * is selected for each gazing iteration.
     *
     * @param min The gazing time minimum bound.
     * @param max The gazing time maximum bound.
     */
    public static void setGazingTimeRange(int min, int max) {
        Controller.mGazingMinTime = min;
        Controller.mGazingMaxTime = max;
    }

    /**
     * Sets "performance" mode which disables all sleeping and
     * animations delays so that different simulation strategies
     * can be compared for performance (speed).
     *
     * @param enabled True to enable, false to disable.
     */
    public static void setPerformanceMode(boolean enabled) {
        mPerformanceMode = enabled;
    }

    /**
     * Outputs logging message only if logging is enabled.
     *
     * @param msg  String or format string to output
     * @param args vararg substitution list if msg is a format string.
     */
    public static void log(String msg, Object... args) {
        if (mLogging) {
            if (args.length > 0) {
                System.out.println(String.format(msg, args));
            } else {
                System.out.println(msg);
            }
        }
    }

    /**
     * @return A random duration value between the default minimum
     * and maximum range values.
     */
    public static long getRandomDelay() {
        return getRandomDelay(getGazingMinTime(), getGazingMaxTime());
    }

    /**
     * @return A random duration value between the specified minimum
     * and maximum range values.
     */
    public static long getRandomDelay(int min, int max) {
        if (mPerformanceMode) {
            return 0;
        } else {
            // Determine the pause range
            int range = Math.max(max - min, 0);

            // If the range is 0 (i.e., min == max) then just set the
            // duration to min, otherwise get a random value within the
            // specified range.
            return range == 0 ? min : mRandom.nextInt(range) + min;
        }
    }
}
