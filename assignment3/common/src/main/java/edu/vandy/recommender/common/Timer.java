package edu.vandy.recommender.common;

/**
 * This Data Transfer Object (DTO) is used when communicating with the
 * timer service. This DTO must be sent via REST to the {@code
 * Constants.EndPoint.POST_TIMING} endpoint.  See {@code
 * TimerController#recordTiming(Timer, HttpServletResponse)} for more
 * information.
 */
public class Timer {
    /**
     * The id for this {@link Timer} instance.
     */
    public String id;

    /**
     * The start time.
     */
    public long startTime;

    /**
     * The stop time.
     */
    public long stopTime;

    /**
     * Constructs a {@link Timer} object and automatically sets
     * its globally unique invocation id. Both start and stop
     * times can only be set by explicitly calling the start() and
     * stop() methods.
     *
     * @param id A {@link String} identifying what entity (e.g.,
     *           applicationName + methodName) is being timed
     */
    public Timer(String id) {
        // Set the id field.
        this.id = id;

        // Start and stop times must be set explicitly.
        startTime = 0L;
        stopTime = 0L;
    }

    /**
     * A default constructor is needed for encoding/decoding.
     */
    public Timer() {
    }
}
