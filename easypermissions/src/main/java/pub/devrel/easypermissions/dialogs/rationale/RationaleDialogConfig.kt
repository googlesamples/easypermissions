package pub.devrel.easypermissions.dialogs.rationale

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import pub.devrel.easypermissions.models.PermissionRequest
import java.util.ArrayList

private const val KEY_POSITIVE_BUTTON = "positiveButton"
private const val KEY_NEGATIVE_BUTTON = "negativeButton"
private const val KEY_RATIONALE_MESSAGE = "rationaleMsg"
private const val KEY_THEME = "theme"
private const val KEY_REQUEST_CODE = "requestCode"
private const val KEY_PERMISSIONS = "permissions"

/**
 * Configuration for either [RationaleDialogFragment] or [RationaleDialogFragmentCompat].
 */
internal class RationaleDialogConfig {

    var positiveButton: String? = null
    var negativeButton: String? = null
    var theme: Int = 0
    var requestCode: Int = 0
    var rationaleMsg: String? = null
    var permissions: List<String>? = null

    constructor(permissionRequest: PermissionRequest) {
        this.positiveButton = permissionRequest.positiveButtonText
        this.negativeButton = permissionRequest.negativeButtonText
        this.rationaleMsg = permissionRequest.rationale
        this.theme = permissionRequest.theme
        this.requestCode = permissionRequest.code
        this.permissions = permissionRequest.perms
    }

    constructor(bundle: Bundle) {
        positiveButton = bundle.getString(KEY_POSITIVE_BUTTON)
        negativeButton = bundle.getString(KEY_NEGATIVE_BUTTON)
        rationaleMsg = bundle.getString(KEY_RATIONALE_MESSAGE)
        theme = bundle.getInt(KEY_THEME)
        requestCode = bundle.getInt(KEY_REQUEST_CODE)
        permissions = bundle.getStringArrayList(KEY_PERMISSIONS)
    }

    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(KEY_POSITIVE_BUTTON, positiveButton)
        bundle.putString(KEY_NEGATIVE_BUTTON, negativeButton)
        bundle.putString(KEY_RATIONALE_MESSAGE, rationaleMsg)
        bundle.putInt(KEY_THEME, theme)
        bundle.putInt(KEY_REQUEST_CODE, requestCode)
        bundle.putStringArrayList(KEY_PERMISSIONS, permissions as ArrayList<String>?)

        return bundle
    }

    fun createSupportDialog(context: Context, listener: DialogInterface.OnClickListener): AlertDialog {
        val builder: AlertDialog.Builder
        if (theme > 0) {
            builder = AlertDialog.Builder(context, theme)
        } else {
            builder = AlertDialog.Builder(context)
        }
        return builder
            .setCancelable(false)
            .setPositiveButton(positiveButton, listener)
            .setNegativeButton(negativeButton, listener)
            .setMessage(rationaleMsg)
            .create()
    }

    fun createFrameworkDialog(
        context: Context,
        listener: DialogInterface.OnClickListener
    ): android.app.AlertDialog {
        val builder: android.app.AlertDialog.Builder
        if (theme > 0) {
            builder = android.app.AlertDialog.Builder(context, theme)
        } else {
            builder = android.app.AlertDialog.Builder(context)
        }
        return builder
            .setCancelable(false)
            .setPositiveButton(positiveButton, listener)
            .setNegativeButton(negativeButton, listener)
            .setMessage(rationaleMsg)
            .create()
    }
}