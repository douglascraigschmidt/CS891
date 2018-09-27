package edu.vanderbilt.imagecrawler.platform;

import edu.vanderbilt.imagecrawler.transforms.Transform;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface that Encapsulates all platform dependent image operations.
 */
public interface PlatformImage {

	/**
	 * Sets the {@codwe imageData} bytes of the image.
	 * @param imageData
	 */
	void setImage(byte[] imageData);

	/**
     * Writes the image bytes to the output stream.
	 */
	void writeImage(OutputStream outputStream) throws IOException;

	/**
	 * Applies the specified transformation {@code type} to the image.
	 */
	PlatformImage applyTransform(Transform.Type type);
}
