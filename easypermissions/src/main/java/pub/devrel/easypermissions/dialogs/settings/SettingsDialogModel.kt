package pub.devrel.easypermissions.dialogs.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import pub.devrel.easypermissions.R

const val DEFAULT_SETTINGS_REQ_CODE = 16061

data class SettingsDialogModel(
    @StyleRes
    var theme: Int,
    var code: Int,
    var openOnNewTask: Boolean,
    var title: String?,
    var rationale: String?,
    var positiveButtonText: String?,
    var negativeButtonText: String?
) {

    /**
     * Builder for an [SettingsDialogModel].
     */
    class Builder(var context: Context?) {
        @StyleRes
        private var theme = 0
        private var code = DEFAULT_SETTINGS_REQ_CODE
        private var openOnNewTask = false
        private var title = context?.getString(R.string.title_settings_dialog)
        private var rationale = context?.getString(R.string.rationale_ask_again)
        private var positiveButtonText = context?.getString(android.R.string.ok)
        private var negativeButtonText = context?.getString(android.R.string.cancel)

        fun theme(@StyleRes theme: Int) = apply { this.theme = theme }
        fun code(code: Int) = apply { this.code = code }
        fun openOnNewTask(openOnNewTask: Boolean) = apply { this.openOnNewTask = openOnNewTask }
        fun title(title: String) = apply { this.title = title }
        fun title(@StringRes resId: Int) = apply { this.title = context?.getString(resId) }
        fun rationale(rationale: String) = apply { this.rationale = rationale }
        fun rationale(@StringRes resId: Int) = apply { this.rationale = context?.getString(resId) }
        fun positiveButtonText(positiveButtonText: String) =
            apply { this.positiveButtonText = positiveButtonText }

        fun positiveButtonText(@StringRes resId: Int) =
            apply { this.positiveButtonText = context?.getString(resId) }

        fun negativeButtonText(negativeButtonText: String) =
            apply { this.negativeButtonText = negativeButtonText }

        fun negativeButtonText(@StringRes resId: Int) =
            apply { this.negativeButtonText = context?.getString(resId) }

        fun build(): SettingsDialogModel {
            return SettingsDialogModel(
                theme = theme,
                code = code,
                openOnNewTask = openOnNewTask,
                title = title,
                rationale = rationale,
                positiveButtonText = positiveButtonText,
                negativeButtonText = negativeButtonText
            )
        }
    }
}