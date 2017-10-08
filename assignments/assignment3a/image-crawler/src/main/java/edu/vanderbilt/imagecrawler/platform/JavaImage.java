package edu.vanderbilt.imagecrawler.platform;

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
}
