package edu.vanderbilt.imagecrawler.admin;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaImageCache;
import edu.vanderbilt.imagecrawler.utils.AdminUtils;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.copyCacheFiles;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.copyDir;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.info;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getAndroidGroundTruthDir;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getAndroidLocalWebPagesDir;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaLocalWebPagesDir;

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
        info("Deleting cache dir ...");
        JavaImageCache.INSTANCE.deleteContents(controller.getCacheDir());
        info("Deleting ground-truth dir ...");
        JavaImageCache.INSTANCE.deleteContents(getJavaGroundTruthDir());
        info("Deleting web-pages dir ...");
        JavaImageCache.INSTANCE.deleteContents(getJavaLocalWebPagesDir());

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

        // Clone the downloaded images into the web-pages directories.
        info("Cloning downloaded dir to web-pages dir ...");
        copyCacheFiles(controller.getCache(), getJavaLocalWebPagesDir());
        copyCacheFiles(controller.getCache(), getAndroidLocalWebPagesDir());

        // Use project's AdminUtils helper to index the local web-pages
        // directories so that they can be locally crawled.
        info("Indexing web-pages dir ...");
        AdminUtils.indexDirectory(getJavaLocalWebPagesDir());
        AdminUtils.indexDirectory(getAndroidLocalWebPagesDir());
    }
}
