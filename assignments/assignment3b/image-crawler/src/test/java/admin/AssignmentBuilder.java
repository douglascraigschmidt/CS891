package admin;

import java.io.File;

import edu.vanderbilt.imagecrawler.crawlers.CrawlerType;
import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.platform.Controller;

import static admin.AdminHelpers.copyDir;
import static admin.AdminHelpers.info;
import static admin.Directories.getAndroidGroundTruthDir;
import static admin.Directories.getAndroidLocalWebPagesDir;
import static admin.Directories.getJavaGroundTruthDir;
import static admin.Directories.getJavaLocalWebPagesDir;
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
            CrawlerType crawlerType) throws Exception {

        File dir;

        // Use cache helper to delete cache, local, and ground-truth dirs.
        dir = controller.getCacheDir();
        info("Deleting Java cache dir " + dir + "...");
        Cache.deleteContents(dir);

        dir = getJavaGroundTruthDir();
        info("Deleting Java ground-truth dir " + dir + "...");

        dir = getJavaLocalWebPagesDir();
        info("Deleting Java web-pages dir " + dir + "...");
        Cache.deleteContents(dir);

        dir = getAndroidGroundTruthDir();
        info("Deleting Android ground-truth dir " + dir + "...");
        Cache.deleteContents(dir);

        dir = getAndroidLocalWebPagesDir();
        info("Deleting Android web-pages dir " + dir + "...");
        Cache.deleteContents(dir);

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

        // Finally, clear out the Java image-cache directory.
        dir = controller.getCacheDir();
        info("Deleting Java cache dir " + dir + "...");
        Cache.deleteContents(dir);

        // And, delete the cache root directory itself.
        dir.delete();
    }
}
