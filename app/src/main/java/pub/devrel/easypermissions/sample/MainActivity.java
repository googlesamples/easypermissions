/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easypermissions.sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import java.util.List;
import pub.devrel.easypermissions.EasyPermission;

public class MainActivity extends AppCompatActivity implements EasyPermission.PermissionCallback {

    private static final String TAG = "MainActivity";

    private static final int RC_CAMERA_PERM = 123;
    private static final int RC_LOCATION_CONTACTS_PERM = 124;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button click listener that will request one permission.
        findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cameraTask();
            }
        });

        // Button click listener that will request two permissions.
        findViewById(R.id.button_location_and_wifi).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                locationAndContactsTask();
            }
        });
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EasyPermission.SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen
            // Let's show Toast for example
            Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void cameraTask() {
        EasyPermission.with(this)
                .rationale(getString(R.string.rationale_camera))
                .addRequestCode(RC_CAMERA_PERM)
                .permissions(Manifest.permission.CAMERA)
                .request();
    }

    @Override public void onPermissionGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_CAMERA_PERM:
                Toast.makeText(this, "TODO: Camera Granted", Toast.LENGTH_LONG)
                        .show();
                break;
            case RC_LOCATION_CONTACTS_PERM:
                Toast.makeText(this, "TODO: LOCATION Granted", Toast.LENGTH_LONG)
                        .show();
                break;
        }
    }

    @Override public void onPermissionDenied(int requestCode, List<String> perms) {
        //switch (requestCode) {
        //    case RC_CAMERA_PERM:
        //        break;
        //    case RC_LOCATION_CONTACTS_PERM:
        //        break;
        //}
        Toast.makeText(this, "onPermissionDenied:" + requestCode + ":" + perms.size(), Toast.LENGTH_SHORT)
                .show();

        //可选的,跳转到Settings界面
        EasyPermission.checkDeniedPermissionsNeverAskAgain(this, getString(R.string.rationale_ask_again),
                                                           R.string.setting, R.string.cancel, null, perms);
    }


    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


    public void locationAndContactsTask() {
        EasyPermission.with(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS)
                .rationale(getString(R.string.rationale_location_contacts))
                .addRequestCode(RC_LOCATION_CONTACTS_PERM)
                .request();
    }

}
