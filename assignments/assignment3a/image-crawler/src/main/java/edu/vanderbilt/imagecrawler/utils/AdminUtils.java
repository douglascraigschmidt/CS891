package edu.vanderbilt.imagecrawler.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class containing static methods used for
 * project administration purposes.
 */
public class AdminUtils {
    /**
     * HTML format string for a page title.
     */
    private static final String titleFormat = "Images in %1$s";

    /**
     * HTML format string for a image link.
     */
    private static final String imageFormat =
            "<p><img src=\"%1$s\"></p>\n";

    /**
     * HTML format string for a directory/page link.
     */
    private static final String dirFormat =
            "<li><a href=\"%1$s/index.html\">%2$s</a></li>\n";

    /**
     * HTML format string for index.html file.
     */
    private static String indexFormat =
            "<html><head><meta http-equiv=\"Content-Type\" "
                    + "content=\"text/html; charset=UTF-8\">\n"
                    + "<title>%1$s</title>\n"
                    + "</head>\n"
                    + "<body text=\"#ffffff\" link=\"#80ffff\" "
                    + "vlink=\"#ffde00\" bgcolor=\"#000055\">\n"
                    + "<h1>%1$s</h1>\n"
                    + "<ul>\n"
                    + "%2$s"
                    + "</ul>\n"
                    + "%3$s"
                    + "</body></html>";


    /**
     * Creates index.html files for the specified cache directory and
     * of it's all sub-directories. Each index.html file will contain
     * links to any discovered images and sub-folders.
     *
     * @param dirPath An absolute file system path.
     * @param subDirName The relative subdirectory path to index.
     */
    public static void indexDirectory(String dirPath, String subDirName) {
        try {
            indexDirectory(new File(new File(dirPath), subDirName));
        } catch (Exception e) {
            System.out.println("Cache indexing failed with exception: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Recursive helper method that creates index.html files for passed
     * directory and all of it's sub-directories.
     *
     * @param dir Directory in which to perform indexing.
     */
    public static void indexDirectory(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Path argument is not a directory.");
        }

        File[] files = dir.listFiles();

        // List of child directories.
        List<File> dirs = Stream.of(files)
                .filter(File::isDirectory)
                .collect(Collectors.toList());

        // HTML stub containing href links to all child directories.
        String dirsStub = dirs.stream()
                .map(file -> {
                    String s = String.format(dirFormat, file.getName(), file.getName());
                    return s;
                })
                .reduce((s, s1) -> s + s1).orElse("");

        // HTML stub containing href links to all child images.
        String imagesStub = Stream.of(files)
                .filter(File::isFile)
                .map(file -> String.format(imageFormat, file.getName()))
                .reduce((s, s1) -> s + s1).orElse("");

        // Creates the index.html file that includes both stubs.
        try {
            String title = String.format(titleFormat, dir.getName());
            FileWriter fileWriter = new FileWriter(new File(dir, "index.html"));
            fileWriter.write(String.format(indexFormat, title, dirsStub, imagesStub));
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Recursively index all children directories.
        dirs.forEach(AdminUtils::indexDirectory);
    }
}
