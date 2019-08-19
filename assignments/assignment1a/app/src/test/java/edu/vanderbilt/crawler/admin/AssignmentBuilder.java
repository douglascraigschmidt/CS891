package edu.vanderbilt.crawler.admin;

import org.apache.commons.io.FileUtils;

import edu.vanderbilt.crawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static edu.vanderbilt.crawler.helpers.AdminHelpers.info;
import static edu.vanderbilt.crawler.helpers.AndroidDirectories.getAndroidGroundTruthDir;
import static edu.vanderbilt.crawler.helpers.AndroidDirectories.getAndroidLocalWebPagesDir;
import static edu.vanderbilt.imagecrawler.utils.AdminUtils.buildLocalWebPages;

/**
 * Helper class that has methods used to build assignments.
 */
public class AssignmentBuilder {
    /**
     * Helper method that builds ground-truth and local crawl directory.
     *
     * @param controller Controller used for the assignment.
     */
    public static void buildAndroidAssignment(Controller controller) throws Exception {
        // Use cache helper to remove cache, local, and ground-truth dirs.
        info("Deleting cache dir ...");
        Cache.deleteContents(controller.getCacheDir());
        info("Deleting ground-truth dir ...");
        Cache.deleteContents(getAndroidGroundTruthDir());
        info("Deleting web-pages dir ...");
        Cache.deleteContents(getAndroidLocalWebPagesDir());

        info("Running crawler ...");
        // Perform the web crawl using the sequential streams crawler.
        AdminHelpers.downloadIntoDirectory(
                ImageCrawler.Type.SEQUENTIAL_LOOPS,
                controller,
                false);

        // Clone the downloaded files into the ground-truth directory.
        info("Cloning downloaded dir to ground-truth dir ...");
        FileUtils.copyDirectory(controller.getCacheDir(), getAndroidGroundTruthDir());

        // Clone the downloaded untransformed image files into
        // the Android assets directory.
        info("Cloning downloaded dir to web-pages dir ...");
        buildLocalWebPages(controller.getCacheDir(), getAndroidLocalWebPagesDir());
    }
}
