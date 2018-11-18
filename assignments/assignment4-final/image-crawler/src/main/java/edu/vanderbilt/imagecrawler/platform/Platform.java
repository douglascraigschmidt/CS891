package edu.vanderbilt.imagecrawler.platform;

import java.io.InputStream;

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
	 */
	String RESOURCES_URI_PREFIX = "file://java_resources";
	/**
	 * Custom resources URL prefix used for crawling pages in Java resources.
	 */
	String PROJECT_URI_PREFIX = "file://project_root";
	/**
	 * Custom assets URL prefix for crawling pages in Android assets.
	 * NOTE: Android requires 3 /// characters.
	 */
	String ASSETS_URI_PREFIX = "file:///android_assets";

	/** Default web URL prefix is http */
	String HTTP_URI_PREFIX = "http:/";

	/** Default web URL prefix is https */
    String HTTPS_URI_PREFIX = "https:/";

    /**
	 * Creates a new platform dependent image from the passed {@code imageData}
	 * bytes array and the source {@code url}.
	 *
	 * @param inputStream The data bytes of the image.
	 * @return A platform image instance.
	 */
    PlatformImage newImage(InputStream inputStream, Cache.Item item);

    /**
	 * @return The platform dependent cache implementation.
	 */
	Cache getCache();

	/**
	 * Creates an input stream for the specified uri resource.
	 * This method supports both web and local uris (for example
	 * objects located in an application's resources or assets).
     *
	 * @param uri a web or local uri.
	 */
	InputStream mapUriToInputStream(String uri);

	/**
	 * Prints log using platform dependent logging if log has
	 * been enabled.
	 * @param msg Message string or format string.
	 * @param args Optional format arguments.
	 */
    void log(String msg, Object[] args);
}
