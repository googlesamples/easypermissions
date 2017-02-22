package pub.devrel.easypermissions;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.Arrays;

/**
 * Click listener for either {@link RationaleDialogFragment} or {@link RationaleDialogFragmentCompat}.
 */
class RationaleDialogClickListener implements Dialog.OnClickListener {

    private Object mHost;
    private RationaleDialogConfig mConfig;
    private EasyPermissions.PermissionCallbacks mCallbacks;

    RationaleDialogClickListener(RationaleDialogFragmentCompat compatDialogFragment,
                                 RationaleDialogConfig config,
                                 EasyPermissions.PermissionCallbacks callbacks) {

        mHost = compatDialogFragment.getParentFragment() != null
                ? compatDialogFragment.getParentFragment()
                : compatDialogFragment.getActivity();

        mConfig = config;
        mCallbacks = callbacks;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    RationaleDialogClickListener(RationaleDialogFragment dialogFragment,
                                 RationaleDialogConfig config,
                                 EasyPermissions.PermissionCallbacks callbacks) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mHost = dialogFragment.getParentFragment() != null ?
                    dialogFragment.getParentFragment() :
                    dialogFragment.getActivity();
        } else {
            mHost = dialogFragment.getActivity();
        }

        mConfig = config;
        mCallbacks = callbacks;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == Dialog.BUTTON_POSITIVE) {
            if (mHost instanceof Fragment) {
                ((Fragment) mHost).requestPermissions(mConfig.permissions, mConfig.requestCode);
            } else if (mHost instanceof android.app.Fragment) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((android.app.Fragment) mHost)
                            .requestPermissions(mConfig.permissions, mConfig.requestCode);
                } else {
                    throw new IllegalArgumentException(
                            "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
                }
            } else if (mHost instanceof FragmentActivity) {
                ActivityCompat.requestPermissions(
                        (FragmentActivity) mHost, mConfig.permissions, mConfig.requestCode);
            }
        } else {
            notifyPermissionDenied();
        }
    }

    private void notifyPermissionDenied() {
        if (mCallbacks != null) {
            mCallbacks.onPermissionsDenied(mConfig.requestCode,
                    Arrays.asList(mConfig.permissions));
        }
    }
}
