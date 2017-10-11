package edu.vanderbilt.imagecrawler.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.vanderbilt.imagecrawler.transforms.Transform;

/**
 * An interface that Encapsulates all platform dependent image operations.
 */
public interface PlatformImage {
	/**
	 * Sets the {@code imageData} data from a provided input stream.
     * @param inputStream Input stream containing image data.
     * @param item
     */
	void setImage(InputStream inputStream, Cache.Item item);

	/**
     * Writes the image bytes to the output stream.
	 */
	void writeImage(OutputStream outputStream) throws IOException;

	/**
	 * Applies the specified transformation {@code type} to the image.
	 */
	PlatformImage applyTransform(Transform.Type type, Cache.Item item);

	/**
	 * @return Number of image bytes.
	 */
	int size();
}
