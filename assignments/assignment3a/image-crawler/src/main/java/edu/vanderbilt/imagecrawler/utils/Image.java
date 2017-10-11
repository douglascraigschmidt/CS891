package edu.vanderbilt.imagecrawler.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.platform.PlatformImage;
import edu.vanderbilt.imagecrawler.transforms.Transform;

/**
 * Stores platform-independent meta-data about an Image and also
 * provides methods for common image- and file-related tasks, such as
 * decoding raw byte arrays into an Image and setting/getting transform
 * and file names.
 */
public class Image {
	private PlatformImage mImage;

	/**
	 * The name of the transform that was applied to this result.
	 */
	private String mFilterName;
	/**
	 * Keeps track of whether operations on this Image succeed.
	 */
	private boolean mSucceeded;

	/**
	 * The source url.
	 */
	private URL mSourceUrl;

	/**
	 * Constructs a new Image object used for wrapping
	 * the actual platform image implementation object.
	 *
	 * @param image The platform image object to be wrapped.
	 */
	public Image(PlatformImage image) {
		mImage = image;
	}

	/**
	 * Construct an Image that wraps a PlatformImage {@code image}
	 * which was downloaded from a URL {@code sourceUrl}.
	 */
	public Image(URL sourceUrl, PlatformImage image) {
		// Initialize other data members.
		mSourceUrl = sourceUrl;
		mFilterName = null;
		mSucceeded = true;
		mImage = image;
	}

	public int size() {
	    return mImage != null ? mImage.size() : 0;
	}

	/**
	 * Modifies the source URL of this result. Necessary for when the
	 * result is constructed before it is associated with data.
	 */
	public void setSourceURL(URL url) {
		throw new RuntimeException("Not currently supported.");
	}

	/**
	 * Returns the source URL for this image.
	 */
	public URL getSourceUrl() {
		return mSourceUrl;
	}

	/**
	 * Returns the name of the transform applied to this result.
	 */
	public String getFilterName() {
		return mFilterName;
	}

	/**
	 * Sets the name of the transform applied to this result.
	 */
	public void setTransformName(Transform transform) {
		mFilterName = transform.getName();
	}

	/**
	 * Returns true if operations on the Image succeeded, else
	 * false.
	 */
	public boolean getSucceeded() {
		return mSucceeded;
	}

	/**
	 * Sets whether operations on the Image succeeded or failed.
	 */
	public void setSucceeded(boolean succeeded) {
		mSucceeded = succeeded;
	}

	/**
	 * Returns the format of the image from the URL in string form.
	 */
	public String getFormatName() {
		URL url = getSourceUrl();
		String format =
				url.getFile().substring
						(url.getFile().lastIndexOf('.') + 1);
		return format.equalsIgnoreCase("jpeg") ? "jpg" : format;
	}

	/**
	 * Applies a transformation to this image.
     *
     * @param type The type of transformation to perform.
	 * @return A new transformed image.
	 */
	public Image applyTransform(Transform.Type type, Cache.Item item) {
		PlatformImage platformImage =
				mImage.applyTransform(type, item);
		return new Image(platformImage);
	}

	/**
     * Writes the image bytes to the output stream.
     *
	 * @param outputStream Output stream to write to.
	 * @throws IOException
	 */
	public void writeImage(OutputStream outputStream) throws IOException {
		mImage.writeImage(outputStream);
	}
}
