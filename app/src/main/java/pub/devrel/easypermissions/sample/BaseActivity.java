package pub.devrel.easypermissions.sample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * simplier request permission in BaseActivity
 * Created by tsy on 2016/12/2.
 */

public class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private Map<Integer, PermissionCallback> mPermissonCallbacks = null;
    private Map<Integer, String[]> mPermissions = null;

    protected interface PermissionCallback {
        /**
         * has all permission
         * @param allPerms all permissions
         */
        void hasPermission(List<String> allPerms);

        /**
         * denied some permission
         * @param deniedPerms denied permission
         * @param grantedPerms granted permission
         * @param hasPermanentlyDenied has permission denied permanently
         */
        void noPermission(List<String> deniedPerms, List<String> grantedPerms, Boolean hasPermanentlyDenied);
    }

    /**
     * request permission
     * @param rationale if denied first, next request rationale
     * @param requestCode requestCode
     * @param perms permissions
     * @param callback callback
     */
    protected void performCodeWithPermission(@NonNull String rationale,
                                             final int requestCode, @NonNull String[] perms, @NonNull PermissionCallback callback) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            callback.hasPermission(Arrays.asList(perms));
        } else {
            if(mPermissonCallbacks == null) {
                mPermissonCallbacks = new HashMap<>();
            }
            mPermissonCallbacks.put(requestCode, callback);

            if(mPermissions == null) {
                mPermissions = new HashMap<>();
            }
            mPermissions.put(requestCode, perms);

            EasyPermissions.requestPermissions(this, rationale, requestCode, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if(mPermissonCallbacks == null || !mPermissonCallbacks.containsKey(requestCode)) {
            return;
        }
        if(mPermissions == null || !mPermissions.containsKey(requestCode)) {
            return;
        }

        // 100% granted permissions
        if(mPermissions.get(requestCode).length == perms.size()) {
            mPermissonCallbacks.get(requestCode).hasPermission(Arrays.asList(mPermissions.get(requestCode)));
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if(mPermissonCallbacks == null || !mPermissonCallbacks.containsKey(requestCode)) {
            return;
        }
        if(mPermissions == null || !mPermissions.containsKey(requestCode)) {
            return;
        }

        //granted permission
        List<String> grantedPerms = new ArrayList<>();
        for(String perm : mPermissions.get(requestCode)) {
            if(!perms.contains(perm)) {
                grantedPerms.add(perm);
            }
        }

        //check has permission denied permanently
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            mPermissonCallbacks.get(requestCode).noPermission(perms, grantedPerms, true);
        } else {
            mPermissonCallbacks.get(requestCode).noPermission(perms, grantedPerms, false);
        }
    }

    /**
     * alert AppSet Permission
     * @param rationale alert setting rationale
     */
    protected void alertAppSetPermission(String rationale) {
        new AppSettingsDialog.Builder(this, rationale)
                .setTitle(getString(R.string.title_settings_dialog))
                .setPositiveButton(getString(R.string.setting))
                .setNegativeButton(getString(R.string.cancel), null)
                .build()
                .show();
    }

    /**
     * alert AppSet Permission
     * @param rationale alert setting rationale
     * @param requestCode onActivityResult requestCode
     */
    protected void alertAppSetPermission(String rationale, int requestCode) {
        new AppSettingsDialog.Builder(this, rationale)
                .setTitle(getString(R.string.title_settings_dialog))
                .setPositiveButton(getString(R.string.setting))
                .setNegativeButton(getString(R.string.cancel), null)
                .setRequestCode(requestCode)
                .build()
                .show();
    }
}
