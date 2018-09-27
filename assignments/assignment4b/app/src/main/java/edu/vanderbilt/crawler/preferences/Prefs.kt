package edu.vanderbilt.crawler.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.vanderbilt.crawler.app.App
import kotlin.reflect.KProperty

/**
 * Adapter interface to support custom encoding and decoding
 * an object of type [T] to and from a String.
 */
interface Adapter<T> {
    fun encode(value: T): String
    fun decode(string: String): T?
}

class GsonAdapter<T>: Adapter<T> {
    override fun encode(value: T): String {
        return Gson().toJson(value)
    }

    override fun decode(string: String): T? {
        return Gson().fromJson<T>(string, object : TypeToken<T>() {}.type)
    }
}

/**
 * Preferences delegate class. Note that any collection based preferences will
 * not automatically update their associated shared preference entries when
 * elements of their collection are added, removed, or modified. To overcome
 * this limitation, shared preference collections should be declared as
 * immutable objects and any editing of these objects should be performed in
 * a temporary copy of the collection and then the original shared preference
 * property should be reassigned the temporary edited collection object.
 */
class Preference<T: Any?>(val default: T, val name: String? = null, val adapter: Adapter<T>? = null) {
    //: ReadWriteProperty<Any?, T> {

    val prefs: SharedPreferences = PreferenceProvider.prefs
    lateinit var actualName: String

    operator inline fun <reified T: Any?> getValue(thisRef: Any?, property: KProperty<*>): T {
        return get(name ?: "${this::class.java.name}.${property.name}", default as T)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        set(name ?: "${this::class.java.name}.${property.name}", value)
    }

    /**
     * Returns the value of the shared preference with the specified [name].
     * or the [default] value if the shared preference has no current value.
     */
    inline fun <reified T> get(name: String, default: T): T {
        with(prefs) {
            @Suppress("UNCHECKED_CAST")
            return when (default) {
                is Int -> getInt(name, default)
                is Long -> getLong(name, default)
                is Float -> getFloat(name, default)
                is Boolean -> getBoolean(name, default)
                is String -> getString(name, default) ?: ""
                else -> {
                    if (adapter != null) {
                        adapter.decode(getString(name, null)) ?: default
                    } else {
                        val string = getString(name, null)
                        if (string != null) {
                            Gson().fromJson<T>(string, object : TypeToken<T>() {}.type)
                        } else {
                            default
                        }
                    }
                }
            } as T
        }
    }

    /**
     * Sets this shared preference instance to the specified [value].
     */
    @SuppressLint("CommitPrefEdits")
    fun set(name: String, value: T) {
        with(prefs.edit()) {
            when (value) {
                is Int -> putInt(name, value)
                is Long -> putLong(name, value)
                is Float -> putFloat(name, value)
                is Boolean -> putBoolean(name, value)
                is String -> putString(name, value)
                else -> {
                    if (adapter != null) {
                        putString(name, adapter.encode(value))
                    } else {
                        putString(name, GsonAdapter<T>().encode(value))
                    }
                }
            }.apply()
        }
    }

    /**
     * Clears the current value of this shared preference instance.
     */
    @SuppressLint("CommitPrefEdits")
    fun clear(name: String) {
        if (prefs.contains(name)) {
            with(prefs.edit()) {
                remove(name)
            }.apply()
        }
    }
}

/**
 * Shared preferences object that supports [] style access and
 * returns sensible default values when no default is specified.
 */
object PreferenceProvider {
    var prefs = default()

    /**
     * Default application shared preferences.
     */
    fun default(context: Context = App.instance): SharedPreferences
            = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Custom preferences is not currently used.
     */
    fun custom(context: Context = App.instance, name: String): SharedPreferences
            = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    /**
     * Return typed object from Json (hides TypeToken<T>(){}.getType()).
     */
    @Suppress("MemberVisibilityCanPrivate")
    inline fun <reified T> Gson.fromJson(json: String): T?
            = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

    /**
     * Converts passed Json [json] string to an instance of [clazz]
     */
    inline fun <reified T> fromJson(json: String, clazz: Class<T>): T? {
        return Gson().fromJson(json, clazz)
    }

    /**
     * Converts [value] instance to Json. Does not support enum classes.
     * Throws an [IllegalArgumentException] if the [value] cannot be
     * restored from Json.
     */
    @Suppress("MemberVisibilityCanPrivate")
    inline fun <reified T> toJson(value: T): String {
        return Gson().toJson(value)
    }
}
