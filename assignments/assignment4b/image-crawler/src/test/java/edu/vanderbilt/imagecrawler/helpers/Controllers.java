package edu.vanderbilt.imagecrawler.helpers;

import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaPlatform;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultJavaLocalRootUrl;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultWebRootUrl;

/**
 * Centralizes all controllers used for assignment creation
 * and testing.  For a new assignment version, add it's
 * controller here.
 */
public class Controllers {
    /**
     * @return A local or web controller for assignment 3a (same as Assignment2c).
     */
    public static Controller buildAssignment3bController(boolean local) throws Exception {
        return Controller.newBuilder()
                // Use a Java platform dependant helper object.
                .platform(new JavaPlatform())

                // Set the crawler to use the local web pages folder.
                .rootUrl(local? getDefaultJavaLocalRootUrl() : getDefaultWebRootUrl())

                // The maximum crawl depth.
                .maxDepth(3)

                // Build the controller.
                .build();
    }
}
