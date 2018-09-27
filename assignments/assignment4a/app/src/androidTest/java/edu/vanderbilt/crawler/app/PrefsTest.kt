package edu.vanderbilt.crawler.app

import org.junit.Test

import org.junit.Assert.*

class PrefsTest {
    @Test
    fun getPrefs() {
        val string = "SomeString"
        val int = Int.MAX_VALUE
        //val long = Long.MAX_VALUE
        val float = Float.MAX_VALUE
        val boolean = true
        val bar = Bar("BarValue")
        val foo = Foo(Int.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, true, bar)

        Prefs["Int"] = int
        //Prefs["Long"] = long
        Prefs["Float"] = float
        Prefs["Boolean"] = boolean
        Prefs["Foo"] = foo
        Prefs["String"] = string

        //assertEquals(long, Prefs["Long"])
        assertEquals(int, Prefs["Int"])
        assertEquals(float, Prefs["Float"])
        assertEquals(boolean, Prefs["Boolean"])
        assertEquals(foo, Prefs["Foo"])
        assertEquals(string, Prefs["String"])
    }

    data class Foo(val int: Int,
                  val long: Long,
                  val float: Float,
                  val boolean: Boolean,
                  val bar: Bar)

    data class Bar(val value: String)
}