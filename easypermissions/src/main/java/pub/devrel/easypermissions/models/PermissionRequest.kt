package pub.devrel.easypermissions.models

import android.app.Activity
import androidx.annotation.RestrictTo
import androidx.annotation.Size
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.R
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import java.util.*

/**
 * An immutable model object that holds all of the parameters associated with a permission request,
 * such as the permissions, request code, and rationale.
 *
 * @see EasyPermissions.requestPermissions
 * @see PermissionRequest.Builder
 */
class PermissionRequest private constructor(
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val helper: PermissionsHelper<*>,
    perms: Array<String>,
    val requestCode: Int,
    val rationale: String,
    val positiveButtonText: String,
    val negativeButtonText: String,
    @get:StyleRes
    val theme: Int
) {
    private val mPerms: Array<String>

    val perms: Array<String>
        get() = mPerms.clone()

    init {
        mPerms = perms.clone()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val request = o as PermissionRequest?

        return Arrays.equals(mPerms, request!!.mPerms) && requestCode == request.requestCode
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(mPerms)
        result = 31 * result + requestCode
        return result
    }

    override fun toString(): String {
        return "PermissionRequest{" +
                "mHelper=" + helper +
                ", mPerms=" + Arrays.toString(mPerms) +
                ", mRequestCode=" + requestCode +
                ", mRationale='" + rationale + '\''.toString() +
                ", mPositiveButtonText='" + positiveButtonText + '\''.toString() +
                ", mNegativeButtonText='" + negativeButtonText + '\''.toString() +
                ", mTheme=" + theme +
                '}'.toString()
    }

    /**
     * Builder to build a permission request with variable options.
     *
     * @see PermissionRequest
     */
    class Builder {
        private val mHelper: PermissionsHelper<*>
        private val mRequestCode: Int
        private val mPerms: Array<String>

        private var mRationale: String? = null
        private var mPositiveButtonText: String? = null
        private var mNegativeButtonText: String? = null
        private var mTheme = -1

        /**
         * Construct a new permission request builder with a host, request code, and the requested
         * permissions.
         *
         * @param activity    the permission request host
         * @param requestCode request code to track this request; must be &lt; 256
         * @param perms       the set of permissions to be requested
         */
        constructor(
            activity: Activity, requestCode: Int,
            @Size(min = 1) vararg perms: String
        ) {
            mHelper = PermissionsHelper.newInstance(activity)
            mRequestCode = requestCode
            mPerms = perms
        }

        /**
         * @see .Builder
         */
        constructor(
            fragment: Fragment, requestCode: Int,
            @Size(min = 1) vararg perms: String
        ) {
            mHelper = PermissionsHelper.newInstance(fragment)
            mRequestCode = requestCode
            mPerms = perms
        }

        /**
         * Set the rationale to display to the user if they don't allow your permissions on the
         * first try. This rationale will be shown as long as the user has denied your permissions
         * at least once, but has not yet permanently denied your permissions. Should the user
         * permanently deny your permissions, use the [AppSettingsDialog] instead.
         *
         *
         * The default rationale text is [R.string.rationale_ask].
         *
         * @param rationale the rationale to be displayed to the user should they deny your
         * permission at least once
         */
        fun setRationale(rationale: String?): Builder {
            mRationale = rationale
            return this
        }

        /**
         * @param resId the string resource to be used as a rationale
         * @see .setRationale
         */
        fun setRationale(@StringRes resId: Int): Builder {
            mRationale = mHelper.context.getString(resId)
            return this
        }

        /**
         * Set the positive button text for the rationale dialog should it be shown.
         *
         *
         * The default is [android.R.string.ok]
         */
        fun setPositiveButtonText(positiveButtonText: String?): Builder {
            mPositiveButtonText = positiveButtonText
            return this
        }

        /**
         * @see .setPositiveButtonText
         */
        fun setPositiveButtonText(@StringRes resId: Int): Builder {
            mPositiveButtonText = mHelper.context.getString(resId)
            return this
        }

        /**
         * Set the negative button text for the rationale dialog should it be shown.
         *
         *
         * The default is [android.R.string.cancel]
         */
        fun setNegativeButtonText(negativeButtonText: String?): Builder {
            mNegativeButtonText = negativeButtonText
            return this
        }

        /**
         * @see .setNegativeButtonText
         */
        fun setNegativeButtonText(@StringRes resId: Int): Builder {
            mNegativeButtonText = mHelper.context.getString(resId)
            return this
        }

        /**
         * Set the theme to be used for the rationale dialog should it be shown.
         *
         * @param theme a style resource
         */
        fun setTheme(@StyleRes theme: Int): Builder {
            mTheme = theme
            return this
        }

        /**
         * Build the permission request.
         *
         * @return the permission request
         * @see EasyPermissions.requestPermissions
         * @see PermissionRequest
         */
        fun build(): PermissionRequest {
            if (mRationale == null) {
                mRationale = mHelper.context.getString(R.string.rationale_ask)
            }
            if (mPositiveButtonText == null) {
                mPositiveButtonText = mHelper.context.getString(android.R.string.ok)
            }
            if (mNegativeButtonText == null) {
                mNegativeButtonText = mHelper.context.getString(android.R.string.cancel)
            }

            return PermissionRequest(
                mHelper,
                mPerms,
                mRequestCode,
                mRationale,
                mPositiveButtonText,
                mNegativeButtonText,
                mTheme
            )
        }
    }
}