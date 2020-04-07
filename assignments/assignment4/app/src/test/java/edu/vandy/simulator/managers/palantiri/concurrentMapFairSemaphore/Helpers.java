package edu.vandy.simulator.managers.palantiri.concurrentMapFairSemaphore;

import java.util.LinkedList;

import admin.ReflectionHelper;

public class Helpers {
    /**
     * Fields to check to see if the class has been implemented.
     */
    private final static Class[] MO_FIELDS = new Class[]{LinkedList.class};
    private final static Class[] CO_FIELDS = new Class[]{LinkedList.class};

    /**
     * Returns true if the alternative class has been implemented.
     */
    public static boolean ignoreTest(Class clazz) {
        return (clazz == FairSemaphoreCO.class &&
                        !isSolutionImplemented(FairSemaphoreCO.class, CO_FIELDS)) ||
                        isSolutionImplemented(FairSemaphoreMO.class, MO_FIELDS) &&
                (clazz == FairSemaphoreMO.class &&
                        !isSolutionImplemented(FairSemaphoreMO.class, MO_FIELDS) &&
                        isSolutionImplemented(FairSemaphoreCO.class, CO_FIELDS));
    }

    public static boolean isSolutionImplemented(Class clazz, Class fields[]) {
        try {
            Object implementation = clazz.newInstance();
            return ReflectionHelper.findFirstMatchingField(implementation, fields) != null;
        } catch (Throwable t) {
            return false;
        }
    }
}
