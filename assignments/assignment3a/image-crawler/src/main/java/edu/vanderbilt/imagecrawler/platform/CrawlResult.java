package edu.vanderbilt.imagecrawler.platform;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.function.Consumer;

/**
 * Immutable data class so getters are redundant.
 */
public class CrawlResult {
    public final String url;
    public final String result;
    public final State state;
    public final Source source;
    public final String errorMessage;
    public final Exception exception;

    private CrawlResult(@NotNull Builder builder) {
        url = builder.url;
        result = builder.result;
        state = builder.status;
        source = builder.source;
        errorMessage = builder.errorMessage;
        exception = builder.exception;
    }

    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public static @NotNull Builder newBuilder(@NotNull CrawlResult copy) {
        Builder builder = new Builder();
        builder.url = copy.url;
        builder.result = copy.result;
        builder.status = copy.state;
        builder.source = copy.source;
        builder.errorMessage = copy.errorMessage;
        builder.exception = copy.exception;
        return builder;
    }

    /**
     * Helper method that submits a state update the the {@code consumer}.
     *
     * @param consumer The consumer that will receive the CrawlResult.
     * @param url      The url identifying the crawl item.
     * @param state    The current crawl state.
     */
    public static void reportStatus(Consumer<CrawlResult> consumer,
                                    URL url,
                                    State state) {
        consumer.accept(
                newBuilder()
                        .url(url.toString())
                        .state(state)
                        .build());
    }

    /**
     * Helper method that submits a error update the the {@code consumer}.
     *
     * @param consumer The consumer that will receive the CrawlResult.
     * @param url      The url identifying the crawl item.
     * @param message  An optional message.
     * @param e        An optional exception.
     */
    public static void submitError(Consumer<CrawlResult> consumer,
                                   URL url,
                                   String message,
                                   Exception e) {
        consumer.accept(
                newBuilder()
                        .url(url.toString())
                        .state(State.ERROR)
                        .exception(e)
                        .errorMessage(message)
                        .build());
    }

    /**
     * Helper method that submits the downloaded image the {@code consumer}.
     *
     * @param consumer The consumer that will receive the CrawlResult.
     * @param url      The url identifying the crawl item.
     * @param path     The cache path of the downloaded image.
     */
    public static void submitResult(Consumer<CrawlResult> consumer,
                                    URL url,
                                    String path) {
        consumer.accept(
                newBuilder()
                        .url(url.toString())
                        .result(path)
                        .state(State.READY).build());
    }

    @Override
    public String toString() {
        return "CrawlResult{" +
                "url='" + url + '\'' +
                "state='" + state + '\'' +
                (result == null ? "" : ", result='" + result + '\'') +
                (source == null ? "" : ", source=" + source) +
                (errorMessage == null ? "": ", errorMessage='" + errorMessage + '\'') +
                (exception == null ? "" : ", exception=" + exception) +
                '}';
    }

    /**
     * Current state of the crawl result
     */
    public enum State {
        QUEUED,
        DOWNLOADING,
        DOWNLOADED,
        TRANSFORMING,
        READY,
        ERROR
    }

    /**
     * Where the crawl object originated from.
     */
    public enum Source {
        WEB,
        LOCAL,
        CACHE
    }

    /**
     * {@code CrawlerResult} builder static inner class.
     */
    public static final class Builder {
        private String url;
        private String result;
        private State status;
        private Source source;
        private String errorMessage;
        private Exception exception;

        private Builder() {
        }

        /**
         * Sets the {@code url} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param val the {@code url} to set
         * @return a reference to this Builder
         */
        public @NotNull Builder url(@NotNull String val) {
            if (val.isEmpty()) {
                throw new IllegalArgumentException("url cannot be an empty string.");
            } else if (url != null) {
                throw new IllegalArgumentException("url has already be set.");
            }

            url = val;
            return this;
        }

        /**
         * Sets the {@code result} and returns a reference to this Builder so that the methods
         * can be chained together.
         * <p>
         * For convenience, result can be reset so that a single builder can be used to build
         * multiple CrawlerResult objects with different byte values.
         *
         * @param val the {@code result} to set (can be null if an error has occurred).
         * @return a reference to this Builder
         */
        public @NotNull Builder result(@NotNull String val) {
            result = val;
            return this;
        }

        /**
         * Sets the {@code state} and returns a reference to this Builder so that the methods can
         * be chained together.
         * <p>
         * For convenience, state can be reset so that a single builder
         * can be used to build multiple CrawlerResult objects with different state values.
         *
         * @param val the {@code state} to set
         * @return a reference to this Builder
         */
        public @NotNull Builder state(State val) {
            if (val == null) {
                throw new IllegalArgumentException("state cannot be null.");
            }
            status = val;
            return this;
        }

        /**
         * Sets the {@code source} and returns a reference to this Builder so that the methods can
         * be chained together.
         *
         * @param val the {@code source} to set
         * @return a reference to this Builder
         */
        public @NotNull Builder source(Source val) {
            if (val == null) {
                throw new IllegalArgumentException("source cannot be null.");
            } else if (url != null) {
                throw new IllegalArgumentException("source has already be set.");
            }

            source = val;
            return this;
        }

        /**
         * Returns a {@code CrawlerResult} built from the parameters previously set.
         *
         * @return a {@code CrawlerResult} built with parameters of this {@code
         * CrawlerResult.Builder}
         */
        public @NotNull Builder errorMessage(@NotNull String val) {
            if (val.isEmpty()) {
                throw new IllegalArgumentException(
                        "errorMessage cannot be set to an empty string.");
            } else if (errorMessage != null) {
                throw new IllegalArgumentException("errorMessage has already be set.");
            }

            errorMessage = val;

            return this;
        }

        /**
         * Returns a {@code CrawlerResult} built from the parameters previously set.
         *
         * @return a {@code CrawlerResult} built with parameters of this {@code
         * CrawlerResult.Builder}
         */
        public @NotNull Builder exception(@NotNull Exception val) {
            if (val == null) {
                throw new IllegalArgumentException("exception cannot be set to null.");
            } else if (exception != null) {
                throw new IllegalArgumentException("exception has already be set.");
            }

            exception = val;

            return this;
        }

        /**
         * Returns a {@code CrawlerResult} built from the parameters previously set.
         *
         * @return a {@code CrawlerResult} built with parameters of this {@code
         * CrawlerResult.Builder}
         */
        public @NotNull
        CrawlResult build() {
            /* monte: add back for android app
            if (url == null) {
                throw new RuntimeException("A url must be specified.");
            } else if (state == null) {
                throw new RuntimeException("A state must be specified.");
            } else if (source == null) {
                throw new RuntimeException("A state must be specified.");
            }
            */

            return new CrawlResult(this);
        }
    }
}
