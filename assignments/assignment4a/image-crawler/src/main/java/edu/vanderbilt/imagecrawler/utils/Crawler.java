package edu.vanderbilt.imagecrawler.utils;

import java.net.URL;

/**
 * An interface defining the operations that must be supported by
 * any web crawler implementation. A crawler is expected to initially
 * return a page object when {@code getPage} is called.
 * In the case of a web crawler this will be a page object and in
 * the case of a file system crawler this would be a directory.
 *
 * A returned Page object is required to support provide
 * getPage and getImageImage methods which return all child
 * PAGE type objects and all IMAGE type objects, respectively.
 */
public interface Crawler {
	/**
	 * The types of objects that can be returned by a crawler.
	 * For a web crawler, the container would be a page and
	 * for a file system crawler the container would be a directory.
	 */
	enum Type {
		PAGE,
		IMAGE
	}

	/**
	 * Returns a Page object for the specified URL. The underlying
	 * physical object may be a web page or a file system folder
	 * depending on the type of crawler.
	 *
	 * @param uri Uri of any page.
	 * @return The Page object matching the specified uri.
	 */
	Page getPage(String uri);

	/**
	 * Interface encapsulating all operations that can be performed a
	 * PAGE type object that is returned by the Crawler
	 * getPage() method.
	 */
	interface Page {
		/**
		 * Returns all children objects of a given type (PAGE or IMAGE).
		 *
		 * @param type Either PAGE or IMAGE
		 * @return An array containing the string urls of all matched children objects.
		 */
		Array<String> getObjectsAsStrings(Type type);

		/**
		 * Returns the URLs for all children objects that match the specified
		 * type.
		 *
		 * @param type Either PAGE or IMAGE
		 * @return An array containing the URLs of all matched children objects.
		 */
		Array<URL> getObjectsAsUrls(Type type);
	}
}
