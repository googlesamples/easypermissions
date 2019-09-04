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
    @StyleRes
    var theme: Int,
    var code: Int,
    var perms: Array<out String>,
    var rationale: String?,
    var positiveButtonText: String?,
    var negativeButtonText: String?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionRequest

        if (theme != other.theme) return false
        if (code != other.code) return false
        if (!perms.contentEquals(other.perms)) return false
        if (rationale != other.rationale) return false
        if (positiveButtonText != other.positiveButtonText) return false
        if (negativeButtonText != other.negativeButtonText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = theme
        result = 31 * result + code
        result = 31 * result + perms.contentHashCode()
        result = 31 * result + (rationale?.hashCode() ?: 0)
        result = 31 * result + (positiveButtonText?.hashCode() ?: 0)
        result = 31 * result + (negativeButtonText?.hashCode() ?: 0)
        return result
    }

    /**
     * Builder to build a permission request with variable options.
     *
     * @see PermissionRequest
     */
    @Suppress("UNUSED")
    class Builder(var context: Context?) {
        @StyleRes
        private var theme = 0
        private var code = 0
        private var perms:Array<out String> = emptyArray()
        private var rationale = context?.getString(R.string.rationale_ask)
        private var positiveButtonText = context?.getString(android.R.string.ok)
        private var negativeButtonText = context?.getString(android.R.string.cancel)

        fun theme(@StyleRes theme: Int) = apply { this.theme = theme }
        fun code(code: Int) = apply { this.code = code }
        fun perms(perms: Array<out String>) = apply { this.perms = perms }
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

