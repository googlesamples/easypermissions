package pub.devrel.easypermissions.sample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.List;
import pub.devrel.easypermissions.EasyPermission;

public class MainFragment extends Fragment implements EasyPermission.PermissionCallback {

    private static final String TAG = "MainFragment";
    private static final int RC_SMS_PERM = 122;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Create view
        View v = inflater.inflate(R.layout.fragment_main, container);

        // Button click listener
        v.findViewById(R.id.button_sms)
                .setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        smsTask();
                    }
                });

        return v;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Do something after user returned from app settings screen. User may be
        // changed/updated the permissions. Let's check whether the user has changed sms
        // permission or not after returned from settings screen
        if (requestCode == EasyPermission.SETTINGS_REQ_CODE) {
            boolean hasReadSmsPermission =
                    EasyPermission.hasPermissions(getContext(), Manifest.permission.READ_SMS);
            String hasReadSmsPermissionText =
                    getString(R.string.has_read_sms_permission, hasReadSmsPermission);

            Toast.makeText(getContext(), hasReadSmsPermissionText, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void smsTask() {
        EasyPermission.with(this)
                .addRequestCode(RC_SMS_PERM)
                .permissions(Manifest.permission.READ_SMS)
                .rationale(getString(R.string.rationale_sms))
                .request();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        EasyPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


    @Override public void onPermissionGranted(int requestCode, List<String> perms) {
        Toast.makeText(getActivity(), "TODO: SMS Granted", Toast.LENGTH_SHORT)
                .show();
    }

    @Override public void onPermissionDenied(int requestCode, List<String> perms) {
        Toast.makeText(getActivity(), "TODO: SMS Denied", Toast.LENGTH_SHORT)
                .show();


        // Handle negative button on click listener
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                // Let's show a toast
                Toast.makeText(getContext(), R.string.settings_dialog_canceled, Toast.LENGTH_SHORT)
                        .show();
            }
        };

        // (Optional) Check whether the user denied permissions and checked NEVER ASK AGAIN.
        // This will display a dialog directing them to enable the permission in app settings.
        EasyPermission.checkDeniedPermissionsNeverAskAgain(this, getString(R.string.rationale_ask_again),
                                                           R.string.setting, R.string.cancel,
                                                           onClickListener, perms);
    }
}
