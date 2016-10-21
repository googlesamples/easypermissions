package pub.devrel.easypermissions.sample;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * NOTE: This class is used only for testing EasyPermissions outside of the support library.
 *
 * See {@link MainActivity} for an example of how to use EasyPermissions.
 */
public class BasicActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "BasicActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        findViewById(R.id.button_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request SMS permission
                EasyPermissions.requestPermissions(BasicActivity.this, "NEED SMS PLZ", 1001,
                        Manifest.permission.READ_SMS);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
    }
}
