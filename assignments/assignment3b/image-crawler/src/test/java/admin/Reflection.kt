package admin

import org.junit.Assert
import java.lang.reflect.Field

inline fun <reified T> Any.getField(name: String): T {
    return getField(name, T::class.java)
}

inline fun <reified T> Any.getJavaPrimitiveField(name: String, type: Class<*>): T {
    return getField(name, type)
}

inline fun <reified T> Any.setField(name: String, value: T) {
    setField(name, value, T::class.java)
}

inline fun <reified T> Any.getField(name: String, type: Class<*>): T {
    return javaClass.findField(name, type).let {
        val wasAccessible = it.isAccessible
        it.isAccessible = true
        val result = it.get(this)
        it.isAccessible = wasAccessible
        result as T
    }
}

inline fun <reified T> Any.setJavaPrimitiveField(name: String, value: T) {
    val javaPrimitiveType = when (value) {
        is Int -> Int::class.javaPrimitiveType
        is Float -> Float::class.javaPrimitiveType
        is Double -> Double::class.javaPrimitiveType
        is Short -> Short::class.javaPrimitiveType
        else -> throw Exception("value is not a have an equivalent Java primitive type")
    }
    javaClass.findField(name, javaPrimitiveType!!).let {
        val wasAccessible = it.isAccessible
        it.isAccessible = true
        it.set(this, value)
        it.isAccessible = wasAccessible
    }
}

inline fun <reified T> Any.setField(name: String, value: T, type: Class<*>?) {
    javaClass.findField(name, type!!).let {
        val wasAccessible = it.isAccessible
        it.isAccessible = true
        it.set(this, value)
        it.isAccessible = wasAccessible
    }
}

fun Class<*>.findField(name: String, type: Class<*>): Field {
    try {
        return declaredFields.firstOrNull {
            (name.isBlank() || it.name == name) && it.type == type
        } ?: superclass.findField(name, type)
    } catch (e: Exception) {
        throw Exception("Class field $name with type $type does not exist")
    }
}

var Any.outerclass: Any
    get() = javaClass.superclass
    set(value) {
        val field = javaClass.getDeclaredField("this$0")
        val wasAccessible = field.isAccessible
        field.isAccessible = true
        field.set(this, value)
        field.isAccessible = wasAccessible
    }

fun Any.reflectiveEquals(expected: Any): Boolean {
    val fields = javaClass.declaredFields
    val expectedFields = expected.javaClass.declaredFields
    Assert.assertEquals(expectedFields.size, fields.size)
    for (i in 0..fields.lastIndex) {
        if (fields.get(i) != expectedFields.get(i)) {
            return false
        }
    }

    return true
}

