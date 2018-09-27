package edu.vanderbilt.platform;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Interface encapsulating all platform dependent operations.
 */
public interface Platform {
	/**
	 * Creates a new platform dependent image from the passed {@code imageData}
	 * bytes array and the source {@code url}.
	 *
	 * @param url       The url where the image is stored.
	 * @param imageData The data bytes of the image.
	 * @return A platform image instance.
	 */
	PlatformImage newImage(URL url, byte[] imageData);

	/**
	 * @return The platform dependent image download folder path.
	 */
	String getCacheDirPath();

	/**
	 * @returns the URI authority for a local web crawl.
	 */
	String getAuthority();

	/**
	 * @returns the URI scheme for a local web crawl.
	 */
	String getScheme();

	/**
	 * @returns the platform root URI for a local web crawl.
	 */
	URI getLocalRootUri();

	/**
	 * Returns image stream that can be used to read in the image byte data.
	 *
	 * @param uri The image source url.
	 * @return An input stream.
	 */
	InputStream getInputStream(String uri);

	/**
	 * Returns the list of user entered URLs to input to the image crawler.
	 *
	 * @return List of URLs.
	 */
	List<URL> getUrlList();

	/**
	 * Downloads the image bytes from the specified URL. When the image
	 * crawler is running in local mode, this url will be mapped into
	 * the applications resources or assets.
	 *
	 * @param url The URL of an image.
	 * @return The downloaded image data.
	 */
	byte[] downloadContent(URL url);

	/**
	 * Called to convert any URL to a relative path part. This needs special
	 * platform specific handle for cases like "file:///android_assets/..."
	 * or "file:///java_resources/...". Currently, only CacheUtils uses the
	 * method so that it can use the path part to store downloaded files (or
	 * locally "downloaded" files) into a file system path.
	 *
	 * @param url The web URL of the image.
	 * @return A relative file path suitable for caching the downloaded image file into.
	 */
	String mapUrlToRelativeFilePath(URL url);
}
