package edu.vanderbilt.imagecrawler.helpers;

import java.util.Arrays;
import java.util.Collections;

import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaPlatform;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultJavaLocalRootUrl;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.getDefaultWebRootUrl;

/**
 * Created by monte on 2017-09-19.
 */

public class Controllers {
    /**
     * @return A local or web controller for assignment 3a.
     */
    public static Controller buildAssignment3aController(boolean local) throws Exception {
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
