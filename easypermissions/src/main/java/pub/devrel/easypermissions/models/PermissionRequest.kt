package pub.devrel.easypermissions.models

import android.content.Context
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import pub.devrel.easypermissions.R

/**
 * An immutable model object that holds all of the parameters associated with a permission request,
 * such as the permissions, request code, and rationale.
 *
 * @see PermissionRequest.Builder
 */
data class PermissionRequest(
    @get:StyleRes
    var theme: Int,
    var code: Int,
    var perms: List<String>,
    var rationale: String,
    var positiveButtonText: String,
    var negativeButtonText: String
) {

    /**
     * Builder to build a permission request with variable options.
     *
     * @see PermissionRequest
     */
    class Builder(var context: Context) {
        private var theme = -1
        private var code = -1
        private var perms = emptyList<String>()
        private var rationale = context.getString(R.string.rationale_ask)
        private var positiveButtonText = context.getString(android.R.string.ok)
        private var negativeButtonText = context.getString(android.R.string.cancel)

        fun theme(@StyleRes theme: Int) = apply { this.theme = theme }
        fun code(code: Int) = apply { this.code = code }
        fun perms(perms: List<String>) = apply { this.perms = perms }
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

        fun build(): PermissionRequest {
            return PermissionRequest(
                theme = theme,
                code = code,
                perms = perms,
                rationale = rationale,
                positiveButtonText = positiveButtonText,
                negativeButtonText = negativeButtonText
            )
        }
    }
}

