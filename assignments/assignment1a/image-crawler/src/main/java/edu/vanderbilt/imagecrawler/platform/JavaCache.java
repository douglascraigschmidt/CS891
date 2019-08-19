package edu.vanderbilt.imagecrawler.platform;

import java.io.File;
import java.io.IOException;

/**
 * A singleton Java Cache object implementation that specifies
 * the root cache directory for running the pure Java version of the
 * crawler application. This singleton's creation is both lazy and
 * thread-safe.
 */
public class JavaCache
       extends Cache {
    // Singleton instance.
    private static JavaCache sInstance;

    // volatile is not really necessary here since the JVM guarantees
    // statically declared values to be thread-safe at creation time
    // (when the JVM loads).

    /**
     * Constructor that binds the cache implementation to a
     * platform specific root directory.
     *
     * @param cacheDir The platform dependent root cache directory.
     */
    private JavaCache(File cacheDir) {
        super(cacheDir);
    }

    /**
     * Builds singleton if it hasn't been built and returns the
     * instance.
     */
    public static Cache instance() {
        if (sInstance == null) {
            //noinspection SynchronizeOnNonFinalField
            synchronized (JavaCache.class) {
                if (sInstance == null) {
                    try {
                        sInstance =
                            new JavaCache(new File("./image-cache")
                                          .getCanonicalFile());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return sInstance;
    }
}
