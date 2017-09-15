package edu.vanderbilt.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

/**
 * Created by monte on 2017-09-11.
 */

public class DirectoryCrawler implements Crawler {
	@Override
	public Container getContainer(String uri) {
		try {
			File file = new File(new URI(uri).getPath());
			if (!file.isDirectory()) {
				throw new RuntimeException("Uri is not a directory: " + uri);
			}
			return new DirectoryContainer(file, uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encapsulates/hides the JSoup Document object into a generic container.
	 */
	protected class DirectoryContainer implements Container {
		private String uri;
		private File directory;

		protected DirectoryContainer(File directory, String uri) {
			this.directory = directory;
			this.uri = uri;
		}

		@Override
		public Array<String> getObjectsAsStrings(Type type) {
			File[] files = directory.listFiles();
			if (files == null || files.length == 0) {
				return new Array<>();
			}

			switch (type) {
				case CONTAINER: {
					return Stream.of(files)
							.filter(File::isDirectory)
							.map(File::toURI)
							.map(URI::toString)
							.collect(ArrayCollector.toArray());
				}
				case IMAGE:
					return Stream.of(files)
							.filter(this::isImageFile)
							.map(File::toURI)
							.map(URI::toString)
							.collect(ArrayCollector.toArray());
				default:
					throw new IllegalArgumentException("Invalid DirectoryCrawler object type.");
			}
		}

		/**
		 * Helper method that uses the {@code file} extension to
		 * determine if it's an image.
		 */
		private boolean isImageFile(File file) {
			return file.isFile() &&
					(file.getName().endsWith(".png")
							|| file.getName().endsWith(".jpg")
							|| file.getName().endsWith(".jpeg"));
		}

		@Override
		public Array<URL> getObjectsAsUrls(Type type) {
			return getObjectsAsStrings(type).stream()
					.map(ExceptionUtils.rethrowFunction(URL::new))
					.collect(ArrayCollector.toArray());
		}
	}
}