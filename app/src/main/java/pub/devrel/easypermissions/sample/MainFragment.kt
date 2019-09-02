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

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

private const val TAG = "MainFragment"
private const val REQUEST_CODE_SMS_PERMISSION = 126

class MainFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    // ============================================================================================
    //  Fragment Lifecycle
    // ============================================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_main, container)
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
    }

    // ============================================================================================
    //  Implementation OnClick
    // ============================================================================================

    @AfterPermissionGranted(REQUEST_CODE_SMS_PERMISSION)
    fun onClickRequestPermissionSMSButton(view: View) {
        if (EasyPermissions.hasPermissions(context!!, Manifest.permission.READ_SMS)) {
            // Have permission, do the thing!
            Toast.makeText(activity, "TODO: SMS things", Toast.LENGTH_LONG).show()
        } else {
            // Request one permission
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_sms_rationale_message),
                REQUEST_CODE_SMS_PERMISSION,
                Manifest.permission.READ_SMS
            )
        }
    }
}