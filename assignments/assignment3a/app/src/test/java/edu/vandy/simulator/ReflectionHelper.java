package edu.vandy.simulator;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ReflectionHelper {
    public static <T> T findFirstFieldValueOfType(Object parentClass, Class findClass) {
        Field field = findFirstFieldOfType(parentClass, findClass);
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

    public static Field findFirstFieldOfType(Object parentClass, Class findClass) {
        return Arrays.stream(parentClass.getClass().getDeclaredFields())
                .filter(field -> {
                    //System.out.println(field);
                    return field.getType() == findClass;
                })
                .findFirst().orElse(null);
    }

    public static void injectFieldValueIntoFirstFieldOfType(
            Object parentClass,
            Class fieldClass,
            Object fieldValue) throws IllegalAccessException {
        Field field = findFirstFieldOfType(parentClass, fieldClass);
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
                    ReflectionHelper.findFirstFieldValueOfType(parentObject, findClass);
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
