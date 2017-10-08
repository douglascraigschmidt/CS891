package edu.vanderbilt.imagecrawler.admin;

import org.junit.Test;

import java.io.File;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.imagecrawler.helpers.AdminHelpers;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.utils.AdminUtils;
import edu.vanderbilt.imagecrawler.utils.ImageCache;

import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.copyDir;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.info;
import static edu.vanderbilt.imagecrawler.helpers.Controllers.buildAssignment3aController;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaGroundTruthDir;
import static edu.vanderbilt.imagecrawler.helpers.Directories.getJavaLocalWebPagesDir;

/**
 * Helper class that has methods used to build assignments.
 */
public class AssignmentBuilder {
    @Test
    public void xxx() throws Exception {
        Controller controller = buildAssignment3aController(false);
        // Finally index the local web-pages dir so that it can be
        // locally crawled.
    }

    /**
     * Helper method that builds ground-truth and local crawl directory.
     * @param controller Controller used for the assignment.
     */
    public static void buildAssignment(Controller controller) throws Exception {
        // Use cache helper to delete cache, local, and ground-truth dirs.
        info("Deleting cache dir ...");
        ImageCache.deleteContents(controller.getCacheDir());
        info("Deleting ground-truth dir ...");
        ImageCache.deleteContents(getJavaGroundTruthDir());
        info("Deleting web-pages dir ...");
        ImageCache.deleteContents(getJavaLocalWebPagesDir());

        info("Running crawler ...");
        // Perform the web crawl using the sequential loops crawler.
        AdminHelpers.downloadIntoDirectory(
                ImageCrawlerBase.Type.SEQUENTIAL_LOOPS,
                controller,
                false);

        // Clone the downloaded files into the ground-truth directory.
        info("Cloning downloaded dir to ground-truth dir ...");
        copyDir(controller.getCacheDir(), getJavaGroundTruthDir());

        // Use project's AdminUtils helper to index the local web-pages
        // so that it can be locally crawled.
        info("Indexing web-pages dir ...");
        AdminUtils.indexDirectory(getJavaLocalWebPagesDir());
    }
}
