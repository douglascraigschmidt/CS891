package admin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

public class ReflectionHelper {
    public static <T> T findFirstMatchingFieldValue(Object parentClass, Class findClass) {
        Field field = findFirstMatchingField(parentClass, findClass);
        if (field == null) {
            return null;
        } else {
            try {
                field.setAccessible(true);
                //noinspection unchecked
                return (T) field.get(parentClass);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    public static <T> T findFirstlMatchingFieldValue(Object parentClass, Class... classes) {
        Optional<Object> first = Stream.of(classes)
                .map(clazz -> findFirstMatchingFieldValue(parentClass, clazz))
                .filter(Objects::nonNull)
                .findFirst();

        Object value = first.orElse(null);
        //noinspection unchecked
        return (T)value;
    }

    public static Field findFirstMatchingField(Object parentClass, Class findClass) {
        return Arrays.stream(parentClass.getClass().getDeclaredFields())
                .filter(field -> field.getType() == findClass)
                .findFirst().orElse(null);
    }

    public static Field findFirstMatchingField(Object parentClass, Class... classes) {
        return Arrays.stream(parentClass.getClass().getDeclaredFields())
                .filter(field -> {
                    for (Class clazz : classes) {
                        if (field.getType() == clazz) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst().orElse(null);
    }

    public static void injectValueIntoNamedField(
            Object parentClass,
            Object fieldValue,
            String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = parentClass.getClass().getField(fieldName);
        boolean wasAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(parentClass, fieldValue);
        field.setAccessible(wasAccessible);
    }

    public static void injectValueIntoFirstMatchingField(
            Object parentClass,
            Object fieldValue,
            Class fieldClass) throws IllegalAccessException {
        Field field = findFirstMatchingField(parentClass, fieldClass);
        if (field == null) {
            throw new IllegalAccessException(
                    "Unable to inject field value into class field of type "
                            + fieldClass.getName());
        }
        boolean wasAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(parentClass, fieldValue);
        field.setAccessible(wasAccessible);
    }

    public static void injectValueIntoFirstMatchingField(
            Object parentClass,
            Object fieldValue,
            Class... classes
            ) throws IllegalAccessException {
        Field field = findFirstMatchingField(parentClass, classes);
        if (field == null) {
            throw new IllegalAccessException(
                    "Unable to inject field value into class field");
        }
        boolean wasAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(parentClass, fieldValue);
        field.setAccessible(wasAccessible);
    }

    public static void injectOuterClass(Object parentObject, Object innerClass)
            throws Exception {
        Field field = innerClass.getClass().getDeclaredField("this$0");
        boolean wasAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(innerClass, parentObject);
        field.setAccessible(wasAccessible);
    }

    public static void assertAnonymousFieldNotNull(Object parentObject, Class findClass) {
        try {
            Object fieldValue =
                    ReflectionHelper.findFirstMatchingFieldValue(parentObject, findClass);
            assertNotNull("Unable to access "
                            + findClass.getSimpleName()
                            + " field in "
                            + parentObject.getClass().getSimpleName() + ".",
                    fieldValue);
        } catch (Exception e) {
            fail("Unable to access "
                    + findClass.getSimpleName()
                    + " field in "
                    + parentObject.getClass().getSimpleName() + ": " + e);
        }
    }
}
