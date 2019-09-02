package pub.devrel.easypermissions.helpers.base

import android.app.Activity
import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.helpers.ActivityPermissionsHelper
import pub.devrel.easypermissions.helpers.AppCompatActivityPermissionsHelper
import pub.devrel.easypermissions.helpers.FragmentPermissionsHelper

/**
 * Delegate class to make permission calls based on the 'host' (Fragment, Activity, etc).
 */
abstract class BasePermissionsHelper<T>(val host: T) {

    abstract fun getContext(): Context?

    private fun shouldShowRationale(vararg perms: String): Boolean {
        for (perm in perms) {
            if (shouldShowRequestPermissionRationale(perm)) {
                return true
            }
        }
        return false
    }

    fun requestPermissions(
        rationale: String,
        positiveButton: String,
        negativeButton: String,
        @StyleRes theme: Int,
        requestCode: Int,
        vararg perms: String
    ) {
        if (shouldShowRationale(*perms)) {
            showRequestPermissionRationale(
                rationale, positiveButton, negativeButton, theme, requestCode, *perms
            )
        } else {
            directRequestPermissions(requestCode, *perms)
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

    fun permissionPermanentlyDenied(perms: String): Boolean {
        return !shouldShowRequestPermissionRationale(perms)
    }

    fun somePermissionDenied(vararg perms: String): Boolean {
        return shouldShowRationale(*perms)
    }

    // ============================================================================
    // Public abstract methods
    // ============================================================================

    abstract fun directRequestPermissions(requestCode: Int, vararg perms: String)

    abstract fun shouldShowRequestPermissionRationale(perm: String): Boolean

    abstract fun showRequestPermissionRationale(
        rationale: String,
        positiveButton: String,
        negativeButton: String,
        @StyleRes theme: Int,
        requestCode: Int,
        vararg perms: String
    )

    companion object {

        fun newInstance(host: Activity): BasePermissionsHelper<out Activity> {
            return (host as? AppCompatActivity)?.let {
                AppCompatActivityPermissionsHelper(it)
            } ?: ActivityPermissionsHelper(host)
        }

        fun newInstance(host: Fragment): BasePermissionsHelper<Fragment> {
            return FragmentPermissionsHelper(host)
        }
    }
}
