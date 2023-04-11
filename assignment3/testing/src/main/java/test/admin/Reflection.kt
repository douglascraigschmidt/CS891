package test.admin

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 *  Generic extension function that finds the receiver's first member
 *  Field that matches the passed value type [T] and optionally the
 *  passed field [name] and then injects the passed [value] into that
 *  field.
 *
 *  @return The receiver.
 */
inline fun <reified S, reified T> S.setValue(value: T, name: String = ""): S {
    // Let the value extension function do the work.
    value.injectInto(this as Any, name)
    return this
}

/**
 *  Generic extension function that injects the receiver value into
 *  the passed [parent] class field that matches either the receiver
 *  type and optional an exact field name.
 */
inline fun <reified T> T.injectInto(parent: Any, name: String = ""): T {
    // Determine if type of this extended object (primitive or normal).
    val type = when (this) {
        is Int -> Int::class.javaPrimitiveType
        is Float -> Float::class.javaPrimitiveType
        is Double -> Double::class.javaPrimitiveType
        is Short -> Short::class.javaPrimitiveType
        else -> T::class.java
    }

    // Use reflection to access this the parent class member
    // Field that will be the target of this injected object.
    val field = parent::class.java.findField(requireNotNull(type), name)

    // Set the field's value
    parent.setField(field.name, this, type)

    // Return this receiver to allow chaining.
    return this
}

/**
 * A generic object extension function that uses reflection to locate
 * the receiver member Field that matches the passed [type] and an
 * optional Field [name] and then sets that Field value to the passed
 * [value].
 *
 *  @return The receiver.
 */
inline fun <reified T> Any.setField(name: String, value: T, type: Class<*>) {
    javaClass.findField(type, name).runWithAccess(this) {
        set(this@setField, value)
    }
}

/**
 * Looks for a class member with the specified type or name
 * starting in the receiver class and working up through all
 * ancestors superclasses.
 *
 * This method will throw an [Exception] if no matching field is found.
 */
fun Class<*>.findField(type: Class<*>, name: String = ""): Field {
    try {
        return declaredFields.firstOrNull {
            (name.isBlank() || it.name == name) && (it.type == type)
        } ?: requireNotNull(superclass).findField(type, name)
    } catch (e: Exception) {
        throw Exception("Class field $name with type $type does not exist")
    }
}

/**
 * Extensions function for any receiver object type that will temporarily
 * set this [Field] receiver to accessible before running the passed
 * [block] lambda.
 */
inline fun <T> Field.runWithAccess(any: Any, block: Field.() -> T): T {
    // Save old accessibility state and make accessible.
    val wasAccessible = canAccess(if (isStatic()) null else any)
    isAccessible = true

    // Run the passed lambda
    val result = block()

    // Restore original accessibility state.
    isAccessible = wasAccessible
    return result
}

fun Field.isStatic(): Boolean = Modifier.isStatic(modifiers)

inline fun <reified T : Annotation> hasClassAnnotation(
    clazz: Class<*>,
    annotationClass: Class<T>,
    noinline validate: ((annotation: T) -> Boolean) = { true },
    onlyAnnotation: Boolean = false
): Boolean =
    clazz.getAnnotation(annotationClass)?.let(validate) == true
            && !onlyAnnotation || clazz.annotations.size == 1

fun Class<*>.method(name: String, vararg params: Class<*>): Method? =
    if (!params.isNullOrEmpty()) {
        getDeclaredMethod(name, *params)
    } else {
        declaredMethods.firstOrNull { it.name == name }
    }

inline fun <reified T : Annotation> Class<*>.hasMethodAnnotation(
    method: String,
    params: Array<Class<*>> = emptyArray(),
    annotationClass: Class<T>,
    noinline validate: (annotation: T) -> Boolean = { true },
    onlyAnnotation: Boolean = false
): Boolean =
    method(method, *params)?.run {
        getDeclaredAnnotation(annotationClass)?.let(validate) == true
                && (!onlyAnnotation || declaredAnnotations.size == 1)
    } == true

inline fun <reified T : Annotation> Class<*>.hasFieldAnnotation(
    annotationClass: Class<T>,
    field: String,
    onlyAnnotation: Boolean = false
): Boolean =
    with(getDeclaredField(field)) {
        isAnnotationPresent(annotationClass)
                && (!onlyAnnotation || declaredAnnotations.size == 1)
    }

inline fun <reified T : Annotation> Class<*>.hasParameterAnnotation(
    method: String,
    params: Array<Class<*>> = emptyArray(),
    param: Int,
    annotationClass: Class<T>,
    noinline validate: ((annotation: T) -> Boolean) = { true },
    onlyAnnotation: Boolean = true
): Boolean =
    method(method, *params)?.run {
        parameters[param]?.run {
            getAnnotation(annotationClass)?.let { validate(it) } == true
                    && (!onlyAnnotation || declaredAnnotations.size == 1)
        }
    } == true