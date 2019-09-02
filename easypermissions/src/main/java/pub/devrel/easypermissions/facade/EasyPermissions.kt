package pub.devrel.easypermissions.facade

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.Size
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.annotations.AfterPermissionGranted
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList

/**
 * Utility to request and check System permissions for apps targeting Android M (API &gt;= 23).
 */
object EasyPermissions {

    private val TAG = "EasyPermissions"

    /**
     * Callback interface to receive the results of `EasyPermissions.requestPermissions()`
     * calls.
     */
    interface PermissionCallbacks : ActivityCompat.OnRequestPermissionsResultCallback {

        fun onPermissionsGranted(requestCode: Int, perms: List<String>)

        fun onPermissionsDenied(requestCode: Int, perms: List<String>)
    }

    /**
     * Callback interface to receive button clicked events of the rationale dialog
     */
    interface RationaleCallbacks {
        fun onRationaleAccepted(requestCode: Int)

        fun onRationaleDenied(requestCode: Int)
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context the calling context.
     * @param perms   one ore more permissions, such as [Manifest.permission.CAMERA].
     * @return true if all permissions are already granted, false if at least one permission is not
     * yet granted.
     * @see Manifest.permission
     */
    fun hasPermissions(
        context: Context,
        @Size(min = 1) vararg perms: String
    ): Boolean {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default")

            // DANGER ZONE!!! Changing this will break the library.
            return true
        }

        // Null context may be passed if we have detected Low API (less than M) so getting
        // to this point with a null context should not be possible.
        if (context == null) {
            throw IllegalArgumentException("Can't check permissions for null context")
        }

        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    /**
     * Request a set of permissions, showing a rationale if the system requests it.
     *
     * @param host        requesting context.
     * @param rationale   a message explaining why the application needs this set of permissions;
     * will be displayed if the user rejects the request the first time.
     * @param requestCode request code to track this request, must be &lt; 256.
     * @param perms       a set of permissions to be requested.
     * @see Manifest.permission
     */
    fun requestPermissions(
        host: Activity, rationale: String,
        requestCode: Int, @Size(min = 1) vararg perms: String
    ) {
        requestPermissions(
            PermissionRequest.Builder(host, requestCode, *perms)
                .setRationale(rationale)
                .build()
        )
    }

    /**
     * Request permissions from a Support Fragment with standard OK/Cancel buttons.
     *
     * @see .requestPermissions
     */
    fun requestPermissions(
        host: Fragment, rationale: String,
        requestCode: Int, @Size(min = 1) vararg perms: String
    ) {
        requestPermissions(
            PermissionRequest.Builder(host, requestCode, *perms)
                .setRationale(rationale)
                .build()
        )
    }

    /**
     * Request a set of permissions.
     *
     * @param request the permission request
     * @see PermissionRequest
     */
    fun requestPermissions(request: PermissionRequest) {

        // Check for permissions before dispatching the request
        if (hasPermissions(request.helper.context, *request.perms)) {
            notifyAlreadyHasPermissions(
                request.helper.host, request.requestCode, request.perms
            )
            return
        }

        // Request permissions
        request.helper.requestPermissions(
            request.rationale,
            request.positiveButtonText,
            request.negativeButtonText,
            request.theme,
            request.requestCode,
            *request.perms
        )
    }

