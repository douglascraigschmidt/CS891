package edu.vanderbilt.imagecrawler.platform;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.Options;

/**
 * This class contains the crawler options and transforms, as well as
 * any device dependent hook methods that are required to transparently
 * run the crawler on different platforms/devices.
 * <p>
 * This class also supports a cancel() method that allows the controlling
 * application to cancel a currently running crawler. The crawler
 * periodically calls the throwExceptionIfCancelled() method at
 * strategically placed locations in the crawler implementation classes.
 * <p>
 * All  default field values are defined in the inner Builder class.
 * <p>
 * To reduce useless boilerplate code the immutable final fields
 * are accessible without getters.
 */
public class Controller {
    /**
     * Platform specific hook methods.
     */
    public final Platform mPlatform;
    /**
     * Platform independent options from UI or command line.
     */
    public final Options mOptions;
    /**
     * List of transforms to be used on downloaded images.
     */
    public final List<Transform> mTransforms;
    /**
     * A consumer that receives state updates and each downloaded
     * image.
     */
    public final Consumer<CrawlResult> mConsumer;

    private Controller(@NotNull Builder builder) {
        mPlatform = builder.mPlatform;
        mOptions = builder.mOptions;
        mTransforms = builder.mTransforms;
        mConsumer = builder.mConsumer;
    }

    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public static @NotNull Builder newBuilder(@NotNull Controller copy) {
        Builder builder = new Builder();
        builder.mPlatform = copy.mPlatform;
        builder.mOptions = copy.mOptions;
        builder.mTransforms = copy.mTransforms;
        return builder;
    }

    /**
     * Returns whether diagnostic output is enabled.
     */
    public static boolean loggingEnabled() {
        return Options.mDebug;
    }

    /**
     * Enables/disables diagnostic output mode.
     */
    public static void setDiagnosticsEnabled(boolean enabled) {
        Options.mDebug = enabled;
    }

    /**
     * Constructs a new platform dependant image object.
     *
     * @param inputStream An input stream containing image data.
     *
     * @return A new platform dependant image object.
     */
    public PlatformImage newImage(InputStream inputStream, Cache.Item item) {
        return mPlatform.newImage(inputStream, item);
    }

    /**
     * Returns a lambda function that creates an input stream for the
     * passed uri. This method supports both normal URLs and any URL
     * located in the application resources.
     */
    public InputStream mapUriToInputStream(String uri) {
        return mPlatform.mapUriToInputStream(uri);
    }

    /**
     * Cache implementation provided by platform.
     *
     * @return A platform dependant cache implementation.
     */
    public Cache getCache() {
        return mPlatform.getCache();
    }

    /**
     * @return The platform dependant root cache directory.
     */
    public File getCacheDir() {
        return getCache().getCacheDir();
    }

    /**
     * Helper for platform dependant logging.
     * @param msg Message or format string.
     * @param args Optional format arguments.
     */
    public void log(String msg, Object... args) {
        if (loggingEnabled()) {
            mPlatform.log(msg, args);
        }
    }

    /**
     * {@code Controller} builder static inner class.
     */
    public static final class Builder {
        /**
         * Set a default consumer that simply prints out the
         * receives state updates.
         */
        public Consumer<CrawlResult> mConsumer = this::debugConsumer;
        /**
         * See Options.Builder for the default options.
         */
        private final Options.Builder mOptionsBuilder = Options.newBuilder();
        /**
         * Final options object constructed by build method.
         */
        private Options mOptions;
        /**
         * Default transform is the NULL_TRANSFORM.
         */
        private List<Transform> mTransforms =
                Collections.singletonList(Transform.Factory
                        .newTransform(Transform.Type.TINT_TRANSFORM));

        /**
         * No default platform.
         */
        private Platform mPlatform;

        private Builder() {
        }

        public void debugConsumer(CrawlResult result) {
            System.out.println(
                    "[Thread: " + Thread.currentThread() + "]: "
                            + result.toString());
        }

        /**
         * Sets the {@code platform} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param val the {@code platform} to set
         * @return a reference to this Builder
         */
        @NotNull
        public Builder platform(@NotNull Platform val) {
            if (mPlatform != null) {
                throw new IllegalStateException("A platform has already been set.");
            }
            mPlatform = val;
            return this;
        }

        /**
         * Sets the {@code transforms} and returns a reference to this
         * Builder so that the methods can be chained together.
         *
         * @param val the {@code transforms} to set
         * @return a reference to this Builder
         */
        @NotNull
        public Builder transforms(List<Transform.Type> val) {
            mTransforms = Transform.Factory.newTransforms(val);
            return this;
        }

        /**
         * Sets the {@code transforms} and returns a reference to this
         * Builder so that the methods can be chained together.
         *
         * @param val the {@code transforms} to set (null to clear
         *            default log consumer).
         * @return a reference to this Builder
         */
        @NotNull
        public Builder consumer(@NotNull Consumer<CrawlResult> val) {
            mConsumer = val;
            return this;
        }

        /**
         * Sets the {@code maxDepth} and returns a reference to this Builder so that the methods
         * can be chained together.
         *
         * @param val the {@code maxDepth} to set
         * @return a reference to this Builder
         */
        public Builder maxDepth(int val) {
            mOptionsBuilder.maxDepth(val);
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
                mOptionsBuilder.rootUrl(val);
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
                mOptionsBuilder.downloadPath(val);
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
            mOptionsBuilder.diagnosticsEnabled(val);
            return this;
        }

        /**
         * Returns a {@code Controller} built from the parameters previously
         * set.
         *
         * @return a {@code Controller} built with parameters of this {@code
         * Controller.Builder}
         */
        @NotNull
        public Controller build() {
            // Validate input.
            if (mPlatform == null) {
                throw new IllegalStateException("A platform must be specified.");
            }

            // Build options.
            mOptions = mOptionsBuilder.build();

            return new Controller(this);
        }
    }
}
