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
package pub.devrel.easypermissions.sample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.mgsoftware.kotlinapp.DialogFragmentCallback
import kotlinx.android.synthetic.main.activity_main.*

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingDialogFragment
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, DialogFragmentCallback {
    val RC_APP_SETTING_DIALOG = 125

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_test.setOnClickListener({
            val config = Bundle()
            config.putInt(AppSettingDialogFragment.KEY_THEME_RED_ID, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
            config.putString(AppSettingDialogFragment.KEY_POSITIVE_BUTTON_TEXT, "settings")
            AppSettingDialogFragment.newInstance(RC_APP_SETTING_DIALOG, config).show(supportFragmentManager, AppSettingDialogFragment::class.java.name)
        })

        // Button click listener that will request one permission.
        findViewById<View>(R.id.button_camera).setOnClickListener { cameraTask() }

        // Button click listener that will request two permissions.
        findViewById<View>(R.id.button_location_and_contacts).setOnClickListener { locationAndContactsTask() }
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    private fun hasLocationAndContactsPermissions(): Boolean {
        return EasyPermissions.hasPermissions(this, *LOCATION_AND_CONTACTS)
    }

    private fun hasSmsPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_SMS)
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    fun cameraTask() {
        if (hasCameraPermission()) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: Camera things", Toast.LENGTH_LONG).show()
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera),
                    RC_CAMERA_PERM,
                    Manifest.permission.CAMERA)
        }
    }

    @AfterPermissionGranted(RC_LOCATION_CONTACTS_PERM)
    fun locationAndContactsTask() {
        if (hasLocationAndContactsPermissions()) {
            // Have permissions, do the thing!
            Toast.makeText(this, "TODO: Location and Contacts things", Toast.LENGTH_LONG).show()
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location_contacts),
                    RC_LOCATION_CONTACTS_PERM,
                    *LOCATION_AND_CONTACTS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            val yes = getString(R.string.yes)
            val no = getString(R.string.no)

            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                    this,
                    getString(R.string.returned_from_app_settings_to_activity,
                            if (hasCameraPermission()) yes else no,
                            if (hasLocationAndContactsPermissions()) yes else no,
                            if (hasSmsPermission()) yes else no),
                    Toast.LENGTH_LONG)
                    .show()
        }
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        when(requestCode) {
            RC_APP_SETTING_DIALOG -> {

            }
        }

    }

    companion object {

        private val TAG = "MainActivity"
        private val LOCATION_AND_CONTACTS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS)

        private const val RC_CAMERA_PERM = 123
        private const val RC_LOCATION_CONTACTS_PERM = 124
    }
}
