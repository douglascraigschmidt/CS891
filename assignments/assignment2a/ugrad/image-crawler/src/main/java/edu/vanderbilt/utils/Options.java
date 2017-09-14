package edu.vanderbilt.utils;

import java.net.URI;
import java.net.URL;

import edu.vanderbilt.platform.PlatformOptions;

/**
 * Immutable data class containing all crawling options.
 */
public class Options implements PlatformOptions {
    public static String DEFAULT_WEB_URL = "http://www.dre.vanderbilt.edu/~schmidt/imgs";
    public static String DEFAULT_DOWNLOAD_DIR_NAME = "downloaded-images";

    /**
     * The max depth for the crawler.
     * <p>
     * Default: 2.
     */
    private final int mMaxDepth;

    /**
     * Starting point for the crawling.
     * <p>
     * Default: "http://www.dre.vanderbilt.edu/~schmidt/imgs".
     */
    private final String mRootUrl;

    /**
     * Download directory name.
     */
    private final String mDownloadDirName;

    /**
     * Controls whether debugging output will be generated.
     * <p>
     * Default: false.
     */
    private final boolean mDiagnosticsEnabled;

    /**
     * Controls whether web-based or local crawling is performed.
     * <p>
     * Default: false.
     */
    private final boolean mLocal;

    private Options(Builder builder) {
        mMaxDepth = builder.mMaxDepth;
        mRootUrl = builder.mRootUrl;
        mDownloadDirName = builder.mDownloadDirName;
        mDiagnosticsEnabled = builder.mDiagnosticsEnabled;
        mLocal = builder.mLocal;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Return if root uri is local.
     */
    public boolean isLocal() {
        return mLocal;
    }

    /**
     * Return the max depth for the crawler.
     */
    @Override
    public int getMaxDepth() {
        return mMaxDepth;
    }

    /**
     * Return the Uri that's used to initiate the crawling. Note that
     * if this is a local crawl, this method will call the Platform
     * class to ensure that the returned URL is, in fact, a local
     * url.
     * rootUrl that is always specified
     */
    @Override
    public String getRootUri() {
        return mRootUrl;
    }

    /**
     * Return the Uri that's used to initiate the crawling.
     */
    @Override
    public String getDownloadDirName() {
        return mDownloadDirName;
    }

    /**
     * Returns whether debugging output is generated.
     */
    @Override
    public boolean getDiagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * {@code Options} builder static inner class with default values set.
     */
    public static final class Builder {
        private int mMaxDepth = 2;
        private String mRootUrl = DEFAULT_WEB_URL;
        private String mDownloadDirName = DEFAULT_DOWNLOAD_DIR_NAME;
        private boolean mDiagnosticsEnabled = false;
        private boolean mLocal = false;

        private Builder() {
        }

        /**
         * Sets the {@code mMaxDepth} and returns a reference to this Builder so that the methods
         * can be chained together.
         *
         * @param val the {@code mMaxDepth} to set
         * @return a reference to this Builder
         */
        public Builder maxDepth(int val) {
            mMaxDepth = val;
            return this;
        }

        /**
         * Sets the {@code mRootUrl} and returns a reference to this Builder so that the methods
         * can
         * be chained together.
         *
         * @param val the {@code mRootUrl} to set
         * @return a reference to this Builder
         */
        public Builder rootUrl(String val) {
            if (val != null && val.isEmpty()) {
                mRootUrl = val;
            }
            return this;
        }

        /**
         * Sets the {@code mDownloadDirName} and returns a reference to this Builder so that the
         * methods can be chained together.
         *
         * @param val the {@code mRootUrl} to set
         * @return a reference to this Builder
         */
        public Builder downloadDirName(String val) {
            mDownloadDirName = val;
            return this;
        }

        /**
         * Sets the {@code mDiagnosticsEnabled} and returns a reference to this Builder so that the
         * methods can be chained together.
         *
         * @param val the {@code mDiagnosticsEnabled} to set
         * @return a reference to this Builder
         */
        public Builder diagnosticsEnabled(boolean val) {
            mDiagnosticsEnabled = val;
            return this;
        }

        /**
         * Sets the {@code mLocal} and returns a reference to this Builder so that the methods can
         * be chained together.
         *
         * @param val the {@code mLocal} to set
         * @return a reference to this Builder
         */
        public Builder local(boolean val) {
            mLocal = val;
            return this;
        }

        /**
         * Returns a {@code Options} built from the parameters previously set.
         *
         * @return a {@code Options} built with parameters of this {@code Options.Builder}
         */
        public Options build() {
            return new Options(this);
        }
    }
}
