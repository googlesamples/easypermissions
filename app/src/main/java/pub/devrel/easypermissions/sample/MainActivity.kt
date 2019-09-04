/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easypermissions.sample

import android.Manifest.permission.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.annotations.AfterPermissionGranted
import pub.devrel.easypermissions.dialogs.settings.DEFAULT_SETTINGS_REQ_CODE
import pub.devrel.easypermissions.dialogs.settings.SettingsDialog
import pub.devrel.easypermissions.facade.EasyPermissions

private const val TAG = "MainActivity"
private const val REQUEST_CODE_CAMERA_PERMISSION = 123
private const val REQUEST_CODE_STORAGE_PERMISSION = 124
private const val REQUEST_CODE_LOCATION_AND_CONTACTS_PERMISSION = 125

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity(),
    EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    // ============================================================================================
    //  Activity Lifecycle
    // ============================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_camera.setOnClickListener {
            onClickRequestPermissionCameraButton()
        }
        button_storage.setOnClickListener {
            onClickRequestPermissionStorageButton()
        }
        button_location_and_contacts.setOnClickListener {
            onClickRequestPermissionLocationAndContactsButton()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DEFAULT_SETTINGS_REQ_CODE) {
            val yes = getString(R.string.yes)
            val no = getString(R.string.no)

            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                this,
                getString(
                    R.string.returned_from_app_settings_to_activity,
                    if (hasCameraPermission()) yes else no,
                    if (hasLocationAndContactsPermissions()) yes else no,
                    if (hasSmsPermission()) yes else no,
                    if (hasStoragePermission()) yes else no
                ),
                LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    // ============================================================================================
    //  Implementation Permission Callbacks
    // ============================================================================================

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, getString(R.string.log_permissions_granted, requestCode, perms.size))
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, getString(R.string.log_permissions_denied, requestCode, perms.size))

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms.toString())) {
            SettingsDialog.Builder(this).build().show()
        }
    }

    // ============================================================================================
    //  Implementation Rationale Callbacks
    // ============================================================================================

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d(TAG, getString(R.string.log_permission_rationale_accepted, requestCode))
    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d(TAG, getString(R.string.log_permission_rationale_denied, requestCode))
    }

    // ============================================================================================
    //  Private Methods
    // ============================================================================================

    @AfterPermissionGranted(REQUEST_CODE_CAMERA_PERMISSION)
    private fun onClickRequestPermissionCameraButton() {
        if (hasCameraPermission()) {
            // Have permission, do things!
            Toast.makeText(this, "TODO: Camera things", LENGTH_LONG).show()
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_camera_rationale_message),
                REQUEST_CODE_CAMERA_PERMISSION,
                CAMERA
            )
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_STORAGE_PERMISSION)
    private fun onClickRequestPermissionStorageButton() {
        if (hasCameraPermission()) {
            // Have permission, do things!
            Toast.makeText(this, "TODO: Storage things", LENGTH_LONG).show()
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_storage_rationale_message),
                REQUEST_CODE_STORAGE_PERMISSION,
                WRITE_EXTERNAL_STORAGE
            )
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_LOCATION_AND_CONTACTS_PERMISSION)
    private fun onClickRequestPermissionLocationAndContactsButton() {
        if (hasLocationAndContactsPermissions()) {
            // Have permissions, do things!
            Toast.makeText(this, "TODO: Location and Contacts things", LENGTH_LONG).show()
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_location_and_contacts_rationale_message),
                REQUEST_CODE_LOCATION_AND_CONTACTS_PERMISSION,
                ACCESS_FINE_LOCATION, READ_CONTACTS
            )
        }
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, CAMERA)
    }

    private fun hasLocationAndContactsPermissions(): Boolean {
        return EasyPermissions.hasPermissions(this, ACCESS_FINE_LOCATION, READ_CONTACTS)
    }

    private fun hasSmsPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, READ_SMS)
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, WRITE_EXTERNAL_STORAGE)
    }
}
