package edu.vandy.recommender.utils;

import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.reverseOrder;

/**
 * This Java utility class efficiently finds the top {@code k} items
 * in a {@link Flux}, {@link Stream}, or {@link List} using a Java
 * {@link PriorityQueue}, which is implemented internally via a Heap
 * data structure (<a
 * href="http://en.wikipedia.org/wiki/Heap_(data_structure)">here</a>).
 */
public class GetTopK {
    /**
     * A Java utility class should have a private constructor.
     */
    private GetTopK() {}

   /**
     * This method returns a {@link Collection} that emits the top
     * {@code k} items in the {@code stream} parameter.
     *
     * @param collection The {@link Collection} given as input
     * @param k The max number of items to return
     * @return A {@link Collection} that contains the top {@code k}
     *         items in the {@code stream} parameter
     */
    public static <T> Collection<T> getTopK(Collection<T> collection,
                                            int k) {
        if (k < 1)
            // Return an empty Collection if k < 1.
            return Collections.emptyList();
        else {
            // Create a new heap to efficiently keep track of the top
            // k items.
            Queue<T> heap = new PriorityQueue<>();

            // Iterate through all the items in the collection.
            for (var item : collection) {
                // Insert the item into the heap.
                heap.offer(item);

                // Remove the item of lowest priority from the
                // heap if its size exceeds k.
                if (heap.size() > k)
                    heap.poll();
            }

            // After all heap update processing completes convert the
            // heap into a List whose values are ordered from
            // highest to lowest.
            return convertHeapToCollection(heap);
        }
    }

    /**
     * Returns a {@link Collection} that contains the items in the {@code
     * heap} sorted from highest to lowest.
     *
     * @param heap The heap containing the top k items
     * @return A {@link Collection} that contains the items in the {@code
     *         heap} sorted from highest to lowest
     */
    private static <T> Collection<T> convertHeapToCollection(Queue<T> heap) {
        var sortedList = new ArrayList<>(heap);
        sortedList.sort(Collections.reverseOrder());
        return sortedList;
    }

    /**
     * This method returns a {@link Stream} that emits the top {@code
     * k} items in the {@code stream} parameter.
     *
     * @param stream The {@link Stream} given as input
     * @param k The max number of items to return
     * @return A {@link Stream} that emits the top {@code k}
     *         items in the {@code stream} parameter
     */
    public static <T> Stream<T> getTopK(Stream<T> stream,
                                        int k) {
        if (k < 1)
            // Return an empty Stream if k < 1.
            return Stream.empty();
        else {
            // Create a new heap to efficiently keep track of the top
            // k items.
            Queue<T> heap = new PriorityQueue<>();

            stream
                // Update the heap.
                .forEach(item -> {
                    // Insert the item into the heap.
                    heap.offer(item);

                    // Remove the item of lowest priority from the
                    // heap if its size exceeds k.
                    if (heap.size() > k)
                         heap.poll();
                });

            // After all heap update processing completes convert the
            // heap into a Stream whose values are ordered from
            // highest to lowest.
            return convertHeapToStream(heap);
        }
    }

    /**
     * Returns a {@link Stream} that contains the items in the {@code
     * heap} sorted from highest to lowest.
     *
     * @param heap The heap containing the top k items
     * @return A {@link Stream} that emits the items in the {@code
     *         heap} sorted from highest to lowest
     */
    private static <T> Stream<T> convertHeapToStream(Queue<T> heap) {
        return heap
            // Convert the Queue into a Stream.
            .stream()

            // Sort the Stream contents from highest to lowest.
            .sorted(reverseOrder());
    }

    /**
     * This adapter method enables integration with Project Reactor's
     * {@code transform()} operator.
     *
     * @param k The max number of items to return
     * @return A {@link Flux} that emits the top {@code k}
     *         items in the implicit {@link Flux parameter}
     */
    public static <T> Function<Flux<T>, Flux<T>> getTopK(int k) {
        return flux -> GetTopK.getTopK(flux, k);
    }

    /**
     * This method returns a {@link Flux} that emits the top {@code k}
     * items in the {@code flux} parameter.
     *
     * @param flux The {@link Flux} given as input
     * @param k The max number of items to return
     * @return A {@link Flux} that emits the top {@code k}
     *         items in the {@code flux} parameter
     */
    public static <T> Flux<T> getTopK(Flux<T> flux,
                                      int k) {
        if (k < 1)
            // Return an empty Flux if k < 1.
            return Flux.empty();
        else {
            // Create a new heap to efficiently keep track of the top
            // k items.
            Queue<T> heap = new PriorityQueue<>();

            return flux
                // Return an empty Flux if there's no input.
                .switchIfEmpty(Flux.empty())

                // This action operator updates the heap.
                .doOnNext(item -> {
                    // Insert the item into the heap.
                    heap.offer(item);

                    // Remove the item of lowest priority from the heap if its
                    // size exceeds k.
                    if (heap.size() > k)
                        heap.poll();
                })

                // After all the updateHeap() processing completes
                // convert the heap into a Flux whose values are
                // ordered from highest to lowest.
                .thenMany(Flux.defer(() -> convertHeapToFlux(heap)));
        }
    }

    /**
     * Returns a {@link Flux} that contains the items in the queue
     * sorted from highest to lowest.
     *
     * @param heap The heap containing the top k items
     * @return A {@link Flux} that emits the items in the queue
     *         sorted from highest to lowest
     */
    private static <T> Flux<T> convertHeapToFlux(Queue<T> heap) {
        return Flux
            // Convert the Queue into a Flux.
            .fromIterable(heap)

            // Sort the Flux contents from highest to lowest.
            .sort(reverseOrder());
    }
}
