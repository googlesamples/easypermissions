package pub.devrel.easypermissions.helpers

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import pub.devrel.easypermissions.dialogs.rationale.RationaleDialogFragmentCompat
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest

private const val TAG = "AppCompatActivityPH"

/**
 * Permissions helper for [AppCompatActivity].
 */
internal class AppCompatActivityPermissionsHelper(
    host: AppCompatActivity
) : PermissionsHelper<AppCompatActivity>(host) {

    override var context: Context? = host

    override fun directRequestPermissions(requestCode: Int, perms: Array<out String>) {
        ActivityCompat.requestPermissions(host, perms, requestCode)
    }

    override fun shouldShowRequestPermissionRationale(perm: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(host, perm)
    }

    override fun showRequestPermissionRationale(permissionRequest: PermissionRequest) {
        val fm = host.supportFragmentManager

        // Check if fragment is already showing
        val fragment = fm.findFragmentByTag(RationaleDialogFragmentCompat.TAG)
        if (fragment is RationaleDialogFragmentCompat) {
            Log.d(TAG, "Found existing fragment, not showing rationale.")
            return
        }

        RationaleDialogFragmentCompat
            .newInstance(permissionRequest)
            .showAllowingStateLoss(fm, RationaleDialogFragmentCompat.TAG)
    }
}