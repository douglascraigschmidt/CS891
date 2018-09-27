package edu.vanderbilt.imagecrawler.helpers;

import java.util.Arrays;
import java.util.Collections;

import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaPlatform;
import edu.vanderbilt.imagecrawler.transforms.Transform;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultJavaLocalRootUrl;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultWebRootUrl;

/**
 * Created by monte on 2017-09-19.
 */

public class Controllers {
    /**
     * @return A local or web controller for assignment 2a.
     */
    public static Controller buildAssignment2aController(boolean local) throws Exception {
        return Controller.newBuilder()
                // Use a Java platform dependant helper object.
                .platform(new JavaPlatform())

                // Set the crawler to use the local web pages folder.
                .rootUrl(local? getDefaultJavaLocalRootUrl() : getDefaultWebRootUrl())

                // Use only the NULL_TRANSFORM.
                .transforms(Collections.singletonList(Transform.Type.NULL_TRANSFORM))

                // The maximum crawl depth.
                .maxDepth(3)

                // Build the controller.
                .build();
    }

    /**
     * @return A local or web controller for assignment 2b.
     */
    public static Controller buildAssignment2bController(boolean local) throws Exception {
        return Controller.newBuilder()
                // Use a Java platform dependant helper object.
                .platform(new JavaPlatform())

                // Set the crawler to use the local web pages folder.
                .rootUrl(local? getDefaultJavaLocalRootUrl() : getDefaultWebRootUrl())

                // Use all defined transforms.
                .transforms(Arrays.asList(Transform.Type.values()))

                // The maximum crawl depth.
                .maxDepth(3)

                // Build the controller.
                .build();
    }

    /**
     * @return A local or web controller for assignment 2c (same as Assignment2b).
     */
    public static Controller buildAssignment2cController(boolean local) throws Exception {
        return Controller.newBuilder()
                // Use a Java platform dependant helper object.
                .platform(new JavaPlatform())

                // Set the crawler to use the local web pages folder.
                .rootUrl(local? getDefaultJavaLocalRootUrl() : getDefaultWebRootUrl())

                // Use all defined transforms.
                .transforms(Arrays.asList(Transform.Type.values()))

                // The maximum crawl depth.
                .maxDepth(3)

                // Build the controller.
                .build();
    }
}
