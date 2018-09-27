package edu.vanderbilt.platform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Java Platform helper methods.
 */
public class JavaPlatform implements Platform {
	/**
	 * Custom resources URL prefix string for local crawl parsing from resources.
	 */
	public final String RESOURCES_URI_PREFIX = "file::///java_resources";
	/**
	 * The path to the image directory.
	 */
	private final String IMAGE_DIRECTORY_PATH =
			"downloaded-images";

	/**
	 * Creates a new Java platform bitmap.
	 *
	 * @param url       Image source URL.
	 * @param imageData Image bytes.
	 * @return A Java implementation of the PlatformImage interface.
	 */
	@Override
	public PlatformImage newImage(URL url, byte[] imageData) {
		return new JavaImage(url, imageData, this);
	}

	/**
	 * @return The cache directory path (absolute path).
	 */
	@Override
	public String getCacheDirPath() {
		try {
			return new File(IMAGE_DIRECTORY_PATH).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates an input stream for the passed URL. This method supports
	 * both normal URLs and any URL located in the application resources.
	 *
	 * @param uri Any URL including a resource URL.
	 * @return An input stream.
	 */
	@Override
	public InputStream getInputStream(String uri) {
		try {
			URL url = new URL(uri);

			if ("file".equals(url.getAuthority())
					&& "resources".equals(url.getHost())) {
				// Resource URL.
				String resourcePath = url.getPath();
				return getClass().getResourceAsStream(File.separator + resourcePath);
			} else {
				// Normal URL.
				return new URL(uri).openStream();
			}

			/*
			if (Device.instance().options().isLocal()) {
				// Resource URL.
				String resourcePath = getFilePathForUrl(new URL(uri));
				return getClass().getResourceAsStream(File.separator + resourcePath);
			} else {
				// Normal URL.
				return new URL(uri).openStream();
			}
			*/
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the list of lists of URLs from which the user wants to
	 * download images.
	 */
	@Override
	public List<URL> getUrlList() {
		try {
			return Collections.singletonList(new URL(Device.options().getRootUri()));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Download the contents found at the given URL and return them as
	 * a raw byte array.
	 */
	@Override
	public byte[] downloadContent(URL url) {
		// The size of the image downloading buffer.
		final int BUFFER_SIZE = 4096;

		// Creates a new ByteArrayOutputStream to write the downloaded
		// contents to a byte array, which is a generic form of the
		// image.
		ByteArrayOutputStream ostream =
				new ByteArrayOutputStream();

		// This is the buffer in which the input data will be stored
		byte[] readBuffer = new byte[BUFFER_SIZE];
		int bytes;

		// Creates an InputStream from the inputUrl from which to read
		// the image data.
		try (InputStream istream = getInputStream(url.toString())) {
			// While there is unread data from the inputStream,
			// continue writing data to the byte array.
			while ((bytes = istream.read(readBuffer)) > 0) {
				ostream.write(readBuffer, 0, bytes);
			}

			return ostream.toByteArray();
		} catch (IOException e) {
			// "Try-with-resources" will clean up the istream
			// automatically.
			System.out.println("Error downloading url " + url + ": " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns a relative file path form of the @a url.
	 */
	@Override
	public String mapUrlToRelativeFilePath(URL url) {
		return url.toString().startsWith(RESOURCES_URI_PREFIX)
			   ? url.toString().replaceFirst(RESOURCES_URI_PREFIX + "/", "")
			   : (url.getHost() + url.getPath());
	}

	/**
	 * @returns the URI authority for a local web crawl.
	 */
	@Override
	public String getAuthority() {
		return getLocalRootUri().getAuthority();
	}

	/**
	 * @returns the URI scheme for a local web crawl.
	 */
	@Override
	public String getScheme() {
		return getLocalRootUri().getScheme();
	}

	/**
	 * @returns the platform root URI for a local web crawl.
	 */
	public URI getLocalRootUri() {
		// The only way to get to the resources directory for both tests
		// and when the jar is run as an executable, is to look for the
		// META-INF directory which is in the root of the JAR file.
		try {
			return new URI(getClass()
					.getResource("/META-INF")
					.toString()
					.replaceFirst("/META-INF", "/"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
