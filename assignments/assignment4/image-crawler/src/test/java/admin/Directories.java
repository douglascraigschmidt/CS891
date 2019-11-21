package admin;

import java.io.File;

import edu.vanderbilt.imagecrawler.platform.Platform;
import edu.vanderbilt.imagecrawler.utils.Options;

import static org.junit.Assert.assertTrue;

/**
 * Centralizes all test directory helpers.
 */
public class Directories {
    /**
     * These fields are used to locate the project resources directory.
     */
    public static final String ASSETS_PATH = "app/src/main/assets";
    public static final String PROJECT_PATH = ".";
    public static final String PROJECT_ROOT_PATH = "..";
    public static final String LOCAL_WEB_PAGES_DIR_NAME = Platform.LOCAL_WEB_PAGES_DIR_NAME;
    public static final String GROUND_TRUTH_DIR_NAME = "ground-truth";

    /**
     * @return this library project's resource directory.
     */
    public static File getJavaGroundTruthDir() throws Exception {
        File dir = new File(getJavaFilesDir(), GROUND_TRUTH_DIR_NAME);
        dir.mkdirs();
        assertTrue(dir.isDirectory());
        return dir;
    }

    /**
     * @return this library project's resource directory.
     */
    public static File getJavaLocalWebPagesDir() throws Exception {
        File dir = new File(getJavaFilesDir(), LOCAL_WEB_PAGES_DIR_NAME);
        dir.mkdirs();
        assertTrue(dir.isDirectory());
        return dir;
    }

    /**
     * @return The directory where Java version ground-truth
     * and local web-pages are stored.
     */
    public static File getJavaDownloadsDir() throws Exception {
        File dir = new File(
                getProjectDir(), Options.DEFAULT_DOWNLOAD_DIR_NAME);
        assertTrue(dir.isDirectory());
        return dir;
    }

    /**
     * @return this library project's resource directory.
     */
    public static File getJavaFilesDir() throws Exception {
        return getProjectDir();
    }

    /**
     * @return this library project's resource directory.
     */
    public static File getAndroidGroundTruthDir() throws Exception {
        File dir = new File(getAndroidFilesDir(), GROUND_TRUTH_DIR_NAME);
        dir.mkdirs();
        assertTrue(dir.isDirectory());
        return dir;
    }

    /**
     * @return this library project's resource directory.
     */
    public static File getAndroidLocalWebPagesDir() throws Exception {
        File dir = new File(getAndroidFilesDir(), LOCAL_WEB_PAGES_DIR_NAME);
        dir.mkdirs();
        assertTrue(dir.isDirectory());
        return dir;
    }

    /**
     * @return this library project's resource directory.
     */
    public static File getAndroidFilesDir() throws Exception {
        return getAssetsDir();
    }

    /**
     * @return the project's assets directory.
     */
    public static File getAssetsDir() throws Exception {
        File dir = new File(getProjectRootDir(), ASSETS_PATH);
        dir.mkdirs();
        assertTrue(dir.isDirectory());
        return dir;
    }

    /**
     * @return The top-level root project directory.
     */
    public static File getProjectRootDir() throws Exception {
        File dir = new File(PROJECT_ROOT_PATH).getCanonicalFile();
        assertTrue(dir.isDirectory());
        return dir;
    }


    /**
     * @return The directory where Java version ground-truth
     * and local web-pages are stored.
     */
    public static File getProjectDir() throws Exception {
        File dir = new File(PROJECT_PATH).getCanonicalFile();
        assertTrue(dir.isDirectory());
        return dir;
    }
}
