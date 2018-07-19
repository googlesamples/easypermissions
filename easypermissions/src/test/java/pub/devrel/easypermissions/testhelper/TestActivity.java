package pub.devrel.easypermissions.testhelper;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class TestActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
