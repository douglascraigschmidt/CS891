package edu.vanderbilt.webcrawler;

import org.junit.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.vanderbilt.crawlers.SequentialLoopsCrawler;
import edu.vanderbilt.crawlers.SequentialStreamsCrawler;
import edu.vanderbilt.crawlers.framework.CrawlerFactory;
import edu.vanderbilt.crawlers.framework.ImageCrawlerBase;
import edu.vanderbilt.filters.Filter;
import edu.vanderbilt.filters.FilterFactory;
import edu.vanderbilt.platform.Device;
import edu.vanderbilt.utils.CacheUtils;
import edu.vanderbilt.utils.LocalPageCrawler;
import edu.vanderbilt.utils.Options;
import edu.vanderbilt.utils.WebPageCrawler;
import edu.vanderbilt.webcrawler.platform.AndroidPlatform;

/**
 * Tests local and remove web crawling.
 */
public class ImageCrawlerTest {
	/**
	 * Keep track of the timing results of the ImageCrawler
	 * implementation strategies so they can be sorted and displayed
	 * when the program is finished.
	 */
	private Map<String, List<Long>> mResultsMap =
			new HashMap<>();

	@Test
	public void testLocalImageCrawlerNullFilterOnly() {
		System.out.println("Starting testLocalImageCrawler");

		// Device protects itself from multiple builds and this
		// method gets around that restriction.
		Device.setPrivateInstanceFieldToNullForUnitTestingOnly();

		// Create a new device with a local page crawler.
		Device.newBuilder()
				.platform(new AndroidPlatform())
				.options(Options.newBuilder()
						.local(true)
						.diagnosticsEnabled(true)
						.build())
				.crawler(new LocalPageCrawler())
				.build();

		// Delete any the filtered images from the previous run.
		CacheUtils.clearCache();

		String rootUri = Device.options().getRootUri();

		Filter filter = FilterFactory.newFilter(FilterFactory.Type.NULL_FILTER);

		List<Filter> filters = Stream.of(filter).collect(Collectors.toList());

		SequentialStreamsCrawler crawler = new SequentialStreamsCrawler(filters, rootUri);

		crawler.run();

		System.out.println("Ending ImageCrawlerTestOld");
	}

	/**
	 * This method is the static main() entry point to run the console
	 * version of the ImageCrawler app.
	 */
	@Test
	public void testLocalImageCrawler() {
		System.out.println("Starting testLocalImageCrawler");

		// Device protects itself from multiple builds and this
		// method gets around that restriction.
		Device.setPrivateInstanceFieldToNullForUnitTestingOnly();

		// Create a new device with a local page crawler.
		Device.newBuilder()
				.platform(new AndroidPlatform())
				.options(Options.newBuilder()
						.local(true)
						.diagnosticsEnabled(true)
						.build())
				.crawler(new LocalPageCrawler())
				.build();

		// Run the tests.
		runTimingTests();

		System.out.println("Ending ImageCrawlerTestOld");
	}

	/**
	 * This method is the static main() entry point to run the console
	 * version of the ImageCrawler app.
	 */
	@Test
	public void testWebImageCrawler() {
		System.out.println("Starting testLocalImageCrawler");

		// Device protects itself from multiple builds and this
		// method gets around that restriction.
		Device.setPrivateInstanceFieldToNullForUnitTestingOnly();

		// Create a new device with a local page crawler.
		Device.newBuilder()
				.platform(new AndroidPlatform())
				.options(Options.newBuilder()
						.local(false)
						.diagnosticsEnabled(true)
						.build())
				.crawler(new WebPageCrawler())
				.build();

		// Run the tests.
		runTimingTests();

		System.out.println("Ending ImageCrawlerTestOld");
	}

	/**
	 * Iterate through all the implementation strategies to test how
	 * they perform.
	 */
	private void runTimingTests() {
		// Options.instance().setDiagnosticsEnabled(true);

		// Iterate through the implementation strategies and test them.
		// Make an ImageCrawlerAsync object via the factory method.
		makeImageCrawler(Device.options().getRootUri()).forEach(crawler -> {
			String test = crawler.getClass().getSimpleName();
			System.out.println("Starting " + test);

			// Delete any the filtered images from the previous run.
			CacheUtils.clearCache();

			// Start running the test.
			crawler.run();

			// Store the execution times.
			mResultsMap.put(test, crawler.executionTimes());

			// Run the garbage collector to avoid perturbing the test.
			System.gc();

			System.out.println("Ending " + test);
		});

		// Print out all the timing results.
		printTimingResults(mResultsMap);
	}

	/**
	 * Factory method that creates the designated type of ImageCrawlerAsync
	 * subclass implementation.
	 */
	private List<ImageCrawlerBase> makeImageCrawler(String rootUri) {
		return
		CrawlerFactory.newCrawlers(
				Arrays.asList(CrawlerFactory.Type.values()),
				Arrays.asList(FilterFactory.Type.values()),
				rootUri);
	}

	/**
	 * Print out all the timing results for all the test runs in order
	 * from fastest to slowest.
	 */
	private void printTimingResults(Map<String, List<Long>> resultsMap) {
		// Determine how many runs of the tests took place.
		int numberOfRuns =
				resultsMap.entrySet().iterator().next().getValue().size();

		// Iterate through the results of each of the test runs.
		for (int i = 0; i < numberOfRuns; i++) {
			final int runNumber = i;
			System.out.println("\nPrinting "
					+ resultsMap.entrySet().size()
					+ " results for input file "
					+ (runNumber + 1)
					+ " from fastest to slowest");

			// Print out the contents of the resultsMap in sorted
			// order.
			resultsMap
					// Get the entrySet for the resultsMap.
					.entrySet()

					// Convert the entrySet into a stream.
					.stream()

					// Create a SimpleImmutableEntry containing the timing
					// results (value) followed by the test name (key).
					.map(entry
							-> new AbstractMap.SimpleImmutableEntry<>
							(entry.getValue().get(runNumber),
									entry.getKey()))

					// Sort the stream by the timing results (key).
					.sorted(Comparator
							.comparing(AbstractMap
									.SimpleImmutableEntry::getKey))

					// Print all the entries in the sorted stream.
					.forEach(entry
							-> System.out.println(""
							+ entry.getValue()
							+ " executed in "
							+ entry.getKey()
							+ " msecs"));
		}
	}
}
