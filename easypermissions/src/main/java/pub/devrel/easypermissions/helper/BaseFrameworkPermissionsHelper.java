package pub.devrel.easypermissions.helper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import pub.devrel.easypermissions.RationaleDialogFragment;

/**
 * Implementation of {@link PermissionHelper} for framework host classes.
 */
public abstract class BaseFrameworkPermissionsHelper<T> extends PermissionHelper<T> {

    public BaseFrameworkPermissionsHelper(@NonNull T host) {
        super(host);
    }

    public abstract FragmentManager getFragmentManager();

    @Override
    public void showRequestPermissionRationale(@NonNull String rationale,
                                               @NonNull String positiveButton,
                                               @NonNull String negativeButton,
                                               @StyleRes int theme,
                                               int requestCode,
                                               @NonNull String... perms) {
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentByTag(RationaleDialogFragment.TAG);
        if (fragment != null) return;
        RationaleDialogFragment
                .newInstance(positiveButton, negativeButton, rationale, theme, requestCode, perms)
                .showAllowingStateLoss(fm, RationaleDialogFragment.TAG);
    }
}
