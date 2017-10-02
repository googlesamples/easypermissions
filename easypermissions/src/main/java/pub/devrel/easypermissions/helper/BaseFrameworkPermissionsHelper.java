package pub.devrel.easypermissions.helper;

import android.app.FragmentManager;
import android.support.annotation.NonNull;

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
                                               int positiveButton,
                                               int negativeButton,
                                               int requestCode,
                                               @NonNull String... perms) {
        RationaleDialogFragment
                .newInstance(positiveButton, negativeButton, rationale, requestCode, perms)
                .showAllowingStateLoss(getFragmentManager(), RationaleDialogFragment.TAG);
    }
}
