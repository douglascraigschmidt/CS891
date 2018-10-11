package edu.vanderbilt.imagecrawler.utils;

/**
 * Assignment framework class used to support unit tests based on
 * graduate or undergraduate implementations.
 */
public class Assignment {
    public static int GRADUATE = 1<<0;
    public static int UNDERGRADUATE = 1<<1;

    /**
     * TODO: (Graduates) set the static types field below to the value GRADUATE.
     * TODO: (undergraduates) set the static types field below to the value UNDERGRADUATE.
     */
    public static int types = GRADUATE | UNDERGRADUATE;

    public static boolean isGraduateTodo() {
        return (types & GRADUATE) == GRADUATE;
    }

    public static boolean isUndergraduateTodo() {
        return (types & UNDERGRADUATE) == UNDERGRADUATE;
    }

    /**
     * If the specified [type] is a member of the [types] Set property,
     * then testType will overwrite [types] to contain ONLY the specified
     * [type] and then return true. This effectively will enable any source
     * code blocks surrounded by if ([type]) { ... } and disable other code
     * blocks where the [type] does not match the passed parameter value.
     * If the [type] is not a member of the [types] Set, then [types] is not
     * modified and false is returned.
     */
    public static boolean testType(int type) {
        if ((types & type) == type) {
            types = type;
            return true;
        } else {
            return false;
        }
    }
}
