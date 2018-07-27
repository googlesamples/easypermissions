package pub.devrel.easypermissions.testhelper;

import android.app.Fragment;
import android.support.annotation.NonNull;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class TestFragment extends Fragment
        implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    public static final int REQUEST_CODE = 2;

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @AfterPermissionGranted(REQUEST_CODE)
    public void afterPermissionGranted() {

    }

    @Override
    public void onRationaleAccepted(int requestCode) {

    }

    @Override
    public void onRationaleDenied(int requestCode) {

    }
}
