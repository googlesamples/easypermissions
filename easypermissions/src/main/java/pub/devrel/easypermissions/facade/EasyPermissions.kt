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
import pub.devrel.easypermissions.utils.AnnotationsUtils

private const val TAG = "EasyPermissions"

/**
 * Utility to request and check System permissions for apps targeting Android M (API &gt;= 23).
 */
@Suppress("UNUSED")
object EasyPermissions {

    /**
     * Callback interface to receive the results of `EasyPermissions.requestPermissions()` calls.
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
    @JvmStatic
    fun hasPermissions(
        context: Context?,
        @Size(min = 1) vararg perms: String
    ): Boolean {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default")
            return true
        }

        context?.let {
            return perms.all { perm ->
                ContextCompat.checkSelfPermission(it, perm) == PackageManager.PERMISSION_GRANTED
            }
        } ?: run {
            throw IllegalArgumentException("Can't check permissions for null context")
        }
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
    @JvmStatic
    fun requestPermissions(
        host: Activity,
        rationale: String,
        requestCode: Int,
        @Size(min = 1) vararg perms: String
    ) {
        val request = PermissionRequest.Builder(host)
            .code(requestCode)
            .perms(perms)
            .rationale(rationale)
            .build()
        requestPermissions(host, request)
    }

    /**
     * Request permissions from a Support Fragment with standard OK/Cancel buttons.
     *
     * @see .requestPermissions
     */
    @JvmStatic
    fun requestPermissions(
        host: Fragment,
        rationale: String,
        requestCode: Int,
        @Size(min = 1) vararg perms: String
    ) {
        val request = PermissionRequest.Builder(host.context)
            .code(requestCode)
            .perms(perms)
            .rationale(rationale)
            .build()
        requestPermissions(host, request)
    }

    /**
     * Request a set of permissions.
     *
     * @param host        requesting context.
     * @param request the permission request
     * @see PermissionRequest
     */
    @JvmStatic
    fun requestPermissions(
        host: Fragment,
        request: PermissionRequest
    ) {
        // Check for permissions before dispatching the request
        if (hasPermissions(host.context, request.perms.toString())) {
            notifyAlreadyHasPermissions(host, request.code, request.perms)
        } else {
            PermissionsHelper.newInstance(host).requestPermissions(request)
        }
    }

    /**
     * Request a set of permissions.
     *
     * @param host        requesting context.
     * @param request the permission request
     * @see PermissionRequest
     */
    @JvmStatic
    fun requestPermissions(
        host: Activity,
        request: PermissionRequest
    ) {
        // Check for permissions before dispatching the request
        if (hasPermissions(host, request.perms.toString())) {
            notifyAlreadyHasPermissions(host, request.code, request.perms)
        } else {
            PermissionsHelper.newInstance(host).requestPermissions(request)
        }
    }

    /**
     * Handle the result of a permission request, should be called from the calling [Activity]'s
     * [ActivityCompat.OnRequestPermissionsResultCallback.onRequestPermissionsResult] method.
     *
     * If any permissions were granted or denied, the `object` will receive the appropriate
     * callbacks through [PermissionCallbacks] and methods annotated with [AfterPermissionGranted]
     * will be run if appropriate.
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param receivers    an array of objects that have a method annotated with
     * [AfterPermissionGranted] or implement [PermissionCallbacks].
     */
    @JvmStatic
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        vararg receivers: Any
    ) {
        val groupedPermissionsResult = grantResults
            .zip(permissions)
            .groupBy({ it.first }, { it.second })

        val grantedList = groupedPermissionsResult[PackageManager.PERMISSION_GRANTED] ?: emptyList()
        val deniedList = groupedPermissionsResult[PackageManager.PERMISSION_GRANTED] ?: emptyList()

        receivers.forEach { receiver ->
            if (receiver is PermissionCallbacks) {
                if (grantedList.isNotEmpty()) {
                    receiver.onPermissionsGranted(requestCode, grantedList)
                }

                if (deniedList.isNotEmpty()) {
                    receiver.onPermissionsDenied(requestCode, deniedList)
                }
            }

            if (grantedList.isNotEmpty() && deniedList.isEmpty()) {
                AnnotationsUtils.notifyAnnotatedMethods(receiver, AfterPermissionGranted::class) {
                    it.value == requestCode
                }
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
     * @param deniedPerms list of denied permissions, usually from
     * [PermissionCallbacks.onPermissionsDenied]
     * @return `true` if at least one permission in the list was permanently denied.
     */
    @JvmStatic
    fun somePermissionPermanentlyDenied(
        host: Activity,
        @Size(min = 1) vararg deniedPerms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).somePermissionPermanentlyDenied(deniedPerms)
    }

    /**
     * @see .somePermissionPermanentlyDenied
     */
    @JvmStatic
    fun somePermissionPermanentlyDenied(
        host: Fragment,
        @Size(min = 1) vararg deniedPerms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).somePermissionPermanentlyDenied(deniedPerms)
    }

    /**
     * See if some denied permission has been permanently denied.
     *
     * @param host  requesting context.
     * @param perms array of permissions.
     * @return true if the user has previously denied any of the `perms` and we should show a
     * rationale, false otherwise.
     */
    @JvmStatic
    fun somePermissionDenied(
        host: Activity,
        @Size(min = 1) vararg perms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).somePermissionDenied(perms)
    }

    /**
     * @see .somePermissionDenied
     */
    @JvmStatic
    fun somePermissionDenied(
        host: Fragment,
        @Size(min = 1) vararg perms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).somePermissionDenied(perms)
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @param host             context requesting permissions.
     * @param deniedPerms denied permission.
     * @return `true` if the permissions has been permanently denied.
     */
    @JvmStatic
    fun permissionPermanentlyDenied(
        host: Activity,
        deniedPerms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).permissionPermanentlyDenied(deniedPerms)
    }

    /**
     * @see .permissionPermanentlyDenied
     */
    @JvmStatic
    fun permissionPermanentlyDenied(
        host: Fragment,
        deniedPerms: String
    ): Boolean {
        return PermissionsHelper.newInstance(host).permissionPermanentlyDenied(deniedPerms)
    }

    // ============================================================================================
    //  Private Methods
    // ============================================================================================

    /**
     * Run permission callbacks on an object that requested permissions but already has them by
     * simulating [PackageManager.PERMISSION_GRANTED].
     *
     * @param receiver    the object requesting permissions.
     * @param requestCode the permission request code.
     * @param perms       a list of permissions requested.
     */
    private fun notifyAlreadyHasPermissions(
        receiver: Any,
        requestCode: Int,
        perms: Array<out String>
    ) {
        val grantResults = IntArray(perms.size) { PackageManager.PERMISSION_GRANTED }
        onRequestPermissionsResult(requestCode, perms, grantResults, receiver)
    }
}
