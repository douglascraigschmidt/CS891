package edu.vanderbilt.crawlers.framework;

import java.util.List;
import java.util.stream.Collectors;

import edu.vanderbilt.crawlers.SequentialLoopsCrawler;
import edu.vanderbilt.crawlers.SequentialStreamsCrawler;
import edu.vanderbilt.filters.FilterFactory;

/**
 * Factory class used to create new instances of supported crawlers
 * or to create list containing new instances of all supported crawlers.
 */
public class CrawlerFactory {
    /**
     * Supported filters (with names).
     */
    public enum Type {
        SEQUENTIAL_LOOPS(SequentialLoopsCrawler.class),
        SEQUENTIAL_STREAMS(SequentialStreamsCrawler.class);

        private final Class<? extends ImageCrawlerBase> clazz;

        Type(Class<? extends ImageCrawlerBase> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }

    private CrawlerFactory () {
    }

    /**
     * Creates the specified {@code type} filter. Any images downloaded using
     * this filter will be saved in a folder that has the same name as the
     * filter class.
     *
     * @param crawlerType Type of filter.
     * @param filterTypes List of filter types for crawler to use.
     * @param rootUri The root uri where the crawl should begin.
     * @return A filter instance of the specified type.
     */
    public static ImageCrawlerBase newCrawler(Type crawlerType,
                                              List<FilterFactory.Type> filterTypes,
                                              String rootUri) {
        try {
            ImageCrawlerBase crawler = crawlerType.clazz.newInstance();
            crawler.initialize(FilterFactory.newFilters(filterTypes),
                               rootUri);
            return crawler;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a list of new crawler instances matching the past crawler types.
     * Note that since the filter FilterDecoratorWithImage is not thread-safe,
     * each crawler has to get its own filter list.
     *
     * @param crawlerTypes List of crawler types to create.
     * @param filterTypes List of filters to use with each crawler.
     * @param rootUri The root uri where the crawl should begin.
     * @return A list of new crawler instances.
     */
    public static List<ImageCrawlerBase> newCrawlers(List<Type> crawlerTypes,
                                                     List<FilterFactory.Type> filterTypes,
                                                     String rootUri) {
        return crawlerTypes.stream()
            .map(type -> newCrawler(type, filterTypes, rootUri))
            .collect(Collectors.toList());
    }
}
