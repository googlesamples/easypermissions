package pub.devrel.easypermissions.helper;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Permissions helper for {@link Activity}.
 */
class ActivityPermissionHelper extends BaseFrameworkPermissionsHelper<Activity> {

    public ActivityPermissionHelper(Activity host) {
        super(host);
    }

    @Override
    public FragmentManager getFragmentManager() {
        return getHost().getFragmentManager();
    }

    @Override
    public void directRequestPermissions(int requestCode, @NonNull String... perms) {
        ActivityCompat.requestPermissions(getHost(), perms, requestCode);
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
