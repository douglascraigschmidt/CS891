package edu.vanderbilt.imagecrawler.platform;

import edu.vanderbilt.imagecrawler.transforms.Transform;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

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
	 * Package only constructor only accessed by Platform.
	 */
    JavaImage(byte[] imageData) {
        setImage(imageData);
	}

	/**
	 * Private constructor only accessed internally by this class.
	 */
    private JavaImage(BufferedImage image) {
		mImage = image;
	}

	/**
	 * Decodes a byte[] into an @a Image that can be used in the rest
	 * of the application.
	 */
	@Override
    public void setImage(byte[] imageData) {
		try {
			mImage = ImageIO.read(new ByteArrayInputStream(imageData));
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
	}

	/**
	 * Write the @a image to the @a outputStream.
     *
     * @param outputStream
     */
	@Override
	public void writeImage(OutputStream outputStream)
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
    public PlatformImage applyTransform(Transform.Type type) {
        switch (type) {
            case GRAY_SCALE_TRANSFORM:
                return grayScale();
            case NULL_TRANSFORM:
            default:
                return this;
        }
    }

    private PlatformImage grayScale() {
        // Forward to the platform-specific implementation of this transform.
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

        return new JavaImage(grayScaleImage);
	}
}
