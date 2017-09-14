package edu.vanderbilt.imagecrawler;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import edu.vanderbilt.crawlers.SequentialStreamsCrawler;
import edu.vanderbilt.crawlers.framework.CrawlerFactory;
import edu.vanderbilt.filters.Filter;
import edu.vanderbilt.filters.FilterFactory;
import edu.vanderbilt.platform.Device;
import edu.vanderbilt.platform.JavaPlatform;
import edu.vanderbilt.utils.CacheUtils;
import edu.vanderbilt.utils.Options;
import edu.vanderbilt.utils.WebPageCrawler;

import static edu.vanderbilt.filters.FilterFactory.Type.NULL_FILTER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertTrue;

/**
 * Created by monte on 2017-09-10.  Do *not* run this test without
 * checking with the instructor!
 */

public class GenerateGroundTruthDirectory {
    /**
     * These fields are used to locate the project resources directory.
     */
    private static final String ASSETS_PATH = "app/src/main/assets";
    private static final String RESOURCES_PATH = "image-crawler/src/main/resources";
    private static final String GROUND_TRUTH_DIR_NAME = "ground-truth";

    /**
     * @Return this library project's resource directory.
     */
    private static File getResourcesDir() throws Exception {
        File resDir = new File(new File(".").getCanonicalFile(), RESOURCES_PATH);
        assertTrue(resDir.isDirectory());
        return resDir;
    }

    /**
     * @Return this parent project's assets directory.
     */
    private static File getAssetsDir() throws Exception {
        File assetsDir = new File(new File(".").getCanonicalFile(), ASSETS_PATH);
        assertTrue(assetsDir.isDirectory());
        return assetsDir;
    }

    /**
     * Downloads default images from the web using a all filters into
     * a resources ground-truth directory that can be used by JUnit
     * test to check the results of assignments.
     */
    @Test
    public void downloadFromWebIntoGroundTruthResourcesDirectory() throws Exception {
        System.out.println("Starting download into ground-truth directory.");

        // Device protects itself from multiple builds and this
        // method gets around that restriction.
        Device.setPrivateInstanceFieldToNullForUnitTestingOnly();

        // Create a new device with a local page crawler.
        Device.newBuilder()
            .platform(new JavaPlatform())
            .options(Options.newBuilder()
                     .local(false)
                     .downloadDirName(GROUND_TRUTH_DIR_NAME)
                     .diagnosticsEnabled(true)
                     .build())
            .crawler(new WebPageCrawler())
            .build();

        // Clear cache first.
        CacheUtils.clearCache();

        List<FilterFactory.Type> filterTypes =
            Collections.singletonList(FilterFactory.Type.NULL_FILTER);

        // Create and run the crawler.
        CrawlerFactory.newCrawler(CrawlerFactory.Type.SEQUENTIAL_STREAMS,
                                  filterTypes,
                                  Device.options().getRootUri()).run();

        /**
         * No need to index this directory since when students do a download
         * into the cache there will not be any index.html files.
         */
        //System.out.println("Indexing ...");
        //filterTypes.stream().forEach(type -> CacheUtils.indexCache(type.toString()));

        // Delete any existing ground truth directory.
        System.out.println("Deleting old ground truth directory from resources ...");
        File groundTruthDir = new File(getResourcesDir(), GROUND_TRUTH_DIR_NAME);
        CacheUtils.deleteSubFolders(groundTruthDir);
        if (groundTruthDir.isDirectory()) {
            assertTrue(groundTruthDir.delete());
        }

        // Move the new ground truth directory from the cache into the resources.
        System.out.println("Copying to resources directory ...");
        Files.move(CacheUtils.getCacheDir().toPath(),
                   (new File(getResourcesDir(), groundTruthDir.getName()).toPath()));
    }
}

