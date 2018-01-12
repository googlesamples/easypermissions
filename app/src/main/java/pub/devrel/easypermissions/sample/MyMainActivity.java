package pub.devrel.easypermissions.sample;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mgsoftware.kotlinapp.DialogFragmentCallback;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import pub.devrel.easypermissions.AppSettingDialogFragment;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import pub.devrel.easypermissions.helper.PermissionHelper;

public class MyMainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, DialogFragmentCallback, PermissionHelper.Callback {
    private static final String TAG = "MainActivity";

    private static final String[] REQUESTED_PERMISSIONS_ON_START = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int RC_REQUIRED_PERM = 125;
    private static final int RC_APP_SETTING_DIALOG = 126;

    // In this case prevent showing dialogs under android permission dialog
    private boolean waiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_main);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        waiting = hasCurrentPermissionsRequest(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EasyPermissions.hasPermissions(this, REQUESTED_PERMISSIONS_ON_START)) {
            if (!waiting) {
                if (!isShowing(getSupportFragmentManager(), AppSettingDialogFragment.Companion.getTAG())) {
                    PermissionRequest request = new PermissionRequest.Builder(this,
                                                                              RC_REQUIRED_PERM,
                                                                              REQUESTED_PERMISSIONS_ON_START)
                            .setRationale("my_rationale")
                            .build();
                    EasyPermissions.requestPermissions(request);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        waiting = false;
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (!EasyPermissions.hasPermissions(this, REQUESTED_PERMISSIONS_ON_START)) {
            if (!isShowing(getSupportFragmentManager(), AppSettingDialogFragment.Companion.getTAG())) {
                Bundle config = new Bundle();
                config.putInt(AppSettingDialogFragment.Companion.getKEY_THEME_RED_ID(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);
                config.putString(AppSettingDialogFragment.Companion.getKEY_POSITIVE_BUTTON_TEXT(), "my_settings");
                AppSettingDialogFragment.Companion.newInstance(RC_APP_SETTING_DIALOG, config).show(getSupportFragmentManager(), AppSettingDialogFragment.Companion.getTAG());
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, @Nullable Bundle data) {
        if(requestCode == AppSettingDialogFragment.Companion.getDEFAULT_SETTINGS_REQ_CODE()) {
            if(resultCode == DialogFragmentCallback.Companion.getRESULT_CANCEL()) {
                // TODO 
            }
        }
    }

    @Override
    public void onAndroidRequestPermissionsCalled() {
        waiting = true;
    }

    private boolean hasCurrentPermissionsRequest(Bundle savedInstanceState) {
        return savedInstanceState != null && savedInstanceState.getBoolean("android:hasCurrentPermissionsRequest", false);
    }

    private boolean isShowing(FragmentManager fragmentManager, String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        return fragment != null;

    }
}
