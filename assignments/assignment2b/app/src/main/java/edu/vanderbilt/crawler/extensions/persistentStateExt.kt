package edu.vanderbilt.crawler.extensions

/**
 * Maintains a map object to support backing fields for
 * property extensions.
 */

object ExtensionBackingField {
    // Has to be public to support inline/reified get function.
    val field: MutableMap<Any, Any> = mutableMapOf()

    operator inline fun <reified T : Any> get(key: Any, default: T? = null): T? {
        return if (field[key] != null) field[key] as T else default
    }

    operator fun <T : Any> set(key: Any, value: T?) {
        if (value == null) {
            field.remove(key)
        } else {
            field[key] = value
        }
    }

//    /** Support for delegation. */
//    operator fun <T> getValue(thisRef: Any?, property: KProperty<*>): T? {
//    }
//
//    /** Support for delegation. */
//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableList<T>) {
//    }
}

