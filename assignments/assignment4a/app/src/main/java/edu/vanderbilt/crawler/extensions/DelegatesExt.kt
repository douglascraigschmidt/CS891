package edu.vanderbilt.crawler.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
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
    fun <T> preference(context: Context, name: String, default: T)
            = Preference(context, name, default)
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
 * Preferences helper class.
 */
class Preference<out T>(val context: Context, val name: String, val default: T) {
    val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("default", Context.MODE_PRIVATE)
    }

    operator inline fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
        with(prefs) {
            return when (default) {
                is Long -> getLong(name, default)
                is String -> getString(name, default)
                is Int -> getInt(name, default)
                is Boolean -> getBoolean(name, default)
                is Float -> getFloat(name, default)
                else -> {
                    val value = getString(name, null)
                    if (value == null) {
                        default
                    } else {
                        Gson().fromJson(value, T::class.java) as Any
                    }
                }
            } as T
        }
    }

    @SuppressLint("CommitPrefEdits")
    operator inline fun <reified T> setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        with (prefs.edit()) {
            when (value) {
                is Long -> putLong(name, value)
                is String -> putString(name, value)
                is Int -> putInt(name, value)
                is Boolean -> putBoolean(name, value)
                is Float -> putFloat(name, value)
                else -> {
                    putString(name, Gson().toJson(value, T::class.java))
                }
            }.apply()
        }
    }

    operator inline fun <reified T>get(name: String, default: T): T = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> {
                val value = getString(name, null)
                if (value == null) {
                    default as Any
                } else {
                    Gson().fromJson(value, T::class.java) as Any
                }
            }
        }

        res as T
    }

    @SuppressLint("CommitPrefEdits")
    operator inline fun <reified T> set(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> {
                putString(name, Gson().toJson(value, T::class.java))
            }
        }.apply()
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
        is Long -> putLong(key, value)
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Boolean -> putBoolean(key, value)
        is Float -> putFloat(key, value)
        is Bundle -> putBundle(key, value) // Must be before Parcelable
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        else -> {
            putString(key, Gson().toJson(value, T::class.java))
        }
    }
}
