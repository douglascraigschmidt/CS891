package edu.vanderbilt.imagecrawler.platform;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Interface encapsulating all platform dependent operations.
 * All platform dependant URL prefix types have been declared
 * here rather than in each platform implementation to make it
 * clear what custom prefixes are supported by the crawler
 * package.
 */
public interface Platform {
	/**
	 * The project sub-directory name where local web pages are stored
	 * (used when performing a local crawl).
	 */
	String LOCAL_WEB_PAGES_DIR_NAME = "web-pages";
	/**
	 * Custom resources URL prefix used for crawling pages in Java resources.
	 * project resources.
	 */
	String RESOURCES_URI_PREFIX = "file://java_resources";
	/**
	 * Custom resources URL prefix used for crawling pages in Java resources.
	 */
	String PROJECT_URI_PREFIX = "file://project_root";
	/**
	 * Custom assets URL prefix for crawling pages in Android assets.
	 */
	String ASSETS_URI_PREFIX = "file:://android_assets";

	/**
	 * Creates a new platform dependent image from the passed {@code imageData}
	 * bytes array and the source {@code url}.
	 *
	 * @param imageData The data bytes of the image.
	 * @return A platform image instance.
	 */
	PlatformImage newImage(byte[] imageData);

	/**
	 * Returns the cache directory File object with the specified name.
	 *
	 * @param dirName The name that is to be used for the root
	 *                cache directory.
	 * @return The cache directory with the given name.
	 */
	File getCacheDir(String dirName);

	/**
	 * Creates an input stream for the specified uri resource.
	 * This method supports both web and local uris (for example
	 * objects located in an application's resources or assets).
     *
	 * @param uri a web or local uri.
	 */
	InputStream mapUriToInputStream(String uri);
}
