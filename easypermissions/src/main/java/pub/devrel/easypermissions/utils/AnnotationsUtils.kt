package pub.devrel.easypermissions.utils

import android.util.Log
import pub.devrel.easypermissions.annotations.AfterPermissionGranted
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

private const val TAG = "AnnotationsUtils"

object AnnotationsUtils {

    /**
     * Find all methods annotated with [AfterPermissionGranted] on a given object with the
     * correct requestCode argument.
     *
     * @param receiver      the object with annotated methods.
     * @param annotationClass the annotated class what we want to invoke
     * @param predicate  execute annotated method that matches with condition
     */
    internal fun <T: Annotation> notifyAnnotatedMethods(
        receiver: Any,
        annotationClass: KClass<T>,
        predicate: (T) -> (Boolean)
    ) {
        var clazz: Class<*>? = receiver.javaClass
        if (isUsingAndroidAnnotations(receiver)) {
            clazz = clazz?.superclass
        }

        while (clazz != null) {
            for (method in clazz.declaredMethods) {
                method.getAnnotation(annotationClass.java)?.let {
                    if (predicate(it)) {
                        // Method must be void so that we can invoke it
                        try {
                            // Make method accessible if private
                            if (!method.isAccessible) {
                                method.isAccessible = true
                            }
                            method.invoke(receiver)
                        } catch (e: IllegalAccessException) {
                            Log.e(TAG, "runDefaultMethod:IllegalAccessException", e)
                        } catch (e: InvocationTargetException) {
                            Log.e(TAG, "runDefaultMethod:InvocationTargetException", e)
                        }
                    }
                }
            }
            clazz = clazz.superclass
        }
    }

    /**
     * Determine if the project is using the AndroidAnnotations library.
     *
     * @param receiver      the object with annotated methods.
     */
    private fun isUsingAndroidAnnotations(receiver: Any): Boolean {
        if (!receiver.javaClass.simpleName.endsWith("_")) {
            return false
        }
        return try {
            val clazz = Class.forName("org.androidannotations.api.view.HasViews")
            clazz.isInstance(receiver)
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}