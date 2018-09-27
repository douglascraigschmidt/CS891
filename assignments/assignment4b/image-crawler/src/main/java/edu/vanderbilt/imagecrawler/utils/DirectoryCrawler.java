package edu.vanderbilt.imagecrawler.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Stream;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * Not currently used.
 */
public class DirectoryCrawler implements Crawler {
	@Override
	public Page getPage(String uri) {
		try {
			File file = new File(new URI(uri).getPath());
			if (!file.isDirectory()) {
				throw new RuntimeException("Uri is not a directory: " + uri);
			}
			return new DirectoryPage(file, uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encapsulates/hides the JSoup Document object into a generic container.
	 */
	protected class DirectoryPage implements Page {
		private String uri;
		private File directory;

		protected DirectoryPage(File directory, String uri) {
			this.directory = directory;
			this.uri = uri;
		}

		@Override
		public Array<WebPageElement> getPageElements(Type... types) {
			return getPageElementsAsStrings(types)
					.stream()
					.map(WebPageElement::newPageElement)
					.collect(ArrayCollector.toArray());
		}

		@Override
		public Array<URL> getPageElementsAsUrls(Type... types) {
			return getPageElementsAsStrings(types).stream()
					.map(ExceptionUtils.rethrowFunction(URL::new))
					.collect(ArrayCollector.toArray());
		}

		@Override
		public Array<String> getPageElementsAsStrings(Type... types) {
			File[] files = directory.listFiles();
			if (files == null || files.length == 0) {
				return new Array<>();
			}

			return Arrays.stream(types)
					.flatMap(type -> {
						if (type == PAGE) {
							return Stream.of(files)
									.filter(File::isDirectory)
									.map(File::toURI)
									.map(URI::toString);
						} else {
							return Stream.of(files)
									.filter(this::isImageFile)
									.map(File::toURI)
									.map(URI::toString);
						}
					}).collect(ArrayCollector.toArray());
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
	}
}
