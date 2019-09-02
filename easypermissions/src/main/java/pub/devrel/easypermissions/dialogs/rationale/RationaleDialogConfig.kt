package pub.devrel.easypermissions.dialogs.rationale

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog

/**
 * Configuration for either [RationaleDialogFragment] or [RationaleDialogFragmentCompat].
 */
internal class RationaleDialogConfig {

    var positiveButton: String? = null
    var negativeButton: String? = null
    var theme: Int = 0
    var requestCode: Int = 0
    var rationaleMsg: String? = null
    var permissions: Array<String>? = null

    constructor(
        positiveButton: String,
        negativeButton: String,
        rationaleMsg: String,
        @StyleRes theme: Int,
        requestCode: Int,
        permissions: Array<String>
    ) {

        this.positiveButton = positiveButton
        this.negativeButton = negativeButton
        this.rationaleMsg = rationaleMsg
        this.theme = theme
        this.requestCode = requestCode
        this.permissions = permissions
    }

    constructor(bundle: Bundle) {
        positiveButton = bundle.getString(KEY_POSITIVE_BUTTON)
        negativeButton = bundle.getString(KEY_NEGATIVE_BUTTON)
        rationaleMsg = bundle.getString(KEY_RATIONALE_MESSAGE)
        theme = bundle.getInt(KEY_THEME)
        requestCode = bundle.getInt(KEY_REQUEST_CODE)
        permissions = bundle.getStringArray(KEY_PERMISSIONS)
    }

    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(KEY_POSITIVE_BUTTON, positiveButton)
        bundle.putString(KEY_NEGATIVE_BUTTON, negativeButton)
        bundle.putString(KEY_RATIONALE_MESSAGE, rationaleMsg)
        bundle.putInt(KEY_THEME, theme)
        bundle.putInt(KEY_REQUEST_CODE, requestCode)
        bundle.putStringArray(KEY_PERMISSIONS, permissions)

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

    companion object {

        private val KEY_POSITIVE_BUTTON = "positiveButton"
        private val KEY_NEGATIVE_BUTTON = "negativeButton"
        private val KEY_RATIONALE_MESSAGE = "rationaleMsg"
        private val KEY_THEME = "theme"
        private val KEY_REQUEST_CODE = "requestCode"
        private val KEY_PERMISSIONS = "permissions"
    }

}