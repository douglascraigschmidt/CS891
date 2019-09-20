package edu.vanderbilt.imagecrawler.platform;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.function.Consumer;

/**
 * Immutable data class so getters are redundant.
 */
public class CrawlResult {
    public final String mUrl;
    public final String mResult;
    public final State mState;
    public final Source mSource;
    public final String mErrorMessage;
    public final Exception mException;

    private CrawlResult(@NotNull Builder builder) {
        mUrl = builder.mUrl;
        mResult = builder.mResult;
        mState = builder.mStatus;
        mSource = builder.mSource;
        mErrorMessage = builder.mErrorMessage;
        mException = builder.mException;
    }

    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public static @NotNull Builder newBuilder(@NotNull CrawlResult copy) {
        Builder builder = new Builder();
        builder.mUrl = copy.mUrl;
        builder.mResult = copy.mResult;
        builder.mStatus = copy.mState;
        builder.mSource = copy.mSource;
        builder.mErrorMessage = copy.mErrorMessage;
        builder.mException = copy.mException;
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
                "url='" + mUrl + '\'' +
                "state='" + mState + '\'' +
                (mResult == null ? "" : ", result='" + mResult + '\'') +
                (mSource == null ? "" : ", source=" + mSource) +
                (mErrorMessage == null ? "": ", errorMessage='" + mErrorMessage + '\'') +
                (mException == null ? "" : ", exception=" + mException) +
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
        private String mUrl;
        private String mResult;
        private State mStatus;
        private Source mSource;
        private String mErrorMessage;
        private Exception mException;

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
            } else if (mUrl != null) {
                throw new IllegalArgumentException("url has already be set.");
            }

            mUrl = val;
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
            mResult = val;
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
            mStatus = val;
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
            } else if (mUrl != null) {
                throw new IllegalArgumentException("source has already be set.");
            }

            mSource = val;
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
            } else if (mErrorMessage != null) {
                throw new IllegalArgumentException("errorMessage has already be set.");
            }

            mErrorMessage = val;

            return this;
        }

        /**
         * Returns a {@code CrawlerResult} built from the parameters previously set.
         *
         * @return a {@code CrawlerResult} built with parameters of this {@code
         * CrawlerResult.Builder}
         */
        public @NotNull Builder exception(@NotNull Exception val) {
            if (mException != null) {
                throw new IllegalArgumentException("exception has already be set.");
            }

            mException = val;

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
            return new CrawlResult(this);
        }
    }
}
