package edu.vanderbilt.imagecrawler.platform;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.transforms.Transform;

/**
 * Stores platform-specific meta-data about an Image and also provides
 * methods for common image- and file-related tasks.  This
 * implementation is specific to the Java platform.
 */
public class JavaImage implements PlatformImage {
    /**
     * Cache item used to report progress.
     */
    Cache.Item mCacheItem;
    /**
     * The Bitmap our Image stores.
     */
    private BufferedImage mImage;
    /**
     * Size of image.
     */
    private int mSize = 0;

    /**
     * Package only constructor only accessed by Platform.
     */
    JavaImage(InputStream inputStream, Cache.Item item) {
        setImage(inputStream, item);
    }

    /**
     * Private constructor only accessed internally by this class.
     */
    private JavaImage(BufferedImage image) {
        mImage = image;
    }

    /**
     * Decodes a input stream into an @a Image that can be used in the rest
     * of the application.
     */
    @Override
    public void setImage(InputStream inputStream, Cache.Item item) {
        try {
            mSize = inputStream.available();
            mImage = ImageIO.read(inputStream);
            mCacheItem = item;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the @a image to the @a mOutputStream.
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
    public PlatformImage applyTransform(Transform.Type type, Cache.Item item) {
        switch (type) {
            case GRAY_SCALE_TRANSFORM:
                return grayScale();
            case NULL_TRANSFORM:
            default:
                return this;
        }
    }

    /**
     * @return Size of image.
     */
    @Override
    public int size() {
        return mSize;
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
        int total = width * height;
        int bytes = 0;

        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                ImageCrawler.throwExceptionIfCancelled();

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

                bytes++;
            }

            mCacheItem.progress(Cache.Operation.TRANSFORM, (float) bytes / total, bytes);
        }

        mCacheItem.progress(Cache.Operation.CLOSE, 1f, total);

        return new JavaImage(grayScaleImage);
    }
}
