package edu.vanderbilt.platform;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Stores platform-specific meta-data about an Image and also provides
 * methods for common image- and file-related tasks.  This
 * implementation is specific to the Java platform.
 */
public class JavaImage implements PlatformImage {
	/**
	 * The Bitmap our Image stores.
	 */
	private BufferedImage mImage;

	/**
	 * The original source of this image.
	 */
	private URL mUrl;

	/**
	 * Platform helper methods passed in to constructor.
	 */
	private final Platform mPlatform;

	/**
	 * Package only constructor only accessed by Platform.
	 */
	JavaImage(URL sourceURL, byte[] imageData, Platform platform) {
		mPlatform = platform;
		setImage(sourceURL, imageData);
	}

	/**
	 * Private constructor only accessed internally by this class.
	 */
	private JavaImage(URL sourceUrl, BufferedImage image, Platform platform) {
		mPlatform = platform;
		mUrl = sourceUrl;
		mImage = image;
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
	 * Decodes a byte[] into an @a Image that can be used in the rest
	 * of the application.
	 */
	@Override
	public void setImage(URL url, byte[] imageData) {
		try {
			mUrl = url;
			mImage = ImageIO.read(new ByteArrayInputStream(imageData));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the @a image to the @a outputStream.
	 */
	@Override
	public void writeImageFile(FileOutputStream outputStream)
			throws IOException {
		BufferedImage bufferedImage = mImage;
		if (bufferedImage == null) {
			System.out.println("null image");
		} else {
			ImageIO.write(bufferedImage,
					"png",
					outputStream);
		}
	}

	/**
	 * Uses the Java platform color transformation values for
	 * grayscale conversion using a pixel-by-pixel coloring algorithm.
	 */
	@Override
	public PlatformImage applyFilter() {
		// Forward to the platform-specific implementation of this
		// filter.
		BufferedImage originalImage = mImage;
		BufferedImage grayScaleImage =
				new BufferedImage
						(originalImage.getColorModel(),
								originalImage.copyData(null),
								originalImage.getColorModel().isAlphaPremultiplied(),
								null);

		boolean hasTransparent =
				grayScaleImage.getColorModel().hasAlpha();
		int width = grayScaleImage.getWidth();
		int height = grayScaleImage.getHeight();

		// A common pixel-by-pixel grayscale conversion algorithm
		// using values obtained from en.wikipedia.org/wiki/Grayscale.
		for (int i = 0; i < height; ++i) {
			for (int j = 0; j < width; ++j) {

				// Check if the pixel is transparent in the original.
				if (hasTransparent
						&& (grayScaleImage.getRGB(j,
						i) >> 24) == 0x00) {
					continue;
				}

				// Convert the pixel to grayscale.
				Color c = new Color(grayScaleImage.getRGB(j,
						i));
				int grayConversion =
						(int) (c.getRed() * 0.299)
								+ (int) (c.getGreen() * 0.587)
								+ (int) (c.getBlue() * 0.114);
				Color grayScale = new Color(grayConversion,
						grayConversion,
						grayConversion);
				grayScaleImage.setRGB(j, i, grayScale.getRGB());
			}
		}

		return new JavaImage(getSourceUrl(), grayScaleImage, mPlatform);
	}
}
