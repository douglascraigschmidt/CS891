package edu.vanderbilt.crawler.extensions

import android.os.Bundle
import android.os.Parcelable
import com.google.gson.Gson
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 * Helpful delegated extensions.
 */

object DelegatesExt {
    fun <T> delegateVal(value: T) = DelegateValue(value)
    fun <T> notNullSingleValue() = NotNullSingleValueVar<T>()
}

/**
 * Simple generic delegate
 */
class DelegateValue<T>(private var value: T? = null) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

/**
 * Used to ensure that a singleton is only created once.
 */
class NotNullSingleValueVar<T> {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
            = value ?: throw IllegalStateException("${property.name} not initialized")

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value == null) value
        else throw IllegalStateException("${property.name} already initialized")
    }
}

/**
 * Operator for retrieving values from bundles using [] syntax.
 */
operator inline fun <reified T> Bundle.get(key: String?, value: T): T? {
    return when (value) {
        is Long -> getLong(key, value)
        is String -> getString(key, value)
        is Int -> getInt(key, value)
        is Boolean -> getBoolean(key, value)
        is Float -> getFloat(key, value)
        is Bundle -> getBundle(key) as Any // Must be before Parcelable
        is Parcelable -> getParcelable(key)
        is Serializable -> getSerializable(key)
        else -> {
            getString(key, Gson().toJson(value, T::class.java))
        }
    } as T
}

/**
 * Operator for assigning values to bundles using [] syntax.
 */
operator inline fun <reified T> Bundle.set(key: String?, value: T) {
    when (value) {
        is Int -> putInt(key, value)
        is Long -> putLong(key, value)
        is Float -> putFloat(key, value)
        is Boolean -> putBoolean(key, value)
        is String -> putString(key, value)
        is Bundle -> putBundle(key, value) // Must be before Parcelable
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        else -> {
            putString(key, Gson().toJson(value, T::class.java))
        }
    }
}