    /**
     * Handle the result of a permission request, should be called from the calling [ ]'s [ActivityCompat.OnRequestPermissionsResultCallback.onRequestPermissionsResult] method.
     *
     *
     * If any permissions were granted or denied, the `object` will receive the appropriate
     * callbacks through [PermissionCallbacks] and methods annotated with [ ] will be run if appropriate.
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param receivers    an array of objects that have a method annotated with [                     ] or implement [PermissionCallbacks].
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        vararg receivers: Any
    ) {
        // Make a collection of granted and denied permissions from the request.
        val granted = ArrayList<String>()
        val denied = ArrayList<String>()
        for (i in permissions.indices) {
            val perm = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }

        // iterate through all receivers
        for (`object` in receivers) {
            // Report granted permissions, if any.
            if (!granted.isEmpty()) {
                if (`object` is PermissionCallbacks) {
                    `object`.onPermissionsGranted(requestCode, granted)
                }
            }

            // Report denied permissions, if any.
            if (!denied.isEmpty()) {
                if (`object` is PermissionCallbacks) {
                    `object`.onPermissionsDenied(requestCode, denied)
                }
            }

            // If 100% successful, call annotated methods
            if (!granted.isEmpty() && denied.isEmpty()) {
                runAnnotatedMethods(`object`, requestCode)
            }
        }
    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * **Note**: Due to a limitation in the information provided by the Android
     * framework permissions API, this method only works after the permission
     * has been denied and your app has received the onPermissionsDenied callback.
     * Otherwise the library cannot distinguish permanent denial from the
     * "not yet denied" case.
     *
     * @param host              context requesting permissions.
     * @param deniedPermissions list of denied permissions, usually from [                          ][PermissionCallbacks.onPermissionsDenied]
     * @return `true` if at least one permission in the list was permanently denied.
     */
    fun somePermissionPermanentlyDenied(
        host: Activity,
        deniedPermissions: List<String>
    ): Boolean {
        return PermissionsHelper.newInstance(host)
            .somePermissionPermanentlyDenied(deniedPermissions)
    }

    /**
     * @see .somePermissionPermanentlyDenied
     */
    fun somePermissionPermanentlyDenied(
        host: Fragment,
        deniedPermissions: List<String>
    ): Boolean {
        return PermissionsHelper.newInstance(host)
            .somePermissionPermanentlyDenied(deniedPermissions)
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @param host             context requesting permissions.
     * @param deniedPermission denied permission.
     * @return `true` if the permissions has been permanently denied.
     */
    fun permissionPermanentlyDenied(
        host: Activity,
        deniedPermission: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).permissionPermanentlyDenied(deniedPermission)
    }

    /**
     * @see .permissionPermanentlyDenied
     */
    fun permissionPermanentlyDenied(
        host: Fragment,
        deniedPermission: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).permissionPermanentlyDenied(deniedPermission)
    }

    /**
     * See if some denied permission has been permanently denied.
     *
     * @param host  requesting context.
     * @param perms array of permissions.
     * @return true if the user has previously denied any of the `perms` and we should show a
     * rationale, false otherwise.
     */
    fun somePermissionDenied(
        host: Activity,
        vararg perms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).somePermissionDenied(*perms)
    }

    /**
     * @see .somePermissionDenied
     */
    fun somePermissionDenied(
        host: Fragment,
        vararg perms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).somePermissionDenied(*perms)
    }

    /**
     * Run permission callbacks on an object that requested permissions but already has them by
     * simulating [PackageManager.PERMISSION_GRANTED].
     *
     * @param object      the object requesting permissions.
     * @param requestCode the permission request code.
     * @param perms       a list of permissions requested.
     */
    private fun notifyAlreadyHasPermissions(
        `object`: Any,
        requestCode: Int,
        perms: Array<String>
    ) {
        val grantResults = IntArray(perms.size)
        for (i in perms.indices) {
            grantResults[i] = PackageManager.PERMISSION_GRANTED
        }

        onRequestPermissionsResult(requestCode, perms, grantResults, `object`)
    }

    /**
     * Find all methods annotated with [AfterPermissionGranted] on a given object with the
     * correct requestCode argument.
     *
     * @param object      the object with annotated methods.
     * @param requestCode the requestCode passed to the annotation.
     */
    private fun runAnnotatedMethods(`object`: Any, requestCode: Int) {
        var clazz: Class<*>? = `object`.javaClass
        if (isUsingAndroidAnnotations(`object`)) {
            clazz = clazz!!.superclass
        }

        while (clazz != null) {
            for (method in clazz.declaredMethods) {
                val ann = method.getAnnotation(AfterPermissionGranted::class.java)
                if (ann != null) {
                    // Check for annotated methods with matching request code.
                    if (ann.value == requestCode) {
                        // Method must be void so that we can invoke it


                        try {
                            // Make method accessible if private
                            if (!method.isAccessible) {
                                method.isAccessible = true
                            }
                            method.invoke(`object`)
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
     */
    private fun isUsingAndroidAnnotations(`object`: Any): Boolean {
        if (!`object`.javaClass.simpleName.endsWith("_")) {
            return false
        }
        try {
            val clazz = Class.forName("org.androidannotations.api.view.HasViews")
            return clazz.isInstance(`object`)
        } catch (e: ClassNotFoundException) {
            return false
        }

    }
}
