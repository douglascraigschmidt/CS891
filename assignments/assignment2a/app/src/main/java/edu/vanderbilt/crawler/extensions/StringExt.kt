package edu.vanderbilt.crawler.extensions

/**
 * Converts a class name or enumerated type name to
 * a title suitable for displaying in as UI text.
 */
fun String.classNameToTitle(): String {
    return toCharArray().mapIndexed { i, c ->
        when (c) {
            in 'A'..'Z' -> if (i == 0) "$c" else " $c"
            '_' -> " "
            else -> "$c"
        }
    }.joinToString(separator = "")
}
