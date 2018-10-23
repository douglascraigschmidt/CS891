package edu.vanderbilt.imagecrawler.utils;

import edu.vanderbilt.imagecrawler.platform.Platform;

/**
 * Immutable data class containing all crawling options. To avoid
 * unnecessary boiler-plate code, this class and all final
 * fields are immutable so the can be accessed directly and not
 * require getters or setters.
 */
public class Options {
    /**
     * Defaults.
     */
    public static String DEFAULT_WEB_URL = "http://www.dre.vanderbilt.edu/~schmidt/imgs";
    public static String DEFAULT_DOWNLOAD_DIR_NAME = "downloaded-images";

    /**
     * Controls whether debugging output will be generated.
     * <p>
     * Default: false.
     */
    public static boolean mDebug = false;
    /**
     * Starting point for the crawling.
     * <p>
     * Default: "http://www.dre.vanderbilt.edu/~schmidt/imgs".
     */
    public static String mRootUrl;
    /**
     * The max depth for the crawler.
     * <p>
     * Default: 2.
     */
    public final int mMaxDepth;
    /**
     * Download directory name.
     */
    public final String mDownloadDirName;

    private Options(Builder builder) {
        mMaxDepth = builder.mMaxDepth;
        mRootUrl = builder.mRootUrl;
        mDownloadDirName = builder.mDownloadDirName;
        mDebug = builder.mDiagnosticsEnabled;
    }

    /**
     * @return Resource locator for the current crawl (local files or local assets or remote web).
     */
    public static String getRootUrlLocator() {
        if (mRootUrl.startsWith(Platform.ASSETS_URI_PREFIX)) {
            return Platform.ASSETS_URI_PREFIX;
        } else if (mRootUrl.startsWith(Platform.PROJECT_URI_PREFIX)) {
            return Platform.PROJECT_URI_PREFIX;
        } else if (mRootUrl.startsWith(Platform.RESOURCES_URI_PREFIX)) {
            return Platform.RESOURCES_URI_PREFIX;
        } else {
            return Platform.HTTP_URI_PREFIX;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * {@code Options} builder static inner class with default values set.
     */
    public static final class Builder {
        private int mMaxDepth = 2;
        private String mRootUrl = DEFAULT_WEB_URL;
        private String mDownloadDirName = DEFAULT_DOWNLOAD_DIR_NAME;
        private boolean mDiagnosticsEnabled = false;

        private Builder() {
        }

        /**
         * Sets the {@code maxDepth} and returns a reference to this Builder so that the methods
         * can be chained together.
         *
         * @param val the {@code maxDepth} to set
         * @return a reference to this Builder
         */
        public Builder maxDepth(int val) {
            mMaxDepth = val;
            return this;
        }

        /**
         * Sets the {@code rootUrl} and returns a reference to this Builder so that the methods
         * can
         * be chained together.
         *
         * @param val the {@code rootUrl} to set
         * @return a reference to this Builder
         */
        public Builder rootUrl(String val) {
            if (val != null && !val.isEmpty()) {
                mRootUrl = val;
            }
            return this;
        }

        /**
         * Sets the {@code downloadPath} and returns a reference to this Builder so that the
         * methods can be chained together.
         *
         * @param val the {@code rootUrl} to set
         * @return a reference to this Builder
         */
        public Builder downloadPath(String val) {
            if (val != null && !val.isEmpty()) {
                mDownloadDirName = val;
            }
            return this;
        }

        /**
         * Sets the {@code debug} and returns a reference to this Builder so that the
         * methods can be chained together.
         *
         * @param val the {@code debug} to set
         * @return a reference to this Builder
         */
        public Builder diagnosticsEnabled(boolean val) {
            mDiagnosticsEnabled = val;
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
