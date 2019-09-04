package pub.devrel.easypermissions.helpers

import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import pub.devrel.easypermissions.dialogs.rationale.RationaleDialog
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest

private const val TAG = "ActivityPH"

/**
 * Permissions helper for [Activity].
 */
internal class ActivityPermissionsHelper(
    host: Activity
) : PermissionsHelper<Activity>(host) {

    override var context: Context? = host

    override fun directRequestPermissions(requestCode: Int, perms: Array<out String>) {
        ActivityCompat.requestPermissions(host, perms, requestCode)
    }

    override fun shouldShowRequestPermissionRationale(perm: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(host, perm)
    }

    override fun showRequestPermissionRationale(permissionRequest: PermissionRequest) {
        RationaleDialog(host, permissionRequest).show()
    }
}
