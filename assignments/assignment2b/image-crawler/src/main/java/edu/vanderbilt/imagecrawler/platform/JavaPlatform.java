package edu.vanderbilt.imagecrawler.platform;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import edu.vanderbilt.imagecrawler.utils.UriUtils;

/**
 * Java Platform helper methods.
 */
public class JavaPlatform implements Platform {
    /**
     * Creates a new Java platform bitmap.
     *
     * @param imageData Image bytes.
     * @return A Java implementation of the PlatformImage interface.
     */
    @Override
    public PlatformImage newImage(byte[] imageData) {
        return new JavaImage(imageData);
    }

    /**
     * Returns the cache directory File object with the specified name.
     *
     * @param dirName The name that is to be used for the root
     *                cache directory.
     * @return The cache directory with the given name.
     */
    @Override
    public File getCacheDir(String dirName) {
        return new File(dirName);
    }

    /**
     * Creates an input stream for the specified uri resource.
     * This method supports both web and local uris (for example
     * objects located in an application's resources or assets).
     * When the application is running as an executable JAR, the
     * resources will be located in the JAR file, but when run
     * from JUnit tests from the IDE, the resources will only be
     * accessible from the project's resources directory. This
     * method handles both cases.
     */
    @Override
    public InputStream mapUriToInputStream(String uri) {
        try {
            if (UriUtils.isResourcesUri(uri)) {
                // Resource URL.
                String resPath = UriUtils.mapUriToRelativePath(uri);
                return getClass().getResourceAsStream(File.separator + resPath);
            } else if (UriUtils.isProjectUri(uri)) {
                // Map custom project host based uri to standard file uri
                // that references the project root directory.
                String relPath =
                        LOCAL_WEB_PAGES_DIR_NAME
                                + "/"
                                + UriUtils.mapUriToRelativePath(uri);
                String absPath = new File(relPath).getCanonicalFile().toURI().toString();
                return new URL(absPath).openStream();
            } else {
                // Normal URL.
                return new URL(uri).openStream();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
