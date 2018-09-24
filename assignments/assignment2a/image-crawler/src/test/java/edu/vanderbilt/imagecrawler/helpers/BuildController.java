package edu.vanderbilt.imagecrawler.helpers;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaPlatform;
import edu.vanderbilt.imagecrawler.transforms.Transform;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultJavaLocalRootUrl;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultWebRootUrl;

/**
 * Centralizes all controllers used for assignment creation
 * and testing.  For a new assignment version, add it's
 * controller here.
 */
public class BuildController {
    /**
     * @return A local or web controller for assignment 4a.
     */
    public static Controller build(boolean local) throws Exception {
        return Controller.newBuilder()
                // Use a Java platform dependant helper object.
                .platform(new JavaPlatform())

                // Set the crawler to use the local web pages folder.
                .rootUrl(local ? getDefaultJavaLocalRootUrl() : getDefaultWebRootUrl())

                // The maximum crawl depth.
                .maxDepth(3)

                // Use all transforms for the tests.
                .transforms(Stream.of(
                        Transform.Type.NULL_TRANSFORM,
                        Transform.Type.GRAY_SCALE_TRANSFORM)
                        .collect(Collectors.toList()))

                // Build the controller.
                .build();
    }
}
