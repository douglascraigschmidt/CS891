package edu.vanderbilt.imagecrawler.helpers;

import junit.framework.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import edu.vanderbilt.crawlers.framework.CrawlerFactory;
import edu.vanderbilt.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.filters.FilterFactory;
import edu.vanderbilt.platform.Device;
import edu.vanderbilt.platform.JavaPlatform;
import edu.vanderbilt.utils.CacheUtils;
import edu.vanderbilt.utils.Options;
import edu.vanderbilt.utils.WebPageCrawler;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestHelpers {
    public static void testDownloadCacheMatchesGroundTruthFiles(
            String groundTruthDirPath,
            boolean localImageCrawl) throws Exception {
        // Device protects itself from multiple builds and this
        // method gets around that restriction.
        Device.setPrivateInstanceFieldToNullForUnitTestingOnly();

        // Create a new device along with default options so that we
        // can call CacheUtils.getCacheDir(). This method needs to get
        // the default download directory name from the Options
        // instance that lives in the Device object.
        Device.newBuilder()
                .platform(new JavaPlatform())
                .options(Options.newBuilder()
                        .local(localImageCrawl)
                        .diagnosticsEnabled(true)
                        .build())
                .crawler(new WebPageCrawler())
                .build();

        File groundTruthDir = new File(groundTruthDirPath);
        File downloadCacheDir = CacheUtils.getCacheDir();

        // First clear the cache of any files from a previous run.
        CacheUtils.clearCache();

        // This assignment only uses the NULL_FILTER.
        List<FilterFactory.Type> filterTypes =
                Collections.singletonList(FilterFactory.Type.NULL_FILTER);

        // Create and run the crawler.
        CrawlerFactory.newCrawler(
                CrawlerFactory.Type.SEQUENTIAL_STREAMS,
                filterTypes,
                Device.options().getRootUri()).run();

        // Call helper method to recursively compare the downloadCacheDir
        // directory with groundTruthDir directory.
        recursivelyCompareDirectories(groundTruthDir,
                downloadCacheDir);
    }

    /**
     * Recursively compare the contents of the directory of downloaded
     * files with the contents of the "ground truth" directory.
     */
    public static void recursivelyCompareDirectories(File expected,
                                               File generated)
            throws IOException {
        // Checks parameters.
        assertTrue("Generated Folder doesn't exist: " + generated,
                generated.exists());
        assertTrue("Generated is not a folder?!?!: " + generated,
                generated.isDirectory());

        assertTrue("Expected Folder doesn't exist: " + expected,
                expected.exists());
        assertTrue("Expected is not a folder?!?!: " + expected,
                expected.isDirectory());

        Files.walkFileTree(expected.toPath(),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir,
                                                             BasicFileAttributes attrs)
                            throws IOException {
                        FileVisitResult result =
                                super.preVisitDirectory(dir, attrs);

                        // Get the relative file name from
                        // path "expected"
                        Path relativize =
                                expected.toPath().relativize(dir);

                        // Construct the path for the
                        // counterpart file in "generated"
                        File otherDir =
                                generated.toPath().resolve(relativize).toFile();

                        assertEquals("Folders doesn't contain same file!?!?",
                                Arrays.toString(dir.toFile().list()),
                                Arrays.toString(otherDir.list()));
                        return result;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file,
                                                     BasicFileAttributes attrs)
                            throws IOException {
                        FileVisitResult result =
                                super.visitFile(file, attrs);

                        // Get the relative file name from
                        // path "expected".
                        Path relativize =
                                expected.toPath().relativize(file);

                        // Construct the path for the
                        // counterpart file in "generated".
                        Path fileInOther =
                                generated.toPath().resolve(relativize);

                        byte[] otherBytes = Files.readAllBytes(fileInOther);
                        byte[] thisBytes = Files.readAllBytes(file);

                        if (!Arrays.equals(otherBytes, thisBytes))
                            throw new AssertionFailedError(file + " is not equal to " + fileInOther);

                        return result;
                    }
                });
    }

    /**
     * Helper to make printing output less verbose.
     */
    public static void info(String msg) {
        System.out.println(msg);
    }

    /**
     * Helper to make printing output less verbose.
     */
    public static void error(String msg) {
        System.err.println(msg);
    }
}
