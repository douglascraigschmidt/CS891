package edu.vanderbilt.crawler.admin;

import edu.vanderbilt.crawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaImageCache;

import static edu.vanderbilt.crawler.helpers.AdminHelpers.copyCacheFiles;
import static edu.vanderbilt.crawler.helpers.AdminHelpers.copyDir;
import static edu.vanderbilt.crawler.helpers.AdminHelpers.info;
import static edu.vanderbilt.crawler.helpers.AndroidDirectories.getAndroidGroundTruthDir;
import static edu.vanderbilt.crawler.helpers.AndroidDirectories.getAndroidLocalWebPagesDir;
import static edu.vanderbilt.imagecrawler.utils.AdminUtils.indexDirectory;

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
        JavaImageCache.INSTANCE.deleteContents(controller.getCacheDir());
        info("Deleting ground-truth dir ...");
        JavaImageCache.INSTANCE.deleteContents(getAndroidGroundTruthDir());
        info("Deleting web-pages dir ...");
        JavaImageCache.INSTANCE.deleteContents(getAndroidLocalWebPagesDir());

        info("Running crawler ...");
        // Perform the web crawl using the sequential streams crawler.
        AdminHelpers.downloadIntoDirectory(
                ImageCrawler.Type.SEQUENTIAL_LOOPS,
                controller,
                false);

        // Clone the downloaded files into the ground-truth directory.
        info("Cloning downloaded dir to ground-truth dir ...");
        copyDir(controller.getCacheDir(), getAndroidGroundTruthDir());

        // Clone the downloaded files NULL_FILTER images into
        // the ground-truth directory.
        info("Cloning downloaded dir to web-pages dir ...");
        copyCacheFiles(controller.getCache(), getAndroidLocalWebPagesDir());

        // Use project's AdminUtils helper to index the local web-pages
        // so that it can be locally crawled.
        info("Indexing web-pages dir ...");
        indexDirectory(getAndroidLocalWebPagesDir());
    }
}
