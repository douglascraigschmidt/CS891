package edu.vanderbilt.imagecrawler.transforms;

import java.io.IOException;
import java.io.OutputStream;

import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.utils.ImageCache;

/**
 * A Decorator whose inherited applyTransform() template method calls the
 * transform() method on the Transform object passed to its constructor and
 * whose decorate() hook method then writes the results of the
 * transformed image to an output file.  Plays the role of the "Concrete
 * Decorator" in the Decorator pattern and the role of the "Concrete
 * Class" in the Template Method pattern.
 */
public class CachingTransformDecorator
       extends TransformDecorator {
    /**
     * Reference to platform dependent image cache implementation.
     */
    private final ImageCache.Item mCacheItem;

    /**
     * Constructor passes the {@code transform} parameter up to the superclass
     * constructor, which stores it in a data member for subsequent
     * use in applyTransform(), which is both a hook method and a
     * template method.
     */
    public CachingTransformDecorator(Transform transform, ImageCache.Item cacheItem) {
        super(transform);
        mCacheItem = cacheItem;
    }

    /**
     * This hook method is called with the {@code image} parameter after it
     * has been transformed with mTransform in the inherited applyTransform()
     * method.  decorate() stores the transformed Image in a file.
     */
    @Override
    protected Image decorate(Image image) {
        // Store the transformed image as its filename (which is derived
        // from its URL), within the appropriate transform directory to
        // organize the transformed results and write the image to the
        // file in the appropriate directory.

        // Store the image using try-with-resources
        try (OutputStream outputFile = mCacheItem.getOutputStream()) {
            image.writeImage(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        return image;
    }
}
