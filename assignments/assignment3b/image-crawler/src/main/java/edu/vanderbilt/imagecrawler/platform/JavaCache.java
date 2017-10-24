package edu.vanderbilt.imagecrawler.platform;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * A singleton Java Cache object implementation that specifies
 * the root cache directory for running the pure Java version of the
 * crawler application. This singleton's creation is both lazy and
 * thread-safe.
 */
public class JavaCache extends Cache {
    // Singleton instance.
    private static JavaCache _instance;

    // volatile is not really necessary here since the JVM
    // guarantees statically declared values to be thread-safe
    // at creation time (when the JVM loads).
    private volatile static Object lock = new Object();

    /**
     * Constructor that binds the cache implementation to a
     * platform specific root directory.
     *
     * @param cacheDir The platform dependent root cache directory.
     */
    private JavaCache(File cacheDir) throws IOException {
        super(cacheDir);
    }

    /**
     * Builds singleton if it hasn't been built and returns the instance.
     */
    public static Cache instance() {
        if (_instance == null) {
            //noinspection SynchronizeOnNonFinalField
            synchronized (lock) {
                if (_instance == null) {
                    try {
                        _instance = new JavaCache(
                                new File("./image-cache").getCanonicalFile());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return _instance;
    }
}