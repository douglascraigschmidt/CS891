import java.util.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class illustrates how inheritance and dynamic binding works in
 * Java.  It uses a simplified hierarchy of *Set classes that are
 * implemented by the corresponding "real" Java classes.
 */
public class DynamicBindingTest {
    /**
     * Superclass for the hierarchy.
     */
    public static abstract class SimpleAbstractSet<E> {
        /**
         * Returns an iterator over the elements in this collection.
         * There are no guarantees concerning the order in which the
         * elements are returned (unless this collection is an
         * instance of some class that provides a guarantee).
         *
         * @return an <tt>Iterator</tt> over the elements in this collection
         *
         * This abstract method must be overridden by concrete subclasses.
         */
        public abstract Iterator<E> iterator();

        /**
         * Returns <tt>true</tt> if this collection contains the
         * specified element.  More formally, returns <tt>true</tt> if
         * and only if this collection contains at least one element
         * <tt>e</tt> such that
         * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
         *
         * @param o element whose presence in this collection is to be tested
         * @return <tt>true</tt> if this collection contains the specified
         *         element
         *
         * This abstract method must be overridden by concrete
         * subclasses.
         */
        public abstract boolean contains(Object o);

        /**
         * Adds the specified element to this set.
         *
         * @param e element to be added
         * @return {@code true} if this set changed as a result of the call
         *
         * This abstract method must be overridden by concrete
         * subclasses.
         */
        public abstract boolean add(E e);
    }
	
    /**
     * One subclass in the hierarchy.
     */
    public static class SimpleHashSet<E>
           extends SimpleAbstractSet<E> {
        /**
         * Concrete state.
         */
        private Set<E> mSet = new HashSet<>();

        /**
         * Override the superclass method.
         */
        @Override
        public Iterator<E> iterator() {
            System.out.println("SimpleHashSet.iterator()");
            return mSet.iterator();
        }

        /**
         * Override the superclass method.
         */
        @Override
        public boolean contains(Object o) {
            System.out.println("SimpleHashSet.contains()");
            return mSet.contains(o);
        }

        /**
         * Override the superclass method.
         */
        @Override
        public boolean add(E e) {
            System.out.println("SimpleHashSet.add()");
            return mSet.add(e);
        }
    }
	
    /**
     * One subclass in the hierarchy.
     */
    public static class SimpleTreeSet<E>
           extends SimpleAbstractSet<E> {
        /**
         * Concrete state.
         */
        private Set<E> mSet = new TreeSet<>();

        /**
         * Override the superclass method.
         */
        @Override
        public Iterator<E> iterator() {
            System.out.println("SimpleTreeSet.iterator()");
            return mSet.iterator();
        }

        /**
         * Override the superclass method.
         */
        @Override
        public boolean contains(Object o) {
            System.out.println("SimpleTreeSet.contains()");
            return mSet.contains(o);
        }

        /**
         * Override the superclass method.
         */
        @Override
        public boolean add(E e) {
            System.out.println("SimpleTreeSet.add()");
            return mSet.add(e);
        }
    }
	
    /**
     * One subclass in the hierarchy.
     */
    private static class SimpleConcurrentHashSet<E>
            extends SimpleAbstractSet<E> {
        /**
         * Concrete state.
         */
        private ConcurrentHashMap<E, Object> mMap =
            new ConcurrentHashMap<>();

        /**
         * A dummy value object needed by ConcurrentHashMap.
         */
        private static final Object mDummyValue = 
            new Object();

        /**
         * Override the superclass method.
         */
        @Override
        public Iterator<E> iterator() {
            System.out.println("SimpleConcurrentHashSet.iterator()");
            return mMap.keySet().iterator();
        }

        /**
         * Override the superclass method.
         */
        @Override
        public boolean contains(Object o) {
            System.out.println("SimpleConcurrentHashSet.contains()");
            return mMap.containsKey(o);
        }

        /**
         * Override the superclass method.
         */
        @Override
        public boolean add(E e) {
            System.out.println("SimpleConcurrentHashSet.add()");
            return mMap.put(e, mDummyValue) != null;
        }
    }
	
    /**
     * Factory method that creates the designated @a mapType.
     */
    private static SimpleAbstractSet<String> makeSet(String mapType) {
        switch (mapType) {
            case "HashSet":
                return new SimpleHashSet<>();
            case "TreeSet":
                return new SimpleTreeSet<>();
            case "ConcurrentHashSet":
                return new SimpleConcurrentHashSet<>();
            default:
                throw new IllegalArgumentException();
        }
    }
	
    /**
     * Main entry point into the test program.
     */
    public static void main(String[] args) {
        // Factory method makes the appropriate type of Set subclass.
        SimpleAbstractSet<String> set =
            makeSet(args.length == 0 ? "HashSet" : args[0]);

        // Add some elements to the set.
        set.add("I");
        set.add("am");
        set.add("Ironman");

        if (set.add("Ironman"))
            System.out.println("add() failed");

        if (!set.contains("I")
            || !set.contains("am")
            || !set.contains("Ironman"))
            System.out.println("contains() failed");

        // Print out the key/values pairs in the Set.
        for (Iterator<String> iter = set.iterator();
             iter.hasNext();
             )
            System.out.println("next item = "
                               + iter.next());
    }
}
