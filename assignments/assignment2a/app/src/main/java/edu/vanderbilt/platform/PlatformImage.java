package edu.vanderbilt.platform;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * An interface that Encapsulates all platform dependent image operations.
 */
public interface PlatformImage {
	/**
	 * Sets the source {@code url} and {@codwe imageData} bytes of the image.
	 * @param url
	 * @param imageData
	 */
	void setImage(URL url, byte[] imageData);

	/**
	 * Returns the source {@Code URL} that was used when the image was
	 * constructed or when {@code setImage()} was called.
	 *
	 * @return The original image {@code URL}.
	 */
	URL getSourceUrl();

	/**
	 * Maps a URL to platform friendly path string that can be used to
	 * cache the image once it has been downloaded.
	 *
	 * @return A file path suitable for caching the downloaded image file.
	 */
	String mapUrlToRelativeFilePath();

	/**
	 *
	 * @param outputStream
	 * @throws IOException
	 */
	void writeImageFile(FileOutputStream outputStream) throws IOException;

	/**
	 *
	 * @return
	 */
	PlatformImage applyFilter();
}
