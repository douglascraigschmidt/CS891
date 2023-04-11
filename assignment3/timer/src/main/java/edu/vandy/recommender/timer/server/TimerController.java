package edu.vandy.recommender.timer.server;

import edu.vandy.recommender.common.Timer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static edu.vandy.recommender.common.Constants.EndPoint.*;
import static edu.vandy.recommender.common.Constants.Service.TIMER;

/**
 * The Spring controller for the {@link TimerService}.
 *
 * The {@code @RestController} annotation is a specialization of
 * {@code @Component} and is automatically detected through classpath
 * scanning.  It adds the {@code @Controller} and
 * {@code @ResponseBody} annotations. It also converts responses to
 * JSON or XML.
 *
 * The {@code @RequestMapping} annotation is used to map HTTP requests
 * to handler methods of REST controllers.
 */
@RestController
public class TimerController {
    /**
     * The central interface to provide configuration for the
     * application.  This field is read-only while the application is
     * running.
     */
    @Autowired
    ApplicationContext mApplicationContext;

    /**
     * Spring will inject the appropriate service associated with the
     * running microservice.
     */
    @Autowired
    private TimerService mService;

    /**
     * A request for testing the Eureka connection.
     *
     * @return The application name
     */
    @GetMapping({"/", "actuator/info"})
    public ResponseEntity<String> info() {
        return ResponseEntity
            // Indicate the request succeeded.
            // and return the application name.
            .ok(mApplicationContext.getId()
                + " is alive and running on "
                + Thread.currentThread()
                + "\n");
    }

    /**
     * Start recording the execution time of the given {@code
     * methodName}.
     *
     * @param timer A {@link Timer} instance
     * @param response The {@link HttpServletResponse} to return
     *                 to the caller
     * @return The {@link Timer} instance passed as a parameter
     */
    @PostMapping(POST_TIMING)
    public Timer recordTiming(@RequestBody Timer timer,
                              HttpServletResponse response) {
        // Set the response header to indicate where the
        // request was sent to.
        response
            .setHeader("Location",
                       ServletUriComponentsBuilder
                       .fromCurrentContextPath()
                       .path("/"
                              + TIMER
                              + "/"
                              + timer.id)
                       .toUriString());

        return mService
            // Forward to the service.
            .addTimer(timer);
    }

    /**
     * Clears all previously recorded timings.
     */
    @PostMapping(CLEAR_TIMINGS)
    public void clearTimings() {
        mService
            // Forward to the service.
            .clearTimings();
    }

    /**
     * @return A {@link String} containing the timing results for all
     *         the method runs ordered from fastest to slowest
     */
    @GetMapping(GET_TIMINGS)
    public String getTimings() {
        return mService
            // Forward request to the service.
            .getTimingResults();
    }
}
