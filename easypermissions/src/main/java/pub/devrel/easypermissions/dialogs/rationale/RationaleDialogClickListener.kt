package pub.devrel.easypermissions.dialogs.rationale

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.facade.EasyPermissions
import pub.devrel.easypermissions.helpers.base.PermissionsHelper
import java.util.*

/**
 * Click listener for either [RationaleDialogFragment] or [RationaleDialogFragmentCompat].
 */
internal class RationaleDialogClickListener : DialogInterface.OnClickListener {

    private var mHost: Any? = null
    private var mConfig: RationaleDialogConfig? = null
    private var mCallbacks: EasyPermissions.PermissionCallbacks? = null
    private var mRationaleCallbacks: EasyPermissions.RationaleCallbacks? = null

    constructor(
        compatDialogFragment: RationaleDialogFragmentCompat,
        config: RationaleDialogConfig,
        callbacks: EasyPermissions.PermissionCallbacks,
        rationaleCallbacks: EasyPermissions.RationaleCallbacks
    ) {

        mHost = if (compatDialogFragment.parentFragment != null)
            compatDialogFragment.parentFragment
        else
            compatDialogFragment.activity

        mConfig = config
        mCallbacks = callbacks
        mRationaleCallbacks = rationaleCallbacks

    }

    constructor(
        dialogFragment: RationaleDialogFragment,
        config: RationaleDialogConfig,
        callbacks: EasyPermissions.PermissionCallbacks,
        dialogCallback: EasyPermissions.RationaleCallbacks
    ) {

        mHost = dialogFragment.activity

        mConfig = config
        mCallbacks = callbacks
        mRationaleCallbacks = dialogCallback
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val requestCode = mConfig!!.requestCode
        if (which == Dialog.BUTTON_POSITIVE) {
            val permissions = mConfig!!.permissions
            if (mRationaleCallbacks != null) {
                mRationaleCallbacks!!.onRationaleAccepted(requestCode)
            }
            if (mHost is Fragment) {
                PermissionsHelper.newInstance(mHost as Fragment?)
                    .directRequestPermissions(requestCode, *permissions)
            } else if (mHost is Activity) {
                PermissionsHelper.newInstance(mHost as Activity?)
                    .directRequestPermissions(requestCode, *permissions)
            } else {
                throw RuntimeException("Host must be an Activity or Fragment!")
            }
        } else {
            if (mRationaleCallbacks != null) {
                mRationaleCallbacks!!.onRationaleDenied(requestCode)
            }
            notifyPermissionDenied()
        }
    }

    private fun notifyPermissionDenied() {
        if (mCallbacks != null) {
            mCallbacks!!.onPermissionsDenied(
                mConfig!!.requestCode,
                Arrays.asList(*mConfig!!.permissions)
            )
        }
    }
}
