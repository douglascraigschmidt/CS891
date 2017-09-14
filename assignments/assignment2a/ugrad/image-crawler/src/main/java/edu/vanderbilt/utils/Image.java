package edu.vanderbilt.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import edu.vanderbilt.filters.Filter;
import edu.vanderbilt.platform.PlatformImage;

/**
 * Stores platform-independent meta-data about an Image and also
 * provides methods for common image- and file-related tasks, such as
 * decoding raw byte arrays into an Image and setting/getting filter
 * and file names.
 */
public class Image {
	private PlatformImage mImage;

	/**
	 * The name of the filter that was applied to this result.
	 */
	private String mFilterName;
	/**
	 * Keeps track of whether operations on this Image succeed.
	 */
	private boolean mSucceeded;

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
	 * Construct an Image from a byte array of @a imageData
	 * downloaded from a URL @a source.
	 */
	public Image(
			URL sourceURL,
			byte[] imageData) {
		// Initialize other data members.
		mFilterName = null;
		mSucceeded = true;

		// Decode the imageData into a Bitmap.
		mImage.setImage(sourceURL, imageData);
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
		return mImage.getSourceUrl();
	}

	/**
	 * Returns the name of the filter applied to this result.
	 */
	public String getFilterName() {
		return mFilterName;
	}

	/**
	 * Sets the name of the filter applied to this result.
	 */
	public void setFilterName(Filter filter) {
		mFilterName = filter.getName();
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
	 * Returns the file name from the URL this Image was
	 * constructed from.
	 */
	public String getFileName() {
		return mImage.mapUrlToRelativeFilePath();
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

	public Image applyFilter() {
		PlatformImage platformImage = mImage.applyFilter();
		return new Image(platformImage);
	}

	public void writeImageFile(FileOutputStream outputStream) throws IOException {
		mImage.writeImageFile(outputStream);
	}
}
