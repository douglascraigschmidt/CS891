package edu.vanderbilt.utils;

import org.jsoup.Jsoup;

import java.io.InputStream;

import edu.vanderbilt.platform.Device;

/**
 * This helper class works around deficiencies in the jsoup library
 * (www.jsoup.org), which doesn't make web-based crawling and local
 * filesystem crawling transparent out-of-the-box..
 */
public class LocalPageCrawler extends WebPageCrawler {
	/**
	 * @return A container that wraps the HTML document associated with the @a pageUri.
	 */
	public Container getContainer(String uri) {
		// This try block gets an input stream to the application's
		// resources and passes this value along with a base URL path
		// to JSoup to convert to a returned Document.
		if (Device.options().getDiagnosticsEnabled()) {
			System.out.println("***************************************");
			System.out.println("GET CONTAINER URI       = " + uri);
		}

		String baseUri = uri;
		if (uri.endsWith(".html") || uri.endsWith("htm")) {
			baseUri = uri.substring(0, uri.lastIndexOf('/')) + "/";
		}

		if (!baseUri.endsWith("/")) {
			baseUri += "/";
		}

		if (Device.options().getDiagnosticsEnabled()) {
			System.out.println("GET CONTAINER BASE URI  = " + baseUri);
			System.out.println("***************************************");
		}

		try (InputStream inputStream =
					 Device.instance().platform().getInputStream(uri)) {

			return new DocumentContainer(
					Jsoup.parse(inputStream, "UTF-8", baseUri),
					uri);
		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("getContainer Exception: " + e);
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			throw new RuntimeException(e);
		}
	}
}

