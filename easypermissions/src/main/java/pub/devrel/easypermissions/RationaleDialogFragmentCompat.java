package pub.devrel.easypermissions;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * {@link AppCompatDialogFragment} to display rationale for permission requests when the request
 * comes from a Fragment or Activity that can host a Fragment.
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class RationaleDialogFragmentCompat extends AppCompatDialogFragment {

    private EasyPermissions.PermissionCallbacks mPermissionCallbacks;
    private EasyPermissions.PermissionCallback mPermissionCallback;

    static RationaleDialogFragmentCompat newInstance(
            @StringRes int positiveButton, @StringRes int negativeButton,
            @NonNull String rationaleMsg, int requestCode, @NonNull String[] permissions) {

        // Create new Fragment
        RationaleDialogFragmentCompat dialogFragment = new RationaleDialogFragmentCompat();

        // Initialize configuration as arguments
        RationaleDialogConfig config = new RationaleDialogConfig(
                positiveButton, negativeButton, rationaleMsg, requestCode, permissions);
        dialogFragment.setArguments(config.toBundle());

        return dialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            if (getParentFragment() instanceof EasyPermissions.PermissionCallbacks) {
                mPermissionCallbacks = (EasyPermissions.PermissionCallbacks) getParentFragment();
            } else if (getParentFragment() instanceof EasyPermissions.PermissionCallback) {
                mPermissionCallback = (EasyPermissions.PermissionCallback) getParentFragment();
            }
        } else if (context instanceof EasyPermissions.PermissionCallbacks) {
            mPermissionCallbacks = (EasyPermissions.PermissionCallbacks) context;
        } else if (context instanceof EasyPermissions.PermissionCallback) {
            mPermissionCallback = (EasyPermissions.PermissionCallback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPermissionCallbacks = null;
        mPermissionCallback = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Rationale dialog should not be cancelable
        setCancelable(false);

        // Get config from arguments, create click listener
        RationaleDialogConfig config = new RationaleDialogConfig(getArguments());
        RationaleDialogClickListener clickListener =
                new RationaleDialogClickListener(this, config, mPermissionCallbacks);

        if (mPermissionCallbacks == null && mPermissionCallback != null) {
            clickListener = new RationaleDialogClickListener(this, config, mPermissionCallbacks);
        } else if (mPermissionCallback != null) {
            clickListener = new RationaleDialogClickListener(this, config, mPermissionCallback);
        }

        // Create an AlertDialog
        return config.createDialog(getContext(), clickListener);
    }
}
