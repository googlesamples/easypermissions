package pub.devrel.easypermissions.helper;

import android.app.FragmentManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

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
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void showRequestPermissionRationale(@NonNull String rationale,
                                               int positiveButton,
                                               int negativeButton,
                                               int requestCode,
                                               @NonNull String... perms) {
        RationaleDialogFragment
                .newInstance(positiveButton, negativeButton, rationale, requestCode, perms)
                .show(getFragmentManager(), RationaleDialogFragment.TAG);
    }
}
