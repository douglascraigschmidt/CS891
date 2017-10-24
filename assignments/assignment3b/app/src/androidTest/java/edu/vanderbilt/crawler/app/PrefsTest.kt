package edu.vanderbilt.crawler.app

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.vanderbilt.crawler.preferences.Preference
import edu.vanderbilt.crawler.preferences.PreferenceProvider
import edu.vanderbilt.imagecrawler.transforms.Transform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField

class PrefsTest {

    private inline fun <reified T : Any, DELEGATE : Any> findDelegate(
            instance: T, delegatingTo: KClass<DELEGATE>): DELEGATE? {

        for (prop in T::class.declaredMemberProperties) {
            val javaField = prop.javaField ?: continue
            javaField.isAccessible = true // is private, have to open that up
            if (delegatingTo.java.isAssignableFrom(javaField.type)) {
                @Suppress("UNCHECKED_CAST")
                return javaField.get(instance) as DELEGATE
            }
        }

        return null
    }

    class SomeClass {
        val intPref: Int by Preference(0)
        val longPref: Long by Preference(0L)
    }

    @Ignore
    @Test
    fun findDelegateTest() {
        val someClass = SomeClass()
        val result =  findDelegate(someClass, Preference::class)
        println(result)
    }

    @Test
    fun getDefaultsTest() {
        PreferenceProvider.prefs.edit().clear().apply()

        val intPref: Int by Preference(0)
        val longPref: Long by Preference(0L)
        val floatPref: Float by Preference(0f)
        val booleanPref: Boolean by Preference(false)
        val stringPref: String by Preference("")
        val transformTypes: MutableList<Transform.Type> by Preference(mutableListOf<Transform.Type>())

        assertEquals(0, intPref)
        assertEquals(0L, longPref)
        assertEquals(0f, floatPref)
        assertEquals(false, booleanPref)
        assertEquals("", stringPref)
        assertTrue(transformTypes.isEmpty())
    }

    inline fun <reified T> fromJson(json: String): T
            = Gson().fromJson<T>(json, object : TypeToken<T>() {}.type)

    @Test
    fun getSetTest() {
        PreferenceProvider.prefs.edit().clear().apply()

        var intPref: Int by Preference(1)
        var longPref: Long by Preference(1L)
        var floatPref: Float by Preference(1f)
        var booleanPref: Boolean by Preference(true)
        var stringPref: String by Preference("fooBar")
        var transformTypes: MutableList<Transform.Type> by Preference(mutableListOf())

        assertEquals(1, intPref)
        assertEquals(1L, longPref)
        assertEquals(1f, floatPref)
        assertEquals(true, booleanPref)
        assertEquals("fooBar", stringPref)
        assertTrue(transformTypes.isEmpty())

        intPref = 2
        longPref = 2L
        floatPref = 2f
        booleanPref = false
        stringPref = "fooBaz"
        transformTypes = mutableListOf(*Transform.Type.values())

        assertEquals(2, intPref)
        assertEquals(2L, longPref)
        assertEquals(2f, floatPref)
        assertEquals(false, booleanPref)
        assertEquals("fooBaz", stringPref)
        var expected = mutableListOf(*Transform.Type.values())
        var result = transformTypes
        assertEquals(expected, result)

        expected = mutableListOf(Transform.Type.GRAY_SCALE_TRANSFORM)
        transformTypes = expected
        result = transformTypes
        assertEquals(expected, result)
    }

    data class Foo(val int: Int = 0,
                   val long: Long = 0L,
                   val float: Float = 0f,
                   val boolean: Boolean = false,
                   val bar: Bar? = null,
                   val someEnum: SomeEnum = SomeEnum.VALUE1)

    data class Bar(val value: String, val someEnum: SomeEnum = SomeEnum.VALUE1)

    enum class SomeEnum {
        VALUE1,
        VALUE2
    }

    @Test
    fun testGetSetEnums() {
        PreferenceProvider.prefs.edit().clear().apply()

        val bar = Bar("BarValue")
        val foo = Foo(Int.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, true, bar)

        var barPref: Bar by Preference(Bar("BarValue"))
        var fooPref: Foo by Preference(Foo(Int.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, true, bar))

        assertEquals(bar, barPref)
        assertEquals(foo, fooPref)

        val barNew = Bar("NewBarValue", SomeEnum.VALUE2)
        val fooNew = Foo(Int.MIN_VALUE, Long.MIN_VALUE, Float.MIN_VALUE, false, bar, SomeEnum.VALUE2)

        barPref = barNew
        fooPref = fooNew

        assertEquals(barNew, barPref)
        assertEquals(fooNew, fooPref)

    }
}
