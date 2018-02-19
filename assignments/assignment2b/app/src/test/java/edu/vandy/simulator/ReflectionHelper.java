package edu.vandy.simulator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.vandy.simulator.managers.beings.executorService.ExecutorServiceMgr;

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
                .filter(field -> field.getType() == findClass)
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
        field.setAccessible(true);
        field.set(parentClass, fieldValue);
    }
}
