package pub.devrel.easypermissions.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import pub.devrel.easypermissions.RationaleDialogFragmentCompat;

/**
 * Permissions helper for {@link Fragment} from the support library.
 */
class SupportFragmentPermissionHelper extends PermissionHelper<Fragment> {

    public SupportFragmentPermissionHelper(@NonNull Fragment host) {
        super(host);
    }

    @Override
    @SuppressLint("NewApi")
    public void requestPermissions(@NonNull String rationale,
                                   @StringRes int positiveButton,
                                   @StringRes int negativeButton,
                                   int requestCode,
                                   @NonNull String... perms) {

        if (shouldShowRationale(perms)) {
            RationaleDialogFragmentCompat
                    .newInstance(positiveButton, negativeButton, rationale, requestCode, perms)
                    .show(getHost().getChildFragmentManager(), RationaleDialogFragmentCompat.TAG);
        } else {
            getHost().requestPermissions(perms, requestCode);
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        return getHost().shouldShowRequestPermissionRationale(perm);
    }

    @Override
    public Context getContext() {
        return getHost().getActivity();
    }
}
