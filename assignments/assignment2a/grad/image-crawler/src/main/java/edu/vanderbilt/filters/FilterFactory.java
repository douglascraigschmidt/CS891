package edu.vanderbilt.filters;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory class used to create new instances of supported filters
 * or to create list containing new instances of all supported filters.
 */
public final class FilterFactory {
    /**
     * Supported filters (with names).
     */
    public enum Type {
        GRAY_SCALE_FILTER(GrayScaleFilter.class),
        NULL_FILTER(NullFilter.class);

        private final Class<? extends Filter> clazz;

        Type(Class<? extends Filter> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }

    private FilterFactory() {
    }

    /**
     * Creates the specified {@code type} filter. Any images downloaded using
     * this filter will be saved in a folder that has the same name as the
     * filter class.
     *
     * @param type Type of filter.
     * @return A filter instance of the specified type.
     */
    public static Filter newFilter(Type type) {
        return newFilter(type, null);
    }

    /**
     * Creates an new instance of the specified {@code type} of filter. Any
     * images downloaded using this filter will be saved in a folder named
     * {@code name}.
     *
     * @param type Type of filter.
     * @param name The name of a directory where all downloaded images will
     *             be saved, or null to let the filter decide..
     * @return A filter instance of the specified type.
     */
    public static Filter newFilter(Type type, String name) {
        try {
            // Construct a new filter instance.
            Filter filter = type.clazz.newInstance();

            // Set the filter name if a name was specified.
            if (name != null && !name.isEmpty()) {
                filter.setName(name);
            }

            return filter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates list containing new instances of the requested filter types.
     *
     * @return List of new instances of the specified filter types.
     */
    public static List<Filter> newFilters(List<Type> types) {
        // Create and initialize filter instances.
        return types.stream()
            .map(type -> newFilter(type, null))
            .collect(Collectors.toList());
    }
}
