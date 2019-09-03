package pub.devrel.easypermissions.helpers.base

import android.app.Activity
import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.helpers.ActivityPermissionsHelper
import pub.devrel.easypermissions.helpers.AppCompatActivityPermissionsHelper
import pub.devrel.easypermissions.helpers.FragmentPermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest

/**
 * Delegate class to make permission calls based on the 'host' (Fragment, Activity, etc).
 */
abstract class PermissionsHelper<T>(val host: T) {

    abstract var context: Context?

    private fun shouldShowRationale(perms: List<String>): Boolean {
        return perms.any { shouldShowRequestPermissionRationale(it) }
    }

    fun requestPermissions(permissionRequest: PermissionRequest) {
        if (shouldShowRationale(permissionRequest.perms)) {
            showRequestPermissionRationale(permissionRequest)
        } else {
            directRequestPermissions(permissionRequest.code, permissionRequest.perms)
        }
    }

    fun somePermissionPermanentlyDenied(perms: List<String>): Boolean {
        for (deniedPermission in perms) {
            if (permissionPermanentlyDenied(deniedPermission)) {
                return true
            }
        }

        return false
    }

    fun permissionPermanentlyDenied(perm: String): Boolean {
        return !shouldShowRequestPermissionRationale(perm)
    }

    fun somePermissionDenied(perms: List<String>): Boolean {
        return shouldShowRationale(perms)
    }

    // ============================================================================
    // Public abstract methods
    // ============================================================================

    abstract fun directRequestPermissions(requestCode: Int, perms: List<String>)

    abstract fun shouldShowRequestPermissionRationale(perm: String): Boolean

    abstract fun showRequestPermissionRationale(permissionRequest: PermissionRequest)

    companion object {

        fun newInstance(host: Activity): PermissionsHelper<out Activity> {
            return (host as? AppCompatActivity)?.let {
                AppCompatActivityPermissionsHelper(it)
            } ?: ActivityPermissionsHelper(host)
        }

        fun newInstance(host: Fragment): PermissionsHelper<Fragment> {
            return FragmentPermissionsHelper(host)
        }
    }
}
