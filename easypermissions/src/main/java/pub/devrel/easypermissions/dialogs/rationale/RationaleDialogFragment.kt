package pub.devrel.easypermissions.dialogs.rationale

import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.annotation.StyleRes
import pub.devrel.easypermissions.facade.EasyPermissions

/**
 * [DialogFragment] to display rationale for permission requests when the request comes from
 * a Fragment or Activity that can host a Fragment.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class RationaleDialogFragment : DialogFragment() {

    private var mPermissionCallbacks: EasyPermissions.PermissionCallbacks? = null
    private var mRationaleCallbacks: EasyPermissions.RationaleCallbacks? = null
    private var mStateSaved = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && parentFragment != null) {
            if (parentFragment is EasyPermissions.PermissionCallbacks) {
                mPermissionCallbacks = parentFragment as EasyPermissions.PermissionCallbacks
            }
            if (parentFragment is EasyPermissions.RationaleCallbacks) {
                mRationaleCallbacks = parentFragment as EasyPermissions.RationaleCallbacks
            }

        }

        if (context is EasyPermissions.PermissionCallbacks) {
            mPermissionCallbacks = context
        }

        if (context is EasyPermissions.RationaleCallbacks) {
            mRationaleCallbacks = context
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mStateSaved = true
        super.onSaveInstanceState(outState)
    }

    /**
     * Version of [.show] that no-ops when an IllegalStateException
     * would otherwise occur.
     */
    fun showAllowingStateLoss(manager: FragmentManager, tag: String) {
        // API 26 added this convenient method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.isStateSaved) {
                return
            }
        }

        if (mStateSaved) {
            return
        }

        show(manager, tag)
    }

    override fun onDetach() {
        super.onDetach()
        mPermissionCallbacks = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        // Rationale dialog should not be cancelable
        isCancelable = false

        // Get config from arguments, create click listener
        val config = RationaleDialogConfig(arguments)
        val clickListener =
            RationaleDialogClickListener(this, config, mPermissionCallbacks, mRationaleCallbacks)

        // Create an AlertDialog
        return config.createFrameworkDialog(activity, clickListener)
    }

    companion object {

        val TAG = "RationaleDialogFragment"

        fun newInstance(
            positiveButton: String,
            negativeButton: String,
            rationaleMsg: String,
            @StyleRes theme: Int,
            requestCode: Int,
            permissions: Array<String>
        ): RationaleDialogFragment {

            // Create new Fragment
            val dialogFragment = RationaleDialogFragment()

            // Initialize configuration as arguments
            val config = RationaleDialogConfig(
                positiveButton, negativeButton, rationaleMsg, theme, requestCode, permissions
            )
            dialogFragment.arguments = config.toBundle()

            return dialogFragment
        }
    }

}