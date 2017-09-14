package edu.vanderbilt.platform;

import java.util.concurrent.CancellationException;

import edu.vanderbilt.utils.Crawler;
import edu.vanderbilt.utils.Options;

/**
 * A container singleton class that provides access to the platform
 * dependent objects Platform, Options, and Crawler. This singleton
 * must be build using the provider Builder and should only be built
 * once.
 */
public class Device {
	private static Device instance = null;
	private final Platform platform;
	private final Options options;
	private final Crawler parser;
	private boolean mStopCrawl = false;

	private Device(Builder builder) {
		platform = builder.platform;
		options = builder.options;
		parser = builder.parser;
		instance = this;
	}

	public static Device instance() {
		return instance;
	}

	public static Builder newBuilder() {
		if (instance != null) {
			throw new IllegalStateException("Device has already been built.");
		}
		return new Builder();
	}

	public static Builder newBuilder(Device copy) {
		Builder builder = new Builder();
		builder.platform = copy.platform;
		builder.options = copy.options;
		builder.parser = copy.parser;
		return builder;
	}

	public static Platform platform() {
		return instance().platform;
	}

	public static Options options() {
		return instance().options;
	}

	public static Crawler parser() {
		return instance().parser;
	}

	public static void stopCrawl(boolean stop) {
		instance().mStopCrawl = stop;
	}

	/**
	 * @return Cancellation flag indicating if the current
	 * crawl should be halted.
	 */
	public static boolean isCancelled() {
		return instance().mStopCrawl;
	}

	/**
	 * Throws a CancellationException if the mStopCrawl flag has been set.
	 * crawl should be halted.
	 */
	public static void throwExceptionIfCancelled() {
		if (isCancelled()) {
			throw new CancellationException("The crawl has been cancelled.");
		}
	}

	/**
	 * This method clears singleton and should only be called from
	 * instrumented and unit test scripts.
	 */
	protected void destroy() {
		instance = null;
	}

	/**
	 * {@code Device} builder static inner class.
	 */
	public static final class Builder {
		private Platform platform;
		private Options options;
		private Crawler parser;

		private Builder() {
		}

		/**
		 * Sets the {@code platform} and returns a reference to this Builder so that the methods
		 * can
		 * be chained together.
		 *
		 * @param val the {@code platform} to set
		 * @return a reference to this Builder
		 */
		public Builder platform(Platform val) {
			platform = val;
			return this;
		}

		/**
		 * Sets the {@code options} and returns a reference to this Builder so that the methods can
		 * be chained together.
		 *
		 * @param val the {@code options} to set
		 * @return a reference to this Builder
		 */
		public Builder options(Options val) {
			options = val;
			return this;
		}

		/**
		 * Sets the {@code crawler} and returns a reference to this Builder so that the methods can
		 * be chained together.
		 *
		 * @param val the {@code crawler} to set
		 * @return a reference to this Builder
		 */
		public Builder crawler(Crawler val) {
			parser = val;
			return this;
		}

		/**
		 * Returns a {@code Device} built from the parameters previously set.
		 *
		 * @return a {@code Device} built with parameters of this {@code Device.Builder}
		 */
		public Device build() {
			return new Device(this);
		}
	}

	/**
	 * Only to be called from unit tests.
	 */
	public static void setPrivateInstanceFieldToNullForUnitTestingOnly() {
		instance = null;
	}
}
