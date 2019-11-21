package edu.vanderbilt.imagecrawler.transforms;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;

import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.utils.ExceptionUtils;
import edu.vanderbilt.imagecrawler.utils.Image;

/**
 * Command object that associates a transform with an image.
 */
public class TransformDecoratorWithImage {
    /**
     * The transform decorator.
     */
    private Transform mTransform;

    /**
     * The image.
     */
    private Image mImage;

    /**
     * Constructor initializes the fields.
     */
    public TransformDecoratorWithImage(Transform transform,
                                       Image image) {
        mTransform = transform;
        mImage = image;
    }

    /**
     * Run the transform decorator on the image.
     */
    public @Nullable Image run(Cache.Item item) {
        Image image = mTransform.transform(mImage, item);
        // Save the image to the cache.
        try (OutputStream outputStream =
                     item.getOutputStream(
                             Cache.Operation.WRITE, image.size())) {
            image.writeImage(outputStream);
        } catch (IOException e) {
            ExceptionUtils.throwAsUnchecked(e);
        }

        return image;
    }
}
