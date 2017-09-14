package edu.vanderbilt.webcrawler.platform;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import edu.vanderbilt.platform.Platform;
import edu.vanderbilt.platform.PlatformImage;
import edu.vanderbilt.webcrawler.App;
import edu.vanderbilt.webcrawler.R;

/**
 * Stores platform-specific meta-data about an Image and also provides
 * methods for common image- and file-related tasks.  This
 * implementation is specific to the Android platform.
 */
public class AndroidImage implements PlatformImage {
	/**
	 * Logging tag.
	 */
	private static final String TAG = "AndroidImage";

	/**
	 * Dimensions representing how large the scaled image should be.
	 */
	private static final int IMAGE_WIDTH = 250;
	private static final int IMAGE_HEIGHT = 250;

	/**
	 * Platform helper methods passed in to constructor.
	 */
	private final Platform mPlatform;

	/**
	 * The Bitmap image.
	 */
	private Bitmap mImage;

	/**
	 * The original source of this image.
	 */
	private URL mUrl;

	/**
	 * Package only constructor only accessed by Platform.
	 */
	AndroidImage(URL url, byte[] imageData, Platform platform) {
		mPlatform = platform;
		setImage(url, imageData);
	}

	/**
	 * Private constructor only accessed internally by this class.
	 */
	private AndroidImage(URL url, Bitmap image, Platform platform) {
		mPlatform = platform;
		mUrl = url;
		mImage = image;
	}

	@Override
	public void setImage(URL url, byte[] imageData) {
		mUrl = url;
		if (imageData == null) {
			mImage = BitmapFactory.decodeResource(
					App.Companion.getInstance().getResources(), R.mipmap.ic_launcher_round);
		} else {
			mImage = decodeSampledBitmapFromByteArray(imageData,
					IMAGE_WIDTH,
					IMAGE_HEIGHT);
		}
	}

	@Override
	public URL getSourceUrl() {
		return mUrl;
	}

	@Override
	public String mapUrlToRelativeFilePath() {
		return mPlatform.mapUrlToRelativeFilePath(mUrl);
	}

	/**
	 * Decodes and scales a bitmap from a byte array.  Adapted from
	 * developer.android.com/training/displaying-bitmaps/load-bitmap.html
	 */
	private Bitmap decodeSampledBitmapFromByteArray(
			byte[] imageData, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check
		// dimensions.
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(imageData,
				0,
				imageData.length,
				options);

		// Calculate inSampleSize.
		options.inSampleSize = calculateInSampleSize(options,
				reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set.
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(imageData,
				0,
				imageData.length,
				options);
	}

	/**
	 * Calculates the Bitmap's sampling rate to fit the given
	 * dimensions. Adapted from
	 * developer.android.com/training/displaying-bitmaps/load-bitmap.html
	 */
	private int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// Raw height and width of image.
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a
			// power of 2 and keeps both height and width larger than
			// the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	/**
	 * Write the @a image to the @a outputStream.
	 */
	@Override
	public void writeImageFile(FileOutputStream outputStream)
			throws IOException {
		if (mImage == null) {
			Log.d(TAG, "NULL image!");
		} else {
			mImage.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
			outputStream.flush();
		}
	}

	/**
	 * Apply a grayscale filter to the @a imageEntity and return it.
	 */
	public PlatformImage applyFilter() {
		// Bail out if something is wrong with the image.
		if (mImage == null) {
			return null;
		}

		Bitmap originalImage = mImage;
		Bitmap grayScaleImage =
				originalImage.copy(originalImage.getConfig(), true);

		boolean hasTransparent = grayScaleImage.hasAlpha();
		int width = grayScaleImage.getWidth();
		int height = grayScaleImage.getHeight();

		// A common pixel-by-pixel grayscale conversion algorithm
		// using values obtained from en.wikipedia.org/wiki/Grayscale.
		for (int i = 0; i < height; ++i) {
			// Break out if we've been interrupted.
			if (Thread.interrupted())
				return null;

			for (int j = 0; j < width; ++j) {

				// Check if the pixel is transparent in the original
				// by checking if the alpha is 0
				if (hasTransparent
						&& ((grayScaleImage.getPixel(j, i) & 0xff000000) >> 24) == 0) {
					continue;
				}

				// Convert the pixel to grayscale.
				int pixel = grayScaleImage.getPixel(j, i);
				int grayScale =
						(int) (Color.red(pixel) * .299
								+ Color.green(pixel) * .587
								+ Color.blue(pixel) * .114);
				grayScaleImage.setPixel(j, i, Color.rgb(grayScale,
						grayScale,
						grayScale));
			}
		}

		return new AndroidImage(getSourceUrl(), grayScaleImage, mPlatform);
	}
}
