package pub.devrel.easypermissions.helper;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Permissions helper for {@link Fragment} from the framework.
 */
class FrameworkFragmentPermissionHelper extends PermissionHelper<Fragment> {

    public FrameworkFragmentPermissionHelper(@NonNull Fragment host) {
        super(host);
    }

    @Override
    @SuppressLint("NewApi")
    public void requestPermissions(@NonNull String rationale,
                                   @StringRes int positiveButton,
                                   @StringRes int negativeButton, int requestCode,
                                   @NonNull String... perms) {

        if (shouldShowRationale(perms)) {
            showRationaleDialogFragment(
                    getHost().getChildFragmentManager(),
                    rationale,
                    positiveButton,
                    negativeButton,
                    requestCode,
                    perms);
        } else {
            getHost().requestPermissions(perms, requestCode);
        }
    }

    @Override
    @SuppressLint("NewApi")
    public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        return getHost().shouldShowRequestPermissionRationale(perm);
    }

    @Override
    @SuppressLint("NewApi")
    public Context getContext() {
        return getHost().getActivity();
    }
}
