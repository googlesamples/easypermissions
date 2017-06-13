package pub.devrel.easypermissions.sample;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * NOTE: This class is used only for testing EasyPermissions outside of the support library.
 *
 * See {@link MainActivity} for an example of how to use EasyPermissions.
 */
public class BasicActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "BasicActivity";

    private static final int RC_REQUEST_SMS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        findViewById(R.id.button_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSmsTask();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_REQUEST_SMS)
    public void doSmsTask() {
        String perm = Manifest.permission.READ_SMS;
        if (!EasyPermissions.hasPermissions(this, perm)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_sms),
                    RC_REQUEST_SMS, perm);
        } else {
            Toast.makeText(this, "TODO: SMS Task", Toast.LENGTH_SHORT).show();
        }
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
