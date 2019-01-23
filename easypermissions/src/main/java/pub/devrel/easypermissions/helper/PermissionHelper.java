package pub.devrel.easypermissions.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Delegate class to make permission calls based on the 'host' (Fragment, Activity, etc).
 */
public abstract class PermissionHelper<T> {

    private T mHost;

    @NonNull
    public static PermissionHelper<? extends Activity> newInstance(Activity host) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new LowApiPermissionsHelper<>(host);
        }

        if (host instanceof AppCompatActivity)
            return new AppCompatActivityPermissionsHelper((AppCompatActivity) host);
        else {
            return new ActivityPermissionHelper(host);
        }
    }

    @NonNull
    public static PermissionHelper<Fragment> newInstance(Fragment host) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new LowApiPermissionsHelper<>(host);
        }

        return new SupportFragmentPermissionHelper(host);
    }

    // ============================================================================
    // Public concrete methods
    // ============================================================================

    public PermissionHelper(@NonNull T host) {
        mHost = host;
    }

    private boolean shouldShowRationale(@NonNull String... perms) {
        for (String perm : perms) {
            if (shouldShowRequestPermissionRationale(perm)) {
                return true;
            }
        }
        return false;
    }

    public void requestPermissions(@NonNull String rationale,
                                   @NonNull String positiveButton,
                                   @NonNull String negativeButton,
                                   @StyleRes int theme,
                                   int requestCode,
                                   @NonNull String... perms) {
        if (shouldShowRationale(perms)) {
            showRequestPermissionRationale(
                    rationale, positiveButton, negativeButton, theme, requestCode, perms);
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
                                                        @NonNull String positiveButton,
                                                        @NonNull String negativeButton,
                                                        @StyleRes int theme,
                                                        int requestCode,
                                                        @NonNull String... perms);

    public abstract Context getContext();

}
