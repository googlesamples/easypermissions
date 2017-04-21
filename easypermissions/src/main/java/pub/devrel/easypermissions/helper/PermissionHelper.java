package pub.devrel.easypermissions.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.RationaleDialogFragment;

/**
 * Delegate class to make permission calls based on the 'host' (Fragment, Activity, etc).
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class PermissionHelper<T> {

    private static final String TAG = "PermissionHelper";

    private T mHost;

    @NonNull
    public static PermissionHelper getInstance(Object host) {
        // If the API version is < M, just return an PermissionsHelper that does not do anything.
        if (Build.VERSION.SDK_INT < 23) {
            return new LowApiPermissionsHelper(host);
        }

        if (host instanceof Activity) {
            return new ActivityPermissionHelper((Activity) host);
        } else if (host instanceof Fragment) {
            return new SupportFragmentPermissionHelper((Fragment) host);
        } else if (host instanceof android.app.Fragment) {
            return new FrameworkFragmentPermissionHelper((android.app.Fragment) host);
        } else {
            throw new IllegalArgumentException("Host object must be an Activity or Fragment");
        }
    }

    // ============================================================================
    // Public concrete methods
    // ============================================================================

    public PermissionHelper(@NonNull T host) {
        this.mHost = host;
    }

    public static boolean hasPermissions(Context context, String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");

            // DANGER ZONE!!! Changing this will break the library.
            return true;
        }

        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public void requestPermissions(@NonNull String rationale,
                                   int requestCode,
                                   @NonNull String... perms) {
        // Use default Android OK and Cancel strings
        requestPermissions(rationale,
                android.R.string.ok,
                android.R.string.cancel,
                requestCode,
                perms);
    }

    public boolean shouldShowRationale(@NonNull String[] perms) {
        for (String perm : perms) {
            if (shouldShowRequestPermissionRationale(perm)) {
                return true;
            }
        }
        return false;
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

    public boolean somePermissionDenied(@NonNull String[] perms) {
        return shouldShowRationale(perms);
    }

    // ============================================================================
    // Public abstract methods
    // ============================================================================

    public abstract void requestPermissions(@NonNull String rationale,
                                            @StringRes int positiveButton,
                                            @StringRes int negativeButton,
                                            int requestCode,
                                            @NonNull String... perms);

    public abstract boolean shouldShowRequestPermissionRationale(@NonNull String perm);

    // ============================================================================
    // Protected methods
    // ============================================================================

    /**
     * Show a {@link RationaleDialogFragment} explaining permission request rationale.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    protected void showRationaleDialogFragment(@NonNull android.app.FragmentManager fragmentManager,
                                               @NonNull String rationale,
                                               @StringRes int positiveButton,
                                               @StringRes int negativeButton,
                                               int requestCode,
                                               @NonNull String... perms) {

        RationaleDialogFragment
                .newInstance(positiveButton, negativeButton, rationale, requestCode, perms)
                .show(fragmentManager, RationaleDialogFragment.TAG);
    }

    protected void notifyAlreadyHasPermissions(Object object,
                                               int requestCode,
                                               @NonNull String[] perms) {
        int[] grantResults = new int[perms.length];
        for (int i = 0; i < perms.length; i++) {
            grantResults[i] = PackageManager.PERMISSION_GRANTED;
        }

        EasyPermissions.onRequestPermissionsResult(requestCode, perms, grantResults, object);
    }

    @NonNull
    protected T getHost() {
        return mHost;
    }

}
