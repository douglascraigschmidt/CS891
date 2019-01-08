

package edu.vandy.app.preferences

import android.util.Range
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertNotEquals
import org.junit.Ignore
import org.junit.Test

@Ignore
class PreferenceTest {

    @Ignore
    @Test
    fun rangeTest() {
        val range = Range(4, 5)
        val cls = range.javaClass
        val fields = cls.declaredFields
        val lowerField = cls.getField("mLower")
        val upperField = cls.getField("mUpper")

        val toJson = Gson().toJson(range, object : TypeToken<Range<Int>>() {}.type)
        assertNotEquals(toJson, "{}")
    }
}