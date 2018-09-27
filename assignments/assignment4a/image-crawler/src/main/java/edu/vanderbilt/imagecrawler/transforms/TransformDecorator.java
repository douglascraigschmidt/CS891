package edu.vanderbilt.imagecrawler.transforms;

import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.utils.Image;

/**
 * Allows the addition of behavior to a Transform object transparently
 * without affecting the behavior of other Transform objects it
 * encapsulates.  Plays the role of the "Decorator" in the Decorator
 * pattern and the role of the "Abstract Class" in the Template Method
 * pattern.
 */
public abstract class TransformDecorator
		extends Transform {
    /**
     * The Transform that's being decorated.
     */
    protected Transform mTransform;

    /**
     * Constructor initializes superclass and data member with the
     * given {@code transform}.
     */
    public TransformDecorator(Transform transform) {
        super(transform.getName());
        mTransform = transform;
    }

    /**
     * An abstract hook method that "decorates" the Transform data member
     * by being applied to the imageEntity after it's been transformed.
     */
    protected abstract Image decorate(Image imageEntity);

    /**
     * This hook method is also a template method that forwards to the
     * decorated transform to transform the {@code imageEntity} parameter.
     */
    @Override
    protected Image applyTransform(Image image, Cache.Item item) {
        return decorate(mTransform.transform(image, item));
    }
}
