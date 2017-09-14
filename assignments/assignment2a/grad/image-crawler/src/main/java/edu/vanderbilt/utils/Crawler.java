package edu.vanderbilt.utils;

import java.net.URL;

/**
 * Created by monte on 2017-09-10.
 */

/**
 * An interface defining the operations that must be supported by
 * any crawler implementation. A crawler is expected to initially
 * return a container object when {@code getContainer} is called.
 * In the case of a web crawler this will be a page object and in
 * the case of a file system crawler this would be a directory.
 *
 * A returned Container object is required to support provide
 * getContainer and getImageImage methods which return all child
 * CONTAINER type objects and all IMAGE type objects, respectively.
 */
public interface Crawler {
	/**
	 * The types of objects that can be returned by a crawler.
	 * For a web crawler, the container would be a page and
	 * for a file system crawler the container would be a directory.
	 */
	enum Type {
		CONTAINER,
		IMAGE
	}

	/**
	 * Returns a Container object for the specified URL. The underlying
	 * physical object may be a web page or a file system folder
	 * depending on the type of crawler.
	 *
	 * @param uri
	 * @return
	 */
	Container getContainer(String uri);

	/**
	 * Interface encapsulating all operations that can be performed a
	 * CONTAINER type object that is returned by the Crawler
	 * getContainer() method.
	 */
	interface Container {
		/**
		 * Returns all children objects of a given type (CONTAINER or IMAGE).
		 *
		 * @param type Either CONTAINER or IMAGE
		 * @return An array containing the string urls of all matched children objects.
		 */
		Array<String> getObjectsAsStrings(Type type);

		/**
		 * Returns the URLs for all children objects that match the specified
		 * type.
		 *
		 * @param type Either CONTAINER or IMAGE
		 * @return An array containing the URLs of all matched children objects.
		 */
		Array<URL> getObjectsAsUrls(Type type);
	}
}
