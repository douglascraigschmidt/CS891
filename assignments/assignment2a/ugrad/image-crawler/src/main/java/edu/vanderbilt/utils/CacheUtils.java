package edu.vanderbilt.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.vanderbilt.filters.Filter;
import edu.vanderbilt.platform.Device;

/**
 * Utility class that manages the download cache.
 */
public class CacheUtils {
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
	 * @return The platform dependent cache directory.
	 */
	public static File getCacheDir() {
		return new File(Device.platform().getCacheDirPath());
	}

	/**
	 * @return The platform dependent root cache directory path.
	 */
	public static String getCacheDirPath() {
		return Device.platform().getCacheDirPath();
	}

	/**
	 * Maps a url to a file that within the cache directory. The resulting file
	 * will automatically have all ancestor folders create so that file operations
	 * can be immediately performed on the return File object.
	 *
	 * @param parentPath A relative path.
	 * @param url        A url to map.
	 * @return A cache file with ancestor directories created.
	 */
	public static File mapUrlToCacheFile(String parentPath, URL url) {
		File file = new File(getCacheDir(),
				parentPath + "/" + mapUrlToRelativeCachePath(url));

		// Return canonical File so that we can be sure that it can
		// actually be used. If not, throw a runtime exception.
		try {
			// Make sure all parent directories exist.
			file.getParentFile().mkdirs();
			return file.getCanonicalFile();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Maps a url to a relative path than can be used safely to store
	 * a file in the cache directory.
	 * <p>
	 * NOTE: This should probably be changed to remove the root url path
	 * so that the cache has a smaller folder tree depth.
	 *
	 * @param url A url to map.
	 * @return A relative file system path version of the url.
	 */
	public static String mapUrlToRelativeCachePath(URL url) {
		String authority = url.getAuthority();
		if (authority != null && (authority.equals("http") || authority.equals("https"))) {
			// Normal web url so just return path part.
			return url.getHost() + url.getPath();
		} else {
			// Must be "file:" which is device dependent, so let the platform
			// decide how to extract the path part.
			return Device.platform().mapUrlToRelativeFilePath(url);
		}
	}

	/**
	 * Removes all cache contents.
	 *
	 * @return The total number of files deleted.
	 */
	public static int clearCache() {
		return deleteSubFolders(getCacheDir());
	}

	/**
	 * Clears the specified cached filter directories.
	 */
	public static void clearFilteredImages(List<Filter> filters) {
		int deleted = filters.stream()
				.map(filter -> new File(getCacheDir(), filter.getName()))
				.mapToInt(CacheUtils::deleteSubFolders)
				.sum();

		if (Device.options().getDiagnosticsEnabled()) {
			System.out.println(deleted +
					" previously downloaded file(s) deleted.");
		}
	}

	/**
	 * Recursively delete files in a specified directory.
	 */
	public static int deleteSubFolders(File dir) {
		File[] files = dir.listFiles();
		if (files == null) {
			return 0;
		} else {
			return Stream.of(files)
					.mapToInt(file -> {
						if (file.isDirectory()) {
							int count = deleteSubFolders(file);
							file.delete();
							return count;
						} else {
							return file.delete() ? 1 : 0;
						}
					})
					.sum();
		}
	}

	/**
	 * Creates index.html files for the specified cache directory and
	 * of it's all sub-directories. Each index.html file will contain
	 * links to any discovered images and sub-folders.
	 *
	 * @param rootDirName Directory in which to perform indexing.
	 */
	public static void indexCache(String rootDirName) {
		try {
			indexDirectory(new File(getCacheDir(), rootDirName));
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
	private static void indexDirectory(File dir) {
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
		dirs.forEach(CacheUtils::indexDirectory);
	}
}

