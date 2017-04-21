package pub.devrel.easypermissions.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;

/**
 * Permissions helper for {@link Activity}.
 */
class ActivityPermissionHelper extends PermissionHelper<Activity> {

    public ActivityPermissionHelper(Activity host) {
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
            showRationaleDialogFragment(
                    getHost().getFragmentManager(),
                    rationale,
                    positiveButton,
                    negativeButton,
                    requestCode,
                    perms);
        } else {
            ActivityCompat.requestPermissions(getHost(), perms, requestCode);
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        return ActivityCompat.shouldShowRequestPermissionRationale(getHost(), perm);
    }

    @Override
    public Context getContext() {
        return getHost();
    }
}
