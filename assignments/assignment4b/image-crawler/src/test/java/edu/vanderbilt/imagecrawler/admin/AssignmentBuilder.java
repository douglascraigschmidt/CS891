package edu.vanderbilt.imagecrawler.admin;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.copyDir;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.info;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getAndroidGroundTruthDir;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getAndroidLocalWebPagesDir;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaLocalWebPagesDir;
import static edu.vanderbilt.imagecrawler.utils.AdminUtils.buildLocalWebPages;

/**
 * Helper class that has methods used to build assignments.
 */
public class AssignmentBuilder {
    /**
     * Helper method that builds both the Java and Android
     * ground-truth and local crawl directories.
     *
     * @param controller Controller used for the assignment.
     */
    public static void buildAssignment(
            Controller controller,
            ImageCrawler.Type crawlerType) throws Exception {
        // Use cache helper to delete cache, local, and ground-truth dirs.
        info("Deleting Java cache dir ...");
        Cache.deleteContents(controller.getCacheDir());
        info("Deleting Java ground-truth dir ...");
        Cache.deleteContents(getJavaGroundTruthDir());
        info("Deleting Java web-pages dir ...");
        Cache.deleteContents(getJavaLocalWebPagesDir());
        info("Deleting Android ground-truth dir ...");
        Cache.deleteContents(getAndroidGroundTruthDir());
        info("Deleting Android web-pages dir ...");
        Cache.deleteContents(getAndroidLocalWebPagesDir());

        info("Running crawler ...");
        // Perform the web crawl using the sequential streams crawler.
        AdminHelpers.downloadIntoDirectory(
                crawlerType,
                controller,
                false);

        // Clone the downloaded files into the ground-truth directories.
        info("Cloning downloaded dir to ground-truth dir ...");
        copyDir(controller.getCacheDir(), getJavaGroundTruthDir());
        copyDir(controller.getCacheDir(), getAndroidGroundTruthDir());

        // Build local web-pages directories that can be used for a local crawl.
        info("Cloning downloaded dir to web-pages dir ...");
        buildLocalWebPages(controller.getCacheDir(), getJavaLocalWebPagesDir());
        buildLocalWebPages(controller.getCacheDir(), getAndroidLocalWebPagesDir());
    }
}
