package edu.vanderbilt.imagecrawler.helpers;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaPlatform;
import edu.vanderbilt.imagecrawler.platform.Platform;
import edu.vanderbilt.imagecrawler.utils.ImageCache;
import edu.vanderbilt.imagecrawler.utils.Options;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class TestHelpers {
//    public static String getDefaultLocalJavaPath() throws Exception {
//        URI uri = new URI(Options.DEFAULT_WEB_URL);
//        return Platform.RESOURCES_URI_PREFIX + "/" + uri.getHost() + uri.getPath();
//    }
//
//    public static String getDefaultLocalFilePath() throws Exception {
//        URI uri = new URI(Options.DEFAULT_WEB_URL);
//        return Platform.PROJECT_URI_PREFIX+ "/" + uri.getHost() + uri.getPath();
//    }
//
//    public static void testDownloadCacheMatchesGroundTruthFiles(
//            boolean localCrawl) throws Exception {
//
//        AdminHelpers.downloadIntoDirectory( );
//        Controller controller = localCrawl
//                                ? getDefaultFileJavaController()
//                                : getDefaultWebController();
//
//        File groundTruthDir = new File(groundTruthDirPath);
//        assertTrue(groundTruthDir.isDirectory());
//
//        // Leverage the handy static delete contents cache
//        // method to clear the cache before starting the crawl.
//        File cacheDir = controller.getCacheDir();
//        ImageCache.deleteContents(cacheDir);
//
//        // Make sure that we are comparing something sensible.
//        assertNotEquals("ground-truth and cache directories should be different.",
//                        groundTruthDir.getAbsolutePath(), cacheDir.getAbsolutePath());
//
//        // Create and run the crawler.
//        ImageCrawlerBase.Factory.newCrawler(
//                ImageCrawlerBase.Type.PARALLEL_STREAMS,
//                controller).run();
//
//        // Call helper method to recursively compare the downloadCacheDir
//        // directory with groundTruthDir directory.
//        recursivelyCompareDirectories(groundTruthDir,
//                                      cacheDir);
//    }
}
