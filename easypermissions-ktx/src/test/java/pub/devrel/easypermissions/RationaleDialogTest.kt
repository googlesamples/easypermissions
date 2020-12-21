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
package mvalceleanu.easypermissions

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_SMS
import android.app.Dialog
import android.content.DialogInterface
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import mvalceleanu.easypermissions.components.TestActivity
import mvalceleanu.easypermissions.dialogs.rationale.RationaleDialog
import mvalceleanu.easypermissions.models.PermissionRequest

private const val REQUEST_CODE = 5
private val PERMS = arrayOf(READ_SMS, ACCESS_FINE_LOCATION)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class RationaleDialogTest {

    @Mock
    private lateinit var dialogInterface: DialogInterface
    @Mock
    private lateinit var testActivity: TestActivity

    private lateinit var rationaleDialog: RationaleDialog

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun shouldOnRationaleAccepted_whenPositiveButtonClicked() {
        val permissionRequest = PermissionRequest.Builder(testActivity)
            .code(REQUEST_CODE)
            .perms(PERMS)
            .build()
        rationaleDialog = RationaleDialog(testActivity, permissionRequest)
        rationaleDialog.onClick(dialogInterface, Dialog.BUTTON_POSITIVE)

        verify(testActivity).onRationaleAccepted(REQUEST_CODE)
        verify(testActivity, never()).onRationaleDenied(anyInt())
        verify(testActivity, never()).onPermissionsGranted(anyInt(), anyList())
        verify(testActivity, never()).onPermissionsDenied(anyInt(), anyList())
    }

    @Test
    fun shouldOnRationaleAcceptedWithDefaultValues_whenPositiveButtonClickedWithEmptyRequest() {
        val permissionRequest = PermissionRequest.Builder(testActivity).build()
        rationaleDialog = RationaleDialog(testActivity, permissionRequest)
        rationaleDialog.onClick(dialogInterface, Dialog.BUTTON_POSITIVE)

        verify(testActivity).onRationaleAccepted(0)
        verify(testActivity, never()).onRationaleDenied(anyInt())
        verify(testActivity, never()).onPermissionsGranted(anyInt(), anyList())
        verify(testActivity, never()).onPermissionsDenied(anyInt(), anyList())
    }

    @Test
    fun shouldOnRationaleDeclined_whenNegativeButtonClicked() {
        val permissionRequest = PermissionRequest.Builder(testActivity)
            .code(REQUEST_CODE)
            .perms(PERMS)
            .build()
        rationaleDialog = RationaleDialog(testActivity, permissionRequest)
        rationaleDialog.onClick(dialogInterface, Dialog.BUTTON_NEGATIVE)

        verify(testActivity).onRationaleDenied(REQUEST_CODE)
        verify(testActivity).onPermissionsDenied(REQUEST_CODE, PERMS.toList())
        verify(testActivity, never()).onRationaleAccepted(anyInt())
        verify(testActivity, never()).onPermissionsGranted(anyInt(), anyList())
    }

    @Test
    fun shouldOnRationaleDeclinedWithDefaultValues_whenPositiveButtonClickedWithEmptyRequest() {
        val permissionRequest = PermissionRequest.Builder(testActivity).build()
        rationaleDialog = RationaleDialog(testActivity, permissionRequest)
        rationaleDialog.onClick(dialogInterface, Dialog.BUTTON_NEGATIVE)

        verify(testActivity).onRationaleDenied(0)
        verify(testActivity).onPermissionsDenied(0, emptyList())
        verify(testActivity, never()).onRationaleAccepted(anyInt())
        verify(testActivity, never()).onPermissionsGranted(anyInt(), anyList())
    }

    @Test
    fun shouldNotCallbacksInvoked_whenAnyButtonClicked() {
        val permissionRequest = PermissionRequest.Builder(testActivity)
            .code(REQUEST_CODE)
            .perms(PERMS)
            .build()
        rationaleDialog = RationaleDialog(testActivity, permissionRequest)

        verify(testActivity, never()).onRationaleAccepted(anyInt())
        verify(testActivity, never()).onRationaleDenied(anyInt())
        verify(testActivity, never()).onPermissionsGranted(anyInt(), anyList())
        verify(testActivity, never()).onPermissionsDenied(anyInt(), anyList())
    }
}
