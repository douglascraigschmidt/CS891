package edu.vanderbilt.imagecrawler.utils;

import java.net.URI;
import java.net.URISyntaxException;

import edu.vanderbilt.imagecrawler.platform.Platform;

/**
 * Utility class containing URI and URL helper methods.
 */
public class UriUtils {
    /**
     * Converts any Platform supported URI to a relative path
     * suitable for storing in a file cache.
     *
     * @param uri Any supported URI (see Platform Interface).
     * @return A relative path.
     */
    public static String mapUriToRelativePath(String uri) {
        if (uri.startsWith(Platform.ASSETS_URI_PREFIX)) {
            return uri.replaceFirst(Platform.ASSETS_URI_PREFIX + "/", "");
        } else if (uri.startsWith(Platform.RESOURCES_URI_PREFIX)) {
            return uri.replaceFirst(Platform.RESOURCES_URI_PREFIX + "/", "");
        } else if (uri.startsWith(Platform.PROJECT_URI_PREFIX)) {
            return uri.replaceFirst(Platform.PROJECT_URI_PREFIX + "/", "");
        } else {
            try {
                URI uriPath = new URI(uri);
                return uriPath.getHost() + uriPath.getPath();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks if a uri refers to an object in the application's assets.
     *
     * @param uri uri to check.
     * @return True if uri refers to an object in app assets.
     */
    public static boolean isAssetUri(String uri) {
        return uri.startsWith(Platform.ASSETS_URI_PREFIX);
    }

    /**
     * Checks if a uri refers to file object in a project's root directory.
     *
     * @param uri uri to check.
     * @return True if uri refers to an object in app assets.
     */
    public static boolean isProjectUri(String uri) {
        return uri.startsWith(Platform.PROJECT_URI_PREFIX);
    }

    /**
     * Checks if a uri refers to an object in an application's resources.
     *
     * @param uri uri to check.
     * @return True if uri refers to an object in app assets.
     */
    public static boolean isResourcesUri(String uri) {
        return uri.startsWith(Platform.RESOURCES_URI_PREFIX);
    }
}
