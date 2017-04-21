package pub.devrel.easypermissions.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Permissions helper for apps built against API < 23, which do not need runtime permissions.
 */
class LowApiPermissionsHelper extends PermissionHelper<Object> {

    public LowApiPermissionsHelper(@NonNull Object host) {
        super(host);
    }

    @Override
    public void requestPermissions(@NonNull String rationale,
                                   @StringRes int positiveButton,
                                   @StringRes int negativeButton,
                                   int requestCode,
                                   @NonNull String... perms) {

        throw new IllegalStateException("Should never be requesting permissions on API < 23!");
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        return false;
    }

    @Override
    public Context getContext() {
        return null;
    }
}
