package pub.devrel.easypermissions.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Permissions helper for apps built against API < 23, which do not need runtime permissions.
 */
class LowApiPermissionsHelper extends PermissionHelper<Object> {

    public LowApiPermissionsHelper(@NonNull Object host) {
        super(host);
    }

    @Override
    public void directRequestPermissions(int requestCode, @NonNull String... perms) {
        throw new IllegalStateException("Should never be requesting permissions on API < 23!");
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        return false;
    }

    @Override
    @SuppressLint("NewApi")
    public void showRequestPermissionRationale(@NonNull String rationale,
                                               int positiveButton,
                                               int negativeButton,
                                               int requestCode,
                                               @NonNull String... perms) {
        throw new IllegalStateException("Should never be requesting permissions on API < 23!");
    }

    @Override
    public Context getContext() {
        return null;
    }
}
