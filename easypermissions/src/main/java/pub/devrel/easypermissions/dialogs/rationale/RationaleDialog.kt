package pub.devrel.easypermissions.dialogs.rationale

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.facade.EasyPermissions
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import pub.devrel.easypermissions.models.PermissionRequest

/**
 * Dialog to prompt the user to go to the app's settings screen and enable permissions. If the user
 * clicks 'OK' on the dialog, they are sent to the settings screen. The result is returned to the
 * Activity via [Activity.onActivityResult].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class RationaleDialog(
    private val context: Context,
    private val model: PermissionRequest
): DialogInterface.OnClickListener {

    private var dialog: AlertDialog? = null
    private val permissionCallbacks: EasyPermissions.PermissionCallbacks?
        get() = if(context is EasyPermissions.PermissionCallbacks) context else null
    private val rationaleCallbacks: EasyPermissions.RationaleCallbacks?
        get() = if(context is EasyPermissions.RationaleCallbacks) context else null

    /**
     * Display the dialog.
     */
    fun show() {
        dialog = AlertDialog.Builder(context, model.theme)
            .setCancelable(false)
            .setMessage(model.rationale)
            .setPositiveButton(model.positiveButtonText, this)
            .setNegativeButton(model.negativeButtonText, this)
            .show()
    }

    override fun onClick(dialogInterface: DialogInterface?, buttonType: Int) {
        when(buttonType) {
            Dialog.BUTTON_POSITIVE -> {
                rationaleCallbacks?.onRationaleAccepted(model.code)
                when(context) {
                    is Fragment ->
                        PermissionsHelper
                        .newInstance(context)
                        .directRequestPermissions(model.code, model.perms)
                    is Activity ->
                        PermissionsHelper
                            .newInstance(context)
                            .directRequestPermissions(model.code, model.perms)
                }
            }
            Dialog.BUTTON_NEGATIVE, Dialog.BUTTON_NEUTRAL -> {
                rationaleCallbacks?.onRationaleDenied(model.code)
                permissionCallbacks?.onPermissionsDenied(model.code, model.perms.toList())
            }
        }
    }
}