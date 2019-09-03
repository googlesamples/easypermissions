package pub.devrel.easypermissions.helpers.base

import android.app.Activity
import android.content.Context
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

    // ============================================================================================
    // Public abstract methods
    // ============================================================================================

    abstract var context: Context?

    abstract fun directRequestPermissions(requestCode: Int, perms: Array<out String>)

    abstract fun shouldShowRequestPermissionRationale(perm: String): Boolean

    abstract fun showRequestPermissionRationale(permissionRequest: PermissionRequest)

    // ============================================================================================
    //  Public methods
    // ============================================================================================

    fun requestPermissions(permissionRequest: PermissionRequest) {
        if (shouldShowRationale(permissionRequest.perms)) {
            showRequestPermissionRationale(permissionRequest)
        } else {
            directRequestPermissions(permissionRequest.code, permissionRequest.perms)
        }
    }

    fun somePermissionPermanentlyDenied(perms: Array<out String>): Boolean {
        return perms.any { permissionPermanentlyDenied(it) }
    }

    fun permissionPermanentlyDenied(perm: String): Boolean {
        return !shouldShowRequestPermissionRationale(perm)
    }

    fun somePermissionDenied(perms: Array<out String>): Boolean {
        return shouldShowRationale(perms)
    }

    // ============================================================================================
    //  Private methods
    // ============================================================================================

    private fun shouldShowRationale(perms: Array<out String>): Boolean {
        return perms.any { shouldShowRequestPermissionRationale(it) }
    }
}
