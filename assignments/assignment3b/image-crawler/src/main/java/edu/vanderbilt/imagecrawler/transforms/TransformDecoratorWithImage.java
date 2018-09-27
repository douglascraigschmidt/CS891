package edu.vanderbilt.imagecrawler.transforms;

import edu.vanderbilt.imagecrawler.utils.Image;

/**
 * Command object that associates a transform with an image.
 */
public class TransformDecoratorWithImage {
    /**
     * The transform decorator.
     */
    public TransformDecorator mTransformDecorator;

    /**
     * The image.
     */
    public Image mImage;

    /**
     * Constructor initializes the fields.
     */
    public TransformDecoratorWithImage(TransformDecorator transformDecorator,
                                       Image image) {
        mTransformDecorator = transformDecorator;
        mImage = image;
    }

    /**
     * Run the transform decorator on the image.
     */
    public Image run() {
        return mTransformDecorator.transform(mImage);
    }
}
