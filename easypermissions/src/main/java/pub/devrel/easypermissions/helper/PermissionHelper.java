package pub.devrel.easypermissions.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * Delegate class to make permission calls based on the 'host' (Fragment, Activity, etc).
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class PermissionHelper<T> {

    private static final String TAG = "PermissionHelper";

    private T mHost;

    @NonNull
    public static PermissionHelper newInstance(Activity host) {
        if (Build.VERSION.SDK_INT < 23) {
            return new LowApiPermissionsHelper(host);
        }

        return new ActivityPermissionHelper(host);
    }

    @NonNull
    public static PermissionHelper newInstance(Fragment host) {
        if (Build.VERSION.SDK_INT < 23) {
            return new LowApiPermissionsHelper(host);
        }

        return new SupportFragmentPermissionHelper(host);
    }

    @NonNull
    public static PermissionHelper newInstance(android.app.Fragment host) {
        if (Build.VERSION.SDK_INT < 23) {
            return new LowApiPermissionsHelper(host);
        }

        return new FrameworkFragmentPermissionHelper(host);
    }

    // ============================================================================
    // Public concrete methods
    // ============================================================================

    public PermissionHelper(@NonNull T host) {
        mHost = host;
    }

    public boolean shouldShowRationale(@NonNull String... perms) {
        for (String perm : perms) {
            if (shouldShowRequestPermissionRationale(perm)) {
                return true;
            }
        }
        return false;
    }

    public void requestPermissions(@NonNull String rationale,
                                   @StringRes int positiveButton,
                                   @StringRes int negativeButton,
                                   int requestCode,
                                   @NonNull String... perms) {
        if (shouldShowRationale(perms)) {
            showRequestPermissionRationale(
                    rationale, positiveButton, negativeButton, requestCode, perms);
        } else {
            directRequestPermissions(requestCode, perms);
        }
    }

    public boolean somePermissionPermanentlyDenied(@NonNull List<String> perms) {
        for (String deniedPermission : perms) {
            if (permissionPermanentlyDenied(deniedPermission)) {
                return true;
            }
        }

        return false;
    }

    public boolean permissionPermanentlyDenied(@NonNull String perms) {
        return !shouldShowRequestPermissionRationale(perms);
    }

    public boolean somePermissionDenied(@NonNull String... perms) {
        return shouldShowRationale(perms);
    }

    @NonNull
    public T getHost() {
        return mHost;
    }

    // ============================================================================
    // Public abstract methods
    // ============================================================================

    public abstract void directRequestPermissions(int requestCode, @NonNull String... perms);

    public abstract boolean shouldShowRequestPermissionRationale(@NonNull String perm);

    public abstract void showRequestPermissionRationale(@NonNull String rationale,
                                                        @StringRes int positiveButton,
                                                        @StringRes int negativeButton,
                                                        int requestCode,
                                                        @NonNull String... perms);
    public abstract Context getContext();

}
