package admin;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.crawlers.CrawlerType;
import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.JavaCache;
import edu.vanderbilt.imagecrawler.platform.Platform;
import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.AdminUtils;
import edu.vanderbilt.imagecrawler.utils.Options;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Helper methods only used for assignment testing and administration.
 */
public class AdminHelpers {

    /**
     * Helper method that centralizes all crawl building test methods so
     * that they predictably use the same parametrized crawler/downloader.
     *
     * @param crawlType  The crawler type to use.
     * @param controller Controller with options and transforms.
     * @param index      boolean flag indicating if indexing should be performed
     *                   on the downloaded directory.
     */
    public static void downloadIntoDirectory(
            CrawlerType crawlType,
            Controller controller,
            boolean index) {

        info("----------------------------------------------------");
        info("Downloading from:   " + controller.mOptions.mRootUrl);
        info("Downloading into:   " + controller.mOptions.mDownloadDirName);
        System.out.print("      Transforms:   ");

        for (int i = 0; i < controller.mTransforms.size(); i++) {
            String name = controller.mTransforms.get(i).toString();
            if (i == 0) {
                info(name);
            } else {
                info("                    " + name);
            }
        }

        info("----------------------------------------------------");

        File cacheDir = controller.getCacheDir();

        // Leverage the handy static delete contents cache
        // method to clear the cache before starting the crawl.
        info("Clearing directory: " + cacheDir);
        JavaCache.instance().clear();

        info("Running with crawl strategy " + crawlType.name());

        // Create and run the crawler.
        ImageCrawler.Factory.newCrawler(crawlType, controller).run();

        info("----------------------------------------------------");
        if (index) {
            info("Indexing directory: " + cacheDir);
            controller.mTransforms.forEach(it ->
                    AdminUtils.indexDirectory(
                            cacheDir.getAbsolutePath(),
                            it.getName()));
        }
        info("----------------------------------------------------");
    }

    /**
     * Helper method that builds a Controller using the passed parameters
     * and passes this into the overloaded downloadIntoDirectory.
     *
     * @Param rootUrl   The root url to start crawling from.
     * @param transforms List of transforms to add or null for the default
     *                   transform (NULL_TRANSFORM).
     * @param crawlType  The crawler type to use.
     * @param destDir    Destination directory.
     * @param index      boolean flag indicating if indexing should be performed
     *                   on the downloaded directory.
     */
    public static void downloadIntoDirectory(String rootUrl,
                                             CrawlerType crawlType,
                                             List<Transform.Type> transforms,
                                             File destDir,
                                             boolean index)
            throws Exception {

        // Create a new controller that will output to ground-truth dir.
        Controller controller =
                Controller.newBuilder()
                        .transforms(transforms)
                        .rootUrl(rootUrl)
                        .downloadPath(destDir.getAbsolutePath())
                        .build();

        downloadIntoDirectory(crawlType, controller, index);
    }

    /**
     * Recursively compare the contents of the directory of downloaded
     * files with the contents of the "ground truth" directory.
     *
     * Note that the local parameter is required because images that
     * originate from the local web-pages directory will have different
     * cache file names from those originating from the web. Specifically,
     * an image file from the web will have an addition "project_root"
     * string in the file name. To work around this difference, this
     * method strips off the "project_root" string from the path before
     * comparing files.
     */
    public static void recursivelyCompareDirectories(File expected,
                                                     File generated)
            throws IOException {
        // Checks parameters.
        TestCase.assertTrue("Generated Folder doesn't exist: " + generated,
                generated.exists());
        TestCase.assertTrue("Generated is not a folder?!?!: " + generated,
                generated.isDirectory());

        TestCase.assertTrue("Expected Folder doesn't exist: " + expected,
                expected.exists());
        TestCase.assertTrue("Expected is not a folder?!?!: " + expected,
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
                        long thisLength = thisBytes.length;
                        long otherLength = otherBytes.length;

                        // @@Doug - this always succeeds.
                        if (thisLength != otherLength) {
                            fail(file + " length should be equal to " + fileInOther);
                        }

                        // @@Doug - this always succeeds.
                        for (int i = 0; i < thisBytes.length; i++) {
                            if (thisBytes[i] != otherBytes[i]) {
                                fail(file + " bytes should be equal to " + fileInOther);
                            }
                        }

                        // @@Doug - both these calls fail for all images.
                        if (false) {
                            if (Arrays.equals(otherBytes, thisBytes)) {
                                System.out.println(file + " should be equal to " + fileInOther);

                            }

                            // @@Doug - this call fails for all images.
                            assertEquals(file + " should be equal to " + fileInOther,
                                    otherBytes, thisBytes);
                        }

                        return result;
                    }
                });
    }

    /**
     * Recursively copies a folder to a new location.
     */
    public static void copyDir(File srcDir, File destDir) throws Exception {
        destDir.mkdirs();
        try (Stream<Path> stream = Files.walk(srcDir.toPath())) {
            stream.forEach(path -> {
                Path toPath = destDir.toPath()
                        .resolve(srcDir.toPath().relativize(path));
                if (path.toFile().isDirectory()) {
                    toPath.toFile().mkdir();
                } else {
                    try {
                        Files.copy(path, toPath, REPLACE_EXISTING);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

   /**
     * @return Local web pages directory for Java testing.
     */
    public static String getDefaultWebRootUrl() throws Exception {
        return Options.DEFAULT_WEB_URL;
    }

    /**
     * @return Local web pages directory for Java testing.
     */
    public static String getDefaultJavaLocalRootUrl() throws Exception {
        // Just swaps the http scheme for the custom project scheme + authority.
        URI uri = new URI(Options.DEFAULT_WEB_URL);
        return Platform.PROJECT_URI_PREFIX + "/" + uri.getHost() + uri.getPath();
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
