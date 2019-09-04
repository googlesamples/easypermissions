package pub.devrel.easypermissions.dialogs.settings

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

/**
 * Dialog to prompt the user to go to the app's settings screen and enable permissions. If the user
 * clicks 'OK' on the dialog, they are sent to the settings screen. The result is returned to the
 * Activity via [Activity.onActivityResult].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SettingsDialog(
    val context: Context,
    val model: SettingsDialogModel
): DialogInterface.OnClickListener {

    private var dialog: AlertDialog? = null

    /**
     * Display the dialog.
     */
    fun show() {
        dialog = AlertDialog.Builder(context, model.theme)
            .setCancelable(false)
            .setTitle(model.title)
            .setMessage(model.rationale)
            .setPositiveButton(model.positiveButtonText, this)
            .setNegativeButton(model.negativeButtonText, this)
            .show()
    }

    override fun onClick(dialogInterface: DialogInterface?, buttonType: Int) {
        when (buttonType) {
            Dialog.BUTTON_POSITIVE -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = if(model.openOnNewTask) Intent.FLAG_ACTIVITY_NEW_TASK else 0
                }
                when (context) {
                    is Activity -> context.startActivityForResult(intent, model.code)
                    is Fragment -> context.startActivityForResult(intent, model.code)
                }
            }
            Dialog.BUTTON_NEGATIVE, Dialog.BUTTON_NEUTRAL -> {
                when (context) {
                    is Activity -> context.setResult(Activity.RESULT_CANCELED)
                    is Fragment -> context.activity?.setResult(Activity.RESULT_CANCELED)
                }
                dialog?.dismiss()
            }
        }
    }
}
