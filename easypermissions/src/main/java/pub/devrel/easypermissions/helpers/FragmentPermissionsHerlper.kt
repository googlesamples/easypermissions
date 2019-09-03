package pub.devrel.easypermissions.helpers

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.dialogs.rationale.RationaleDialogFragmentCompat
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest

private const val TAG = "AppCompatActivityPH"

/**
 * Permissions helper for [Fragment].
 */
internal class FragmentPermissionsHelper(
    host: Fragment
) : PermissionsHelper<Fragment>(host) {

    override var context: Context? = host.activity

    override fun directRequestPermissions(requestCode: Int, perms: Array<out String>) {
        host.requestPermissions(perms, requestCode)
    }

    override fun shouldShowRequestPermissionRationale(perm: String): Boolean {
        return host.shouldShowRequestPermissionRationale(perm)
    }

    override fun showRequestPermissionRationale(permissionRequest: PermissionRequest) {
        val fm = host.childFragmentManager

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