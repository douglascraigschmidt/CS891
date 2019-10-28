package admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.LongStream;

import edu.vanderbilt.imagecrawler.utils.Array;

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class ArrayHelper {
    /**
     * Constructs a sample input data list that contains a mix of object
     * types and duplication styles.
     */
    public static List<Object> constructMixedInput() {
        // Create a list with 5 objects each of which has a single duplicate.
        List<Object> objects = constructObjects(5, 2);

        // Add a couple of null values.
        objects.addAll(constructDuplicates(null, 2));

        // Add Special objects that are the same but are not equal.
        objects.addAll(constructInputWithObjectsSameButNotEqual());

        // Add Special objects that are equal but are not the same.
        objects.addAll(constructInputWithObjectsEqualButNotSame());

        return objects;
    }

    public static List<Object> constructInputWithObjectsSameButNotEqual() {
        // Add two occurrences of a single ObjectIsSameButNotEqual instance
        // which will return true for == comparison, but false for equals().
        List<Object> objects = constructDuplicates(new ObjectIsSameButNotEqual(), 2);
        int index = 0;

        // Make sure the ObjectIsSameButNotEqual class behaves in the expected manner.
        assertSame(objects.get(index), objects.get(index + 1));
        assertNotEquals(objects.get(index), objects.get(index + 1));
        assertNotEquals(objects.get(index + 1), objects.get(index));

        return objects;
    }

    public static List<Object> constructInputWithObjectsEqualButNotSame() {
        // Add a single long value that is contained in two boxed Long instances so
        // that when compared by == will return false, and when compared by equals()
        // will return true.
        List<Object> objects = new ArrayList<>(2);
        objects.add(new ObjectIsEqualButIsNotSame(1));
        objects.add(new ObjectIsEqualButIsNotSame(1));
        int index = 0;

        // Ensure expected behaviour of ObjectIsEqualButNotSame instances.
        assertNotSame(objects.get(index), objects.get(index + 1));
        assertEquals(objects.get(index), objects.get(index + 1));
        assertEquals(objects.get(index + 1), objects.get(index));

        return objects;
    }

    @SuppressWarnings("SameParameterValue")
    public static List<Object> getInputNoDups(int size) {
        List<Object> list = LongStream.rangeClosed(1, size).boxed().collect(toList());
        Collections.shuffle(list);
        return list;
    }

    @SuppressWarnings("SameParameterValue")
    public static List<Object> constructDuplicates(Object object, int duplicates) {
        List<Object> objects = new ArrayList<>();
        for (int i = 0; i < duplicates; i++) {
            objects.add(object);
        }
        assertEquals(duplicates, objects.size());
        return objects;
    }

    @SuppressWarnings("SameParameterValue")
    public static List<Object> constructObjects(int size, int duplicates) {
        List<Object> objects = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            objects.addAll(constructDuplicates(new Object(), duplicates));
        }
        assertEquals(size * duplicates, objects.size());
        return objects;
    }

    public static List<Object> constructShuffledMixedInput() {
        List<Object> mixedInput = constructMixedInput();
        Collections.shuffle(mixedInput);
        return mixedInput;
    }

    /**
     * Helper that uses reflection to access the contents
     * of the private Object[] field "mElementData".
     */
    public static Object[] getElements(Array array) {
        return ReflectionHelper.findFirstMatchingFieldValue(
                array,
                Object[].class,
                "mElementData");
    }

    /**
     * Helper that uses reflection to set the contents
     * of the private Object[] field "mElementData".
     */
    public static void setElements(Array array, Object[] elements) throws IllegalAccessException {
        ReflectionHelper.injectValueIntoMatchingField(
                array, elements, Object[].class, "mElementData");
    }

    /**
     * Helper that uses reflection to access the contents
     * of the private "mSize" int field.
     */
    public static int getSize(Array array) {
        //noinspection ConstantConditions
        return ReflectionHelper.findFirstMatchingFieldValue(
                array,
                int.class,
                "mSize");
    }

    /**
     * Helper that uses reflection to set the contents
     * of the private Object[] field "mElementData".
     */
    public static void setSize(Array array, int size) throws IllegalAccessException {
        ReflectionHelper.injectValueIntoMatchingField(
                array, size, int.class, "mSize");
    }

    /**
     * Helper that compares contents of two arrays for sameness (== operator).
     */
    public static void assertSameContents(Object[] expected, Object[] objects) {
        for (int i = 0; i < expected.length; i++) {
            assertSame(expected[i], objects[i]);
        }
    }

    /**
     * Helper that compares contents of two arrays for sameness (== operator).
     */
    public static void assertSameContentsAndLength(Object[] expected, Object[] objects) {
        assertEquals(expected.length, objects.length);
        assertSameContents(expected, objects);
    }

    /**
     * Helper that compares 2 iterators for same content.
     */
    public static void assertSameIteratorContents(Iterator expected, Iterator iterator) {
        while (expected.hasNext() && iterator.hasNext()) {
            assertSame(expected.next(), iterator.next());
        }

        assertEquals(expected.hasNext(), iterator.hasNext());

        try {
            iterator.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        } catch (Throwable t) {
            fail("Expected NoSuchElementException but instead got " + t.getClass().getName());
        }
    }

    /**
     * Helper that compares contents of two arrays for sameness (== operator).
     */
    public static void assertArrayEquals(Array<Object> expected, Array<Object> array) {
        assertEquals(expected.size(), array.size());
        for (int i = 0; i < expected.size(); i++) {
            assertSame(expected.get(i), array.get(i));
        }
    }


    /**
     * Special test object that has no equal object instances.
     * Note the equals() (equality) will return false even when
     * o1 == o2 is true (sameness); this behaviour is intentional.
     */
    public static class ObjectIsSameButNotEqual {
        @Override
        public boolean equals(Object o) {
            return false;
        }
    }

    /**
     * Special class similar to boxed long where 2 boxed instances
     * of the same value will return true for equals() but will return
     * false for == operator. Long cannot be used since the compiler
     * will return true when the == operator compares 2 different instances
     * of the same long value (e.g. new Long(2) == new Long(3)).
     */
    public static class ObjectIsEqualButIsNotSame {
        long mValue;

        public ObjectIsEqualButIsNotSame(long mValue) {
            this.mValue = mValue;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ObjectIsEqualButIsNotSame
                    && ((ObjectIsEqualButIsNotSame) o).mValue == mValue;
        }
    }
}
