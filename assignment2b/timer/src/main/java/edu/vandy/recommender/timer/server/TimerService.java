package edu.vandy.recommender.timer.server;

import edu.vandy.recommender.timer.common.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class defines implementation methods that are called by the
 * {@link TimerController} to provide asynchronous and synchronous
 * computation of method execution times.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning.  It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
@ComponentScan("edu.vandy.recommender.timer")
public class TimerService {
    /**
     * Keep track of all the timing results. This {@link Map}
     * associates an identifier with its {@link Timing}.
     */
    @Autowired
    private ConcurrentHashMap<String, Timing> mResultsMap;

    /**
     * Keep a record the passed {@link Timer}.
     *
     * @param timer A {@link Timer} instance.
     */
    public Timer addTimer(Timer timer) {
        // Compute the elapsed time in microseconds.
        long elapsedTime = (timer.stopTime - timer.startTime) 
            / 1_000_000;

        // Lookup the TimingRecord associated with methodName.
        Timing timing = mResultsMap.get(timer.id);

        // This handles the case where methodName wasn't in the Map.
        if (timing == null) {
            timing = new Timing(elapsedTime);

            // Put the methodName and timingRecord into the results
            // Map.
            mResultsMap.put(timer.id, timing);
        } else {
            // Increment the invocation count.
            timing.invocationCount.incrementAndGet();

            // Update the average time.
            timing
                .averageTime
                .set((timing.invocationCount.get()
                      * timing.averageTime.get()
                      + elapsedTime)
                     / (timing.invocationCount.get()));
        }

        return timer;
    }

    /**
     * @return A {@link String} containing the timing results for all
     *         the method runs ordered from fastest to slowest
     */
    public String getTimingResults() {
        StringBuilder stringBuffer =
            new StringBuilder();

        stringBuffer.append("\nPrinting ")
            .append(mResultsMap.entrySet().size())
            .append(" results from fastest to slowest\n");

        if (mResultsMap.isEmpty()) {
            return "No timings have been recorded.";
        }

        // Print the contents of the mResultsMap in sorted order.
        mResultsMap
            // Get the entrySet for the mResultsMap.
            .entrySet()

            // Convert the entrySet into a stream.
            .stream()

            // Create a SimpleEntry containing the timing results
            // (value) followed by the test name (key).
            .map(entry
                 -> new SimpleEntry<>(entry.getValue(),
                                      entry.getKey()))

            // Sort the stream by the timing results (key).
            .sorted(Comparator.comparing(SimpleEntry::getKey))

            // Append the entries in the sorted stream.
            .forEach(entry -> stringBuffer
                     .append(entry.getKey().invocationCount)
                     .append(" call(s) to ")
                     .append(entry.getValue())
                     .append(" executed in an average of ")
                     .append(entry.getKey().averageTime)
                     .append(" msecs\n"));

        // Convert stringBuffer to a String and return it.
        var results = stringBuffer.toString();
        System.out.println(results);
        return results;
    }

    /**
     * Clears all previously recorded timings.
     */
    public void clearTimings() {
        mResultsMap.clear();
    }

    /**
     * This class records the execution time of timing requests.
     */
    public static class Timing
           implements Comparable<Timing> {
        /**
         * The average time of all the computations for a given
         * identifier.
         */
        final AtomicLong averageTime;

        /**
         * The total number of invocations for a given identifier.
         */
        final AtomicLong invocationCount;

        /**
         * Constructor initializes the fields.
         */
        public Timing(long elapsedTime) {
            this.averageTime = new AtomicLong(elapsedTime);
            this.invocationCount = new AtomicLong(1);
        }

        /**
         * Compares this {@link Timing} with the specified {@link
         * Timing} for order using the {@code averageTime()} method.
         * Returns a negative integer, zero, or a positive integer as
         * this {@link Timing} is less than, equal to, or greater than
         * the specified {@link Timing}.
         *
         * @param that The {@link Timing} to be compared
         * @return A negative integer, zero, or a positive integer as
         *         this {@link Timing}'s average time is less than,
         *         equal to, or greater than the specified {@link
         *         Timing}
         */
        @Override
        public int compareTo(Timing that) {
            return (int) (this.averageTime.get()
                          - that.averageTime.get());
        }
    }
}
