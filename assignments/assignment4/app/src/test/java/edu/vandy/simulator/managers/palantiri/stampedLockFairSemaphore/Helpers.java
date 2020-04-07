package edu.vandy.simulator.managers.palantiri.stampedLockFairSemaphore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import admin.ReflectionHelper;

public class Helpers {
    public static boolean isLockSolution(Object parentClass) {
        return ReflectionHelper.findFirstMatchingField(parentClass, Lock.class) != null;
    }

    public static boolean isReentrantLockSolution(Object parentClass) {
        return ReflectionHelper.findFirstMatchingField(parentClass, ReentrantLock.class) != null;
    }
}
