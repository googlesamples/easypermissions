package pub.devrel.easypermissions.sample;


import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A component tied to a <i>feature</i>.
 * This component can be viewed as a partial view in any
 * architecture that encapsulates a logic and exposes
 * it through public methods.
 */
public class FeatureComponent implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "FeatureComponent";
    private static final int RC_WRITE_EXTERNAL_STORAGE = 102;

    private final Context context;

    public FeatureComponent(Context context) {
        this.context = context;
    }

    public void start() {
        writeExternalStorage();
    }

    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE)
    private void writeExternalStorage() {
        final String[] permissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
        if (EasyPermissions.hasPermissions(context, permissions)) {
            Toast.makeText(context, "TODO: Use external storage!", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(context, context.getString(R.string.rationale_write_external_storage),
                    RC_WRITE_EXTERNAL_STORAGE, permissions);
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

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {}
}
