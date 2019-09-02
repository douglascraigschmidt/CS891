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
 * Preferences delegate class. Note that any collection based preferences will
 * not automatically update their associated shared preference entries when
 * elements of their collection are added, removed, or modified. To overcome
 * this limitation, shared preference collections should be declared as
 * immutable objects and any editing of these objects should be performed in
 * a temporary copy of the collection and then the original shared preference
 * property should be reassigned the temporary edited collection object.
 *
 * It would be nice to have this class implement ReadWriteProperty<Any?, T>
 * but that isn't possible because if getValue uses <reified T: Any?> which
 * is results in a different signature from the getValue declaration in
 * the ReadWriteProperty interface.
 *
 * The benefit of using a reified getValue method is that non-standard
 * property types can be automatically saved and restored using typed
 * Gson Strings rather than requiring delegate declarations to specify
 * typed Gson adapter.
 *
 * It would be really nice to somehow allow property values to access this
 * Preference delegate to directly call it's methods, but Kotlin generics
 * only make it possible to find the delegate instance based on data type
 * which would only work if there was just one Preference<Type> declaration
 * per class. If there are two or more, there is no way to use generics
 * to determine the correct instance to use for declared values that
 * delegate to the common Preference<Type> signature. The reason why
 * it would be nice to be able to access the Preference instance would be
 * to allow calling property.clear() to clear a shared preference or, to
 * set a shared preference change Observer on any given preference key value.
 */
open class Preference<T : Any>(val default: T,
                               val name: String? = null,
                               val adapter: Adapter<T>? = null,
                               val subscriber: Subscriber<T>? = null) {

    companion object {
        /** Can be used to trace get/set of any shared pref */
        val tracePref = ""
    }

    /**
     * Constructor that can be used with a PreferenceEntry that may contain
     * an optional adapter and subscriber.
     */
    constructor(preferenceEntry: PreferenceEntry<T>)
            : this(preferenceEntry.value,
            preferenceEntry.key,
            preferenceEntry.adapter,
            preferenceEntry.subscriber)

    val prefs: SharedPreferences = PreferenceProvider.prefs
    lateinit var actualName: String // not currently working

    inline operator fun <reified T : Any?> getValue(thisRef: Any?, property: KProperty<*>): T {
        return try {
            get(name ?: "${this::class.java.name}.${property.name}", default as T)
        } catch (e: Exception) {
            default as T
        }
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
            val value = when (default) {
                is Int -> getInt(name, default)
                is Long -> getLong(name, default)
                is Float -> getFloat(name, default)
                is Boolean -> getBoolean(name, default)
                is String -> getString(name, default) ?: ""
                else -> {
                    getString(name, null)?.let {
                        if (adapter != null) {
                            adapter.decode(it)
                        } else {
                            Gson().fromJson(it, object : TypeToken<T>() {}.type)
                        }
                    } ?: default
                }
            } as T

            // For debug tracing of a pref
            if (name == tracePref) {
                println("PREF-GET: $name = $value (default = $default)")
            }

            return value
        }
    }

    /**
     * Sets this shared preference instance to the specified [value].
     */
    @SuppressLint("CommitPrefEdits")
    fun set(name: String, value: T) {
        // For debug tracing of a pref
        if (name == tracePref) {
            println("PREF-SET: $name = $value")
        }

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
    fun default(context: Context = App.instance): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Custom preferences is not currently used.
     */
    fun custom(context: Context = App.instance, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    /**
     * Return typed object from Json (hides TypeToken<T>(){}.getType()).
     */
    @Suppress("MemberVisibilityCanPrivate")
    inline fun <reified T> Gson.fromJson(json: String): T? = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

    /**
     * Converts passed Json [json] string to an instance of [clazz]
     */
    inline fun <reified T> fromJson(json: String, clazz: Class<T>): T? =
            Gson().fromJson(json, clazz)

    /**
     * Converts [value] instance to Json. Does not support enum classes.
     * Throws an [IllegalArgumentException] if the [value] cannot be
     * restored from Json.
     */
    @Suppress("MemberVisibilityCanPrivate")
    inline fun <reified T> toJson(value: T): String = Gson().toJson(value)
}

/**
 * A single preference entry that maps a key to a default value and
 * provides an optional adapter and optional subscriber.
 */
class PreferenceEntry<T>(val key: String,
                         val value: T,
                         val adapter: Adapter<T>? = null,
                         val subscriber: Subscriber<T>? = null)

/**
 * Adapter interface to support custom encoding and decoding
 * an object of type [T] to and from a String.
 */
interface Adapter<T> {
    fun encode(value: T): String
    fun decode(string: String): T?
}

class GsonAdapter<T> : Adapter<T> {
    override fun encode(value: T): String = Gson().toJson(value)

    override fun decode(string: String): T? =
            Gson().fromJson<T>(string, object : TypeToken<T>() {}.type)
}

interface Subscriber<T> {
    val subscriber: (T) -> Unit
    fun unsubscribe(callback: () -> Unit)
}

/**
 * Can be used to observe shared preference changes as an alternative
 * to simply declaring a shared preference property using
 * "by Preference(...)".
 *
 * <pre>{@code
 *   private val compositeUnsubscriber = CompositeUnsubscriber()
 *   private var crawlSpeed: Int by ObservablePreference(
 *     default = Settings.DEFAULT_CRAWL_SPEED,
 *     name = Settings.CRAWL_SPEED_PREF,
 *     subscriber = object : Subscriber<Int> {
 *     override val subscriber: (Int) -> Unit
 *       get() = {
 *         speedSeekBar?.progress = it
 *         viewModel.crawlSpeed = it
 *       }
 *
 *       override fun unsubscribe(callback: () -> Unit) {
 *         compositeUnsubscriber.add(callback)
 *       }
 *    })
 * }</pre>
 *
 * Since a class can't use reified type parameters, this class
 * is forced to redefine the preference get function without
 * using the inline/reified keywords. Also this means that
 * all complex objects require an adapter since gson is not able
 * to automatically determine the correct type adapter to use
 * when converting to/from JSON.
 */
class ObservablePreference<T : Any>(default: T,
                                    name: String,
                                    adapter: Adapter<T>? = null,
                                    subscriber: Subscriber<T>)
    : Preference<T>(default, name, adapter) {

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        try {
            if (key == name) {
                with(prefs) {
                    @Suppress("UNCHECKED_CAST")
                    val value = when (default) {
                        is Int -> getInt(name, default)
                        is Long -> getLong(name, default)
                        is Float -> getFloat(name, default)
                        is Boolean -> getBoolean(name, default)
                        is String -> getString(name, default) ?: ""
                        else -> {
                            getString(name, null)?.let {
                                if (adapter != null) {
                                    adapter.decode(it)
                                } else {
                                    Gson().fromJson(it, object : TypeToken<T>() {}.type)
                                }
                            } ?: default as Any
                        }
                    } as T

                    subscriber.subscriber.invoke(value)
                }
            }
        } catch (e: Exception) {
            error("ObservablePreference for key $key failed: $e")
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        subscriber.unsubscribe {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}

class CompositeUnsubscriber {
    private val destroyListeners: MutableCollection<() -> Unit> = mutableSetOf()
    private var invoked = false

    /**
     * Add a new destroy listener to the group
     * @param listener the destroy listener that is going to be added in the group
     */
    fun add(listener: () -> Unit) = destroyListeners.add(listener)

    /**
     * Invoke all destroy listeners
     * **Note:** every group can be invoked **only once**.
     */
    fun invoke() {
        if (invoked) {
            throw IllegalStateException("This group is already invoked once")
        }
        destroyListeners.forEach { it.invoke() }
        invoked = true
    }
}
