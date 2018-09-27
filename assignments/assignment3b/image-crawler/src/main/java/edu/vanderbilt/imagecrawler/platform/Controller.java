package edu.vanderbilt.imagecrawler.platform;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
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
     * Static flag shared by all controller instances that
     * determines if diagnostic output should be displayed
     * to standard output.
     */
    public static boolean diagnosticsEnabled = true;
    /**
     * Platform specific hook methods.
     */
    public final Platform platform;
    /**
     * Platform independent options from UI or command line.
     */
    public final Options options;
    /**
     * List of transforms to be used on downloaded images.
     */
    public List<Transform> transforms;
    /**
     * A consumer that receives state updates and each downloaded
     * image.
     */
    public Consumer<CrawlResult> consumer;

    private Controller(@NotNull Builder builder) {
        platform = builder.platform;
        options = builder.options;
        transforms = builder.transforms;
        consumer = builder.consumer;
    }

    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public static @NotNull Builder newBuilder(@NotNull Controller copy) {
        Builder builder = new Builder();
        builder.platform = copy.platform;
        builder.options = copy.options;
        builder.transforms = copy.transforms;
        return builder;
    }

    /**
     * Returns whether diagnostic output is enabled.
     */
    public static boolean isDiagnosticsEnabled() {
        return diagnosticsEnabled;
    }

    /**
     * Enables/disables diagnostic output mode.
     */
    public static void setDiagnosticsEnabled(boolean enabled) {
        diagnosticsEnabled = enabled;
    }

    /**
     * Returns the cache directory File object with the specified name.
     *
     * @return The cache directory with the given name.
     */
    public File getCacheDir() {
        return platform.getCacheDir(options.downloadDirName);
    }

    /**
     * Constructs a new platform dependant image object.
     *
     * @param imageData The image bytes.
     * @return A new platform dependant image object.
     */
    public PlatformImage newImage(byte[] imageData) {
        return platform.newImage(imageData);
    }

    /**
     * Returns a lambda function that creates an input stream for the
     * passed uri. This method supports both normal URLs and any URL
     * located in the application resources.
     */
    public InputStream mapUriToInputStream(String uri) {
        return platform.mapUriToInputStream(uri);
    }

    /**
     * {@code Controller} builder static inner class.
     */
    public static final class Builder {
        /**
         * Set a default consumer that simply prints out the
         * receives state updates.
         */
        public Consumer<CrawlResult> consumer = this::debugConsumer;
        /**
         * Java is the default platform.
         */
        private Platform platform = new JavaPlatform();
        /**
         * See Options.Builder for the default options.
         */
        private Options.Builder optionsBuilder = Options.newBuilder();
        /**
         * Final options object constructed by build method.
         */
        private Options options;
        /**
         * Default transform is the NULL_TRANSFORM.
         */
        private List<Transform> transforms =
                Collections.singletonList(Transform.Factory
                        .newTransform(Transform.Type.NULL_TRANSFORM));

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
            if (platform != null) {
                platform = val;
            }
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
            if (val != null && !val.isEmpty()) {
                transforms = Transform.Factory.newTransforms(val);
            }
            return this;
        }

        /**
         * Sets the {@code transforms} and returns a reference to this
         * Builder so that the methods can be chained together.
         *
         * @param val the {@code transforms} to set (null to clear
         *            default debug consumer).
         * @return a reference to this Builder
         */
        @NotNull
        public Builder consumer(@NotNull Consumer<CrawlResult> val) {
            consumer = val;
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
            optionsBuilder.maxDepth(val);
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
                optionsBuilder.rootUrl(val);
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
                optionsBuilder.downloadPath(val);
            }
            return this;
        }

        /**
         * Sets the {@code diagnosticsEnabled} and returns a reference to this Builder so that the
         * methods can be chained together.
         *
         * @param val the {@code diagnosticsEnabled} to set
         * @return a reference to this Builder
         */
        public Builder diagnosticsEnabled(boolean val) {
            optionsBuilder.diagnosticsEnabled(val);
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
            // Build options.
            options = optionsBuilder.build();
            return new Controller(this);
        }
    }
}
