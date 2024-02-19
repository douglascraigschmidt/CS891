package edu.vandy.recommender.timer.common;

import edu.vandy.recommender.timer.server.TimerController;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This Data Transfer Object (DTO) is used when communicating with the
 * timer service. This DTO must be sent via REST to the
 * {@link Constants.EndPoint#POST_TIMING} endpoint.
 * See {@link TimerController#recordTiming(Timer, HttpServletResponse)}
 * for more information.
 *
 * This class must be cloned to any application
 * that uses the Timer service.
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
}
