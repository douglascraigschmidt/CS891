package edu.vanderbilt.crawler.helpers;

import java.net.URI;

import edu.vanderbilt.crawler.platform.AndroidPlatform;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaPlatform;

import static edu.vanderbilt.crawler.helpers.AdminHelpers.getDefaultJavaLocalRootUrl;
import static edu.vanderbilt.crawler.helpers.AdminHelpers.getDefaultWebRootUrl;

public class AndroidControllers {
    /**
     * @return Default controller and options for local Java crawl.
     */
    public static Controller getDefaultAndroidWebController() throws Exception {
        return buildAndroidAssignment3aController(false);
    }

    /**
     * @return Default controller and options for local Java crawl.
     */
    public static Controller getDefaultAndroidLocalController() throws Exception{
        return buildAndroidAssignment3aController(true);
    }

    /**
     * @return A local or web controller for assignment 3a (same as Assignment2c).
     */
    public static Controller buildAndroidAssignment3aController(boolean local) throws Exception {
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

    /**
     * @Return The default url mapped into the Android assets.
     */
    public static String getDefaultAndroidLocalRootUrl() throws Exception {
        URI uri = new URI(getDefaultWebRootUrl());
        return AndroidPlatform.ASSETS_URI_PREFIX + "/" + uri.getHost() + uri.getPath();
    }
}

