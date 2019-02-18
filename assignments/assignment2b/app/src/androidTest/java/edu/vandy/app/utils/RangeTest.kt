package edu.vandy.app.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertNotEquals
import org.junit.Ignore
import org.junit.Test

@Ignore
class RangeTest {
    @Ignore
    @Test
    fun rangeTest() {
        val range = Range(4, 5)
        val toJson = Gson().toJson(range, object : TypeToken<Range<Int>>() {}.type)
        assertNotEquals(toJson, "{}")
    }
}