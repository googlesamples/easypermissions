package pub.devrel.easypermissions.helpers

import android.content.Context
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.dialogs.rationale.RationaleDialog
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest

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
        context?.let {
            RationaleDialog(it, permissionRequest).show()
        }
    }
}