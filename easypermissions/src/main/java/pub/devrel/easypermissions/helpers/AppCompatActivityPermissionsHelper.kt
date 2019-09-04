package pub.devrel.easypermissions.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import pub.devrel.easypermissions.dialogs.rationale.RationaleDialog
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest

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
        RationaleDialog(host, permissionRequest).show()
    }
}