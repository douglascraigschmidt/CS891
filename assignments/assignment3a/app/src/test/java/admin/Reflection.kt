package admin

import org.junit.Assert
import java.lang.reflect.Field

inline fun <reified T> Any.firstField(): Field? =
        javaClass.declaredFields.firstOrNull { field: Field ->
            field.type == T::class.java
        }

inline fun <reified T> Any.getField(name: String): T = getField(name, T::class.java)

inline fun <reified T> Any.getField(name: String, type: Class<*>): T {
    return javaClass.findField(name, type).let {
        val wasAccessible = it.isAccessible
        it.isAccessible = true
        val result = it.get(this)
        it.isAccessible = wasAccessible
        result as T
    }
}

inline fun <reified T> Any.setField(name: String, value: T) {
    setField(name, value, T::class.java)
}

fun Any.injectInto(parent: Any): Any {
    val field = parent::class.java.findField("", javaClass)
    parent.setField(field.name, this, javaClass)
    return this
}

inline fun <reified T> Any.inject(type: Class<T>, value: T) {
    val field = javaClass.findField("", type)
    setField(field.name, value, type)
}

inline fun <reified T> Any.inject(value: Any) {
    val field = javaClass.findField("", T::class.java)
    setField(field.name, value, T::class.java)
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
            val wasAccessible = it.isAccessible
            try {
                it.isAccessible = true
                (name.isBlank() || it.name == name) && it.type == type
            } finally {
                it.isAccessible = wasAccessible
            }
        } ?: superclass!!.findField(name, type)
    } catch (e: Exception) {
        throw Exception("Class field $name with type $type does not exist")
    }
}

var Any.outerclass: Any
    get() = javaClass.superclass!!
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

