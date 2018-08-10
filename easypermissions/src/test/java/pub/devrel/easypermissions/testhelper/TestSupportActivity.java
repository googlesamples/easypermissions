package pub.devrel.easypermissions.testhelper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.R;

public class TestSupportActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    public static final int REQUEST_CODE = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getTheme().applyStyle(R.style.Theme_AppCompat, true);
        super.onCreate(savedInstanceState);
    }

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
