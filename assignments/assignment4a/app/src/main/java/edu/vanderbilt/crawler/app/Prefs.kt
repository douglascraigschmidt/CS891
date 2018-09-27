package edu.vanderbilt.crawler.app

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson

/**
 * Created by monte on 08/10/17.
 */
object Prefs {
    val prefs = default()

    fun default(context: Context = App.instance): SharedPreferences
            = PreferenceManager.getDefaultSharedPreferences(context)

    fun custom(context: Context = App.instance, name: String): SharedPreferences
            = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    /**
     * Returns the shared preference that matches the specified [key] or
     * if no match is found, then the [default] value is returned.
     * Classes not supported by standard [SharedPreferences] serialization
     * are restored from Json strings via Gson.
     */
    operator inline fun <reified T : Any> get(key: String, default: T? = null): T? {
        with(prefs) {
            return when (T::class) {
                String::class -> getString(key, default as? String) as T?
                Int::class -> getInt(key, default as? Int ?: -1) as T?
                Boolean::class -> getBoolean(key, default as? Boolean ?: false) as T?
                Float::class -> getFloat(key, default as? Float ?: -1f) as T?
                Long::class -> getLong(key, default as? Long ?: -1) as T?
                else -> {
                    val value = getString(key, null)
                    if (value == null) {
                        default
                    } else {
                        Gson().fromJson(value, T::class.java)
                    }
                }
            }
        }
    }

    /**
     * Sets a shared the preference [value] for the specified [key].
     * Classes not supported by standard [SharedPreferences] serialization
     * are saved as Json strings via Gson.
     */
    operator inline fun <reified T: Any> set(key: String, value: T?) {
        with(prefs) {
            when (value) {
                is String? -> edit { it.putString(key, value) }
                is Int -> edit { it.putInt(key, value) }
                is Boolean -> edit { it.putBoolean(key, value) }
                is Float -> edit { it.putFloat(key, value) }
                is Long -> edit { it.putLong(key, value) }
                else -> {
                    edit { it.putString(key, Gson().toJson(value, T::class.java)) }
                }
            }
        }
    }

    /**
     * Helper used to save SharedPreference values using
     * SharedPreferences.edit instance. This function must
     * be public to support call from inline/reified [set]
     * function.
     */
    inline fun SharedPreferences.edit(
            operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }
}
