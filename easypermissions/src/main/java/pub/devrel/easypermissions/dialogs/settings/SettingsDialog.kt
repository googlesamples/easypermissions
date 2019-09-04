/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easypermissions.dialogs.settings

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.R

const val DEFAULT_SETTINGS_REQ_CODE = 16061

/**
 * Dialog to prompt the user to go to the app's settings screen and enable permissions. If the user
 * clicks 'OK' on the dialog, they are sent to the settings screen. The result is returned to the
 * Activity via [Activity.onActivityResult].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SettingsDialog(
    private val context: Context,
    @StyleRes
    private var theme: Int,
    private var requestCode: Int,
    private var openOnNewTask: Boolean,
    private var title: String?,
    private var rationale: String?,
    private var positiveButtonText: String?,
    private var negativeButtonText: String?
) : DialogInterface.OnClickListener {
    private var dialog: AlertDialog? = null

    /**
     * Display the dialog.
     */
    fun show() {
        dialog = AlertDialog.Builder(context, theme)
            .setCancelable(false)
            .setTitle(title)
            .setMessage(rationale)
            .setPositiveButton(positiveButtonText, this)
            .setNegativeButton(negativeButtonText, this)
            .show()
    }

    override fun onClick(dialogInterface: DialogInterface?, buttonType: Int) {
        when (buttonType) {
            Dialog.BUTTON_POSITIVE -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = if (openOnNewTask) Intent.FLAG_ACTIVITY_NEW_TASK else 0
                }
                when (context) {
                    is Activity -> context.startActivityForResult(intent, requestCode)
                    is Fragment -> context.startActivityForResult(intent, requestCode)
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

    /**
     * Builder for an [SettingsDialog].
     */
    @Suppress("UNUSED")
    class Builder(private var context: Context) {
        @StyleRes
        private var theme = 0
        private var requestCode = DEFAULT_SETTINGS_REQ_CODE
        private var openOnNewTask = false
        private var title = context.getString(R.string.title_settings_dialog)
        private var rationale = context.getString(R.string.rationale_ask_again)
        private var positiveButtonText = context.getString(android.R.string.ok)
        private var negativeButtonText = context.getString(android.R.string.cancel)

        fun theme(@StyleRes theme: Int) = apply { this.theme = theme }
        fun requestCode(requestCode: Int) = apply { this.requestCode = requestCode }
        fun openOnNewTask(openOnNewTask: Boolean) = apply { this.openOnNewTask = openOnNewTask }
        fun title(title: String) = apply { this.title = title }
        fun title(@StringRes resId: Int) = apply { this.title = context.getString(resId) }
        fun rationale(rationale: String) = apply { this.rationale = rationale }
        fun rationale(@StringRes resId: Int) = apply { this.rationale = context.getString(resId) }
        fun positiveButtonText(positiveButtonText: String) =
            apply { this.positiveButtonText = positiveButtonText }

        fun positiveButtonText(@StringRes resId: Int) =
            apply { this.positiveButtonText = context.getString(resId) }

        fun negativeButtonText(negativeButtonText: String) =
            apply { this.negativeButtonText = negativeButtonText }

        fun negativeButtonText(@StringRes resId: Int) =
            apply { this.negativeButtonText = context.getString(resId) }

        fun build(): SettingsDialog {
            return SettingsDialog(
                context = context,
                requestCode = requestCode,
                openOnNewTask = openOnNewTask,
                theme = theme,
                title = title,
                rationale = rationale,
                positiveButtonText = positiveButtonText,
                negativeButtonText = negativeButtonText
            )
        }
    }
}
