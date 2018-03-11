package edu.vandy.simulator.managers.palantiri.reentrantLockHashMapSimpleSemaphore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.vandy.simulator.ReflectionHelper;

public class Helpers {
    public static boolean isLockSolution(Object parentClass) {
        return ReflectionHelper.findFirstFieldOfType(parentClass, Lock.class) != null;
    }

    public static boolean isReentrantLockSolution(Object parentClass) {
        return ReflectionHelper.findFirstFieldOfType(parentClass, ReentrantLock.class) != null;
    }
}
