package pub.devrel.easypermissions.helpers

import android.content.Context
import android.util.Log
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.RationaleDialogFragmentCompat
import pub.devrel.easypermissions.helpers.base.BasePermissionsHelper

private const val TAG = "AppCompatActivityPH"

/**
 * Permissions helper for [Fragment].
 */
internal class FragmentPermissionsHelper(
    host: Fragment
) : BasePermissionsHelper<Fragment>(host) {

    override fun directRequestPermissions(requestCode: Int, vararg perms: String) {
        host.requestPermissions(perms, requestCode)
    }

    override fun shouldShowRequestPermissionRationale(perm: String): Boolean {
        return host.shouldShowRequestPermissionRationale(perm)
    }

    override fun getContext(): Context? {
        return host.activity
    }

    override fun showRequestPermissionRationale(
        rationale: String,
        positiveButton: String,
        negativeButton: String,
        @StyleRes theme: Int,
        requestCode: Int,
        vararg perms: String
    ) {
        val fm = host.childFragmentManager

        // Check if fragment is already showing
        val fragment = fm.findFragmentByTag(RationaleDialogFragmentCompat.TAG)
        if (fragment is RationaleDialogFragmentCompat) {
            Log.d(TAG, "Found existing fragment, not showing rationale.")
            return
        }

        RationaleDialogFragmentCompat
            .newInstance(rationale, positiveButton, negativeButton, theme, requestCode, perms)
            .showAllowingStateLoss(fm, RationaleDialogFragmentCompat.TAG)
    }
}