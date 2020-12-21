/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mvalceleanu.easypermissions.utils

import android.util.Log
import mvalceleanu.easypermissions.annotations.AfterPermissionGranted
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

private const val TAG = "AnnotationsUtils"

/**
 * Utils for notify things related permissions like declined, granted via annotations
 */
object AnnotationsUtils {

    /**
     * Find all methods annotated with [AfterPermissionGranted] on a given object with the
     * correct requestCode argument.
     *
     * @param receiver the object with annotated methods.
     * @param annotationClass the annotated class what we want to invoke
     * @param predicate execute annotated method that matches with condition
     */
    internal fun <T : Annotation> notifyAnnotatedMethods(
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
     * @param receiver the object with annotated methods.
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
