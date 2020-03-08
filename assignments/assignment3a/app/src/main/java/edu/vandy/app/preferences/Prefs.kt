package edu.vandy.app.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.vandy.app.App
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
            @Suppress("UNCHECKED_CAST")
            return when (default) {
                is Int -> getInt(name, default)
                is Long -> getLong(name, default)
                is Float -> getFloat(name, default)
                is Boolean -> getBoolean(name, default)
                is String -> getString(name, default) ?: ""
                else -> {
                    val string = getString(name, null)
                    when {
                        string == null -> default
                        adapter != null -> adapter.decode(string) ?: default
                        else -> Gson().fromJson<T>(string, object : TypeToken<T>() {}.type)
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
            }.commit()
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
    fun default(context: Context = App.instance): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Custom preferences is not currently used.
     */
    fun custom(context: Context = App.instance, name: String): SharedPreferences =
            context.getSharedPreferences(name, Context.MODE_PRIVATE)

    /**
     * Registers the passed [listener] to receive
     * shared preference changes notifications.
     */
    fun addListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregisters the passed [listener] from receiving
     * shared preference change notifications.
     */
    fun removeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Clears all preferences stored with this provider.
     */
    @SuppressLint("CommitPrefEdits")
    fun clear() {
        // Clear all shared preferences.
        prefs.run {
            with(edit()) {
                clear()
            }.commit()
        }
    }

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
 * TODOx:
 * I really don't like using this class because it requires way too
 * much ceremony. Here is what it looks like using this class:
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

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
        try {
            if (key == name) {
                with(pref) {
                    @Suppress("UNCHECKED_CAST")
                    val value = when (default) {
                        is Int -> getInt(name, default)
                        is Long -> getLong(name, default)
                        is Float -> getFloat(name, default)
                        is Boolean -> getBoolean(name, default)
                        is String -> getString(name, default) ?: ""
                        else -> {
                            val string = getString(name, null)
                            when {
                                string == null -> default
                                adapter != null -> adapter.decode(string) ?: default
                                else -> Gson().fromJson<T>(string, object : TypeToken<T>() {}.type)
                            }
                        }
                    } as T

                    subscriber.subscriber.invoke(value)
                }
            }
        } catch (e: Exception) {
            error("ObservablePreference for key $key requires a custom adapter: $e")
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        subscriber.unsubscribe {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}

/**
 * Preference adapter class for Range<Int> required since
 * PreferenceObserver can't automatically handle complex objects.
 */
open class EnumAdapter<T : Enum<T>>(enumType: Class<T>) : Adapter<T> {
    private val enumConstants: Array<T> = enumType.enumConstants!!

    override fun encode(value: T): String {
        return value.ordinal.toString()
    }

    override fun decode(string: String): T? {
        try {
            return enumConstants[string.toInt()]
        } catch (e: Exception) {
            error("Preferences EnumAdapter decode failed (value=[$string]): $e")
        }
    }
}

/**
 * Preference adapter class for Size values that saves all
 * sizes in dp and restores those values in pixels. This
 * allows defining default size values in dp units.
 * ensures that size default values will remain the same
 * PreferenceObserver can't automatically handle complex objects.
 */

/**
 * Keeps tracks of a collection of unsubscribe handlers which
 * are invoked when when this composite unsubscriber is invoked.
 * Currently, this class is only used to remove shared preference
 * listeners when an activity is being destroyed.
 */
class CompositeUnsubscriber {
    private val unsubscribers: MutableCollection<() -> Unit> = mutableSetOf()
    private var invoked = false

    fun add(unsubscriber: () -> Unit) = unsubscribers.add(unsubscriber)

    fun invoke() {
        if (invoked) {
            throw IllegalStateException("This group is already invoked once")
        }
        unsubscribers.forEach { it.invoke() }
        invoked = true
    }
}
