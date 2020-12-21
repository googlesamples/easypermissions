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
import android.app.Application
import android.app.Dialog
import android.widget.TextView
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import mvalceleanu.easypermissions.components.TestActivity
import mvalceleanu.easypermissions.components.TestAppCompatActivity
import mvalceleanu.easypermissions.components.TestFragment
import mvalceleanu.easypermissions.components.TestSupportFragmentActivity
import mvalceleanu.easypermissions.controllers.ActivityController
import mvalceleanu.easypermissions.controllers.FragmentController
import java.util.*
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.capture
import org.junit.Assert.fail
import org.mockito.Mockito.*
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlertDialog
import mvalceleanu.easypermissions.facade.EasyPermissions
import mvalceleanu.easypermissions.models.PermissionRequest

private const val RATIONALE = "RATIONALE"
private const val POSITIVE = "POSITIVE"
private const val NEGATIVE = "NEGATIVE"

private val ONE_PERM = arrayOf(READ_SMS)
private val ALL_PERMS = arrayOf(READ_SMS, ACCESS_FINE_LOCATION)
private val SMS_DENIED_RESULT = intArrayOf(PERMISSION_DENIED, PERMISSION_GRANTED)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
@SuppressWarnings("SameParameterValue")
class EasyPermissionsTest {

    private var shadowApp: ShadowApplication? = null
    private var app: Application? = null

    private lateinit var spyActivity: TestActivity
    private lateinit var spyFragmentActivity: TestSupportFragmentActivity
    private lateinit var spyAppCompatActivity: TestAppCompatActivity
    private lateinit var spyFragment: TestFragment

    private lateinit var fragmentController: FragmentController<TestFragment>
    private lateinit var activityController: ActivityController<TestActivity>
    private lateinit var fragmentActivityController: ActivityController<TestSupportFragmentActivity>
    private lateinit var appCompatActivityController: ActivityController<TestAppCompatActivity>

    @Captor
    private lateinit var integerCaptor: ArgumentCaptor<Int>
    @Captor
    private lateinit var listCaptor: ArgumentCaptor<List<String>>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        app = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(app)

        activityController = ActivityController(TestActivity::class.java)
        fragmentActivityController =
            ActivityController(TestSupportFragmentActivity::class.java)
        appCompatActivityController = ActivityController(TestAppCompatActivity::class.java)
        fragmentController = FragmentController(TestFragment::class.java)

        spyActivity = spy(activityController.resume())
        spyFragmentActivity = spy(fragmentActivityController.resume())
        spyAppCompatActivity = spy(appCompatActivityController.resume())
        spyFragment = spy(fragmentController.resume())
    }

    // ------ General tests ------

    @Test
    fun shouldNotHavePermissions_whenNoPermissionsGranted() {
        assertThat(EasyPermissions.hasPermissions(app, *ALL_PERMS)).isFalse()
    }

    @Test
    fun shouldNotHavePermissions_whenNotAllPermissionsGranted() {
        shadowApp?.grantPermissions(*ONE_PERM)
        assertThat(EasyPermissions.hasPermissions(app, *ALL_PERMS)).isFalse()
    }

    @Test
    fun shouldHavePermissions_whenAllPermissionsGranted() {
        shadowApp?.grantPermissions(*ALL_PERMS)
        assertThat(EasyPermissions.hasPermissions(app, *ALL_PERMS)).isTrue()
    }

    @Test
    fun shouldThrowException_whenHasPermissionsWithNullContext() {
        try {
            EasyPermissions.hasPermissions(null, *ALL_PERMS)
            fail("IllegalStateException expected because of null context.")
        } catch (e: IllegalArgumentException) {
            assertThat(e).hasMessageThat()
                .isEqualTo("Can't check permissions for null context")
        }
    }

    // ------ From Activity ------

    @Test
    fun shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromActivity() {
        EasyPermissions.onRequestPermissionsResult(
            TestActivity.REQUEST_CODE,
            ALL_PERMS,
            SMS_DENIED_RESULT,
            spyActivity
        )

        verify(spyActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(ACCESS_FINE_LOCATION)))

        verify(spyActivity)
            .onPermissionsDenied(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(READ_SMS)))

        verify(spyActivity, never()).afterPermissionGranted()
    }

    @Test
    fun shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromActivity() {
        grantPermissions(ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyActivity,
            RATIONALE,
            TestActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        verify(
            spyActivity,
            never()
        ).requestPermissions(any(Array<String>::class.java), anyInt())
        assertThat(integerCaptor.value).isEqualTo(TestActivity.REQUEST_CODE)
        assertThat(listCaptor.value).containsAtLeastElementsIn(ALL_PERMS)
    }

    @Test
    fun shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFromActivity() {
        grantPermissions(ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyActivity,
            RATIONALE,
            TestActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spyActivity, times(2)).afterPermissionGranted()
    }

    @Test
    fun shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromActivity() {
        grantPermissions(ONE_PERM)

        EasyPermissions.requestPermissions(
            spyActivity,
            RATIONALE,
            TestActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyActivity, never()).afterPermissionGranted()
    }

    @Test
    fun shouldRequestPermissions_whenMissingPermissionAndNotShowRationaleFromActivity() {
        grantPermissions(ONE_PERM)
        showRationale(false, *ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyActivity,
            RATIONALE,
            TestActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyActivity).requestPermissions(ALL_PERMS, TestActivity.REQUEST_CODE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromActivity() {
        grantPermissions(ONE_PERM)
        showRationale(true, *ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyActivity,
            RATIONALE,
            TestActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        assertThat(ShadowAlertDialog.getShownDialogs().size).isEqualTo(1)
        assertThatHasExpectedRationale(ShadowAlertDialog.getLatestDialog(), RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromActivity() {
        grantPermissions(ONE_PERM)
        showRationale(true, *ALL_PERMS)

        val request = PermissionRequest.Builder(spyActivity)
            .theme(R.style.Theme_AppCompat)
            .code(TestActivity.REQUEST_CODE)
            .perms(ALL_PERMS)
            .rationale(android.R.string.unknownName)
            .positiveButtonText(android.R.string.ok)
            .negativeButtonText(android.R.string.cancel)
            .build()
        EasyPermissions.requestPermissions(spyActivity, request)

        assertThat(ShadowAlertDialog.getShownDialogs().size).isEqualTo(1)
        assertThatHasExpectedButtonsAndRationale(
            ShadowAlertDialog.getLatestDialog(),
            android.R.string.unknownName,
            android.R.string.ok,
            android.R.string.cancel
        )
    }

    @Test
    fun shouldHaveSomePermissionDenied_whenShowRationaleFromActivity() {
        showRationale(true, *ALL_PERMS)

        assertThat(EasyPermissions.somePermissionDenied(spyActivity, *ALL_PERMS)).isTrue()
    }

    @Test
    fun shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromActivity() {
        showRationale(false, *ALL_PERMS)

        assertThat(EasyPermissions.somePermissionDenied(spyActivity, *ALL_PERMS)).isFalse()
    }

    @Test
    fun shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromActivity() {
        showRationale(false, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionPermanentlyDenied(
                spyActivity,
                *ALL_PERMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromActivity() {
        showRationale(true, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionPermanentlyDenied(
                spyActivity,
                *ALL_PERMS
            )
        ).isFalse()
    }

    @Test
    fun shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromActivity() {
        showRationale(false, READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyActivity,
                READ_SMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromActivity() {
        showRationale(true, READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyActivity,
                READ_SMS
            )
        ).isFalse()
    }

    @Test
    fun shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromAppCompatActivity() {
        EasyPermissions.onRequestPermissionsResult(
            TestAppCompatActivity.REQUEST_CODE,
            ALL_PERMS,
            SMS_DENIED_RESULT,
            spyAppCompatActivity
        )

        verify(spyAppCompatActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestAppCompatActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(ACCESS_FINE_LOCATION)))

        verify(spyAppCompatActivity)
            .onPermissionsDenied(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestAppCompatActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(READ_SMS)))

        verify(spyAppCompatActivity, never()).afterPermissionGranted()
    }

    @Test
    fun shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromAppCompatActivity() {
        grantPermissions(ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyAppCompatActivity,
            RATIONALE,
            TestAppCompatActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyAppCompatActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        verify(spyAppCompatActivity, never()).requestPermissions(
            any(
                Array<String>::class.java
            ), anyInt()
        )
        assertThat(integerCaptor.value).isEqualTo(TestAppCompatActivity.REQUEST_CODE)
        assertThat(listCaptor.value).containsAtLeastElementsIn(ALL_PERMS)
    }

    @Test
    fun shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFromAppCompatActivity() {
        grantPermissions(ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyAppCompatActivity,
            RATIONALE,
            TestAppCompatActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spyAppCompatActivity, times(2)).afterPermissionGranted()
    }

    @Test
    fun shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromAppCompatActivity() {
        grantPermissions(ONE_PERM)

        EasyPermissions.requestPermissions(
            spyAppCompatActivity,
            RATIONALE,
            TestAppCompatActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyAppCompatActivity, never()).afterPermissionGranted()
    }

    @Test
    fun shouldRequestPermissions_whenMissingPermissionAndNotShowRationaleFromAppCompatActivity() {
        grantPermissions(ONE_PERM)
        showRationale(false, *ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyAppCompatActivity,
            RATIONALE,
            TestAppCompatActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyAppCompatActivity)
            .requestPermissions(ALL_PERMS, TestAppCompatActivity.REQUEST_CODE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromAppCompatActivity() {
        grantPermissions(ONE_PERM)
        showRationale(true, *ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyAppCompatActivity,
            RATIONALE,
            TestAppCompatActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        assertThat(ShadowAlertDialog.getShownDialogs().size).isEqualTo(1)
        assertThatHasExpectedRationale(ShadowAlertDialog.getLatestDialog(), RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromSupportFragmentActivity() {
        grantPermissions(ONE_PERM)
        showRationale(true, *ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyFragmentActivity,
            RATIONALE,
            TestSupportFragmentActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        assertThat(ShadowAlertDialog.getShownDialogs().size).isEqualTo(1)
        assertThatHasExpectedRationale(ShadowAlertDialog.getLatestDialog(), RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromAppCompatActivity() {
        grantPermissions(ONE_PERM)
        showRationale(true, *ALL_PERMS)

        val request = PermissionRequest.Builder(spyAppCompatActivity)
            .theme(R.style.Theme_AppCompat)
            .code(TestAppCompatActivity.REQUEST_CODE)
            .perms(ALL_PERMS)
            .rationale(android.R.string.unknownName)
            .positiveButtonText(android.R.string.ok)
            .negativeButtonText(android.R.string.cancel)
            .build()
        EasyPermissions.requestPermissions(spyAppCompatActivity, request)

        assertThat(ShadowAlertDialog.getShownDialogs().size).isEqualTo(1)
        assertThatHasExpectedButtonsAndRationale(
            ShadowAlertDialog.getLatestDialog(),
            android.R.string.unknownName,
            android.R.string.ok,
            android.R.string.cancel
        )
    }

    @Test
    fun shouldHaveSomePermissionDenied_whenShowRationaleFromAppCompatActivity() {
        showRationale(true, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionDenied(
                spyAppCompatActivity,
                *ALL_PERMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromAppCompatActivity() {
        showRationale(false, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionDenied(
                spyAppCompatActivity,
                *ALL_PERMS
            )
        ).isFalse()
    }

    @Test
    fun shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromAppCompatActivity() {
        showRationale(false, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionPermanentlyDenied(
                spyAppCompatActivity,
                *ALL_PERMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromAppCompatActivity() {
        showRationale(true, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionPermanentlyDenied(
                spyAppCompatActivity,
                *ALL_PERMS
            )
        ).isFalse()
    }

    @Test
    fun shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromAppCompatActivity() {
        showRationale(false, READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyAppCompatActivity,
                READ_SMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromAppCompatActivity() {
        showRationale(true, READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyAppCompatActivity,
                READ_SMS
            )
        ).isFalse()
    }

    @Test
    fun shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromFragment() {
        EasyPermissions.onRequestPermissionsResult(
            TestFragment.REQUEST_CODE,
            ALL_PERMS,
            SMS_DENIED_RESULT,
            spyFragment
        )

        verify(spyFragment)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestFragment.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(ACCESS_FINE_LOCATION)))

        verify(spyFragment)
            .onPermissionsDenied(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestFragment.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(READ_SMS)))

        verify(spyFragment, never()).afterPermissionGranted()
    }

    @Test
    fun shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromFragment() {
        grantPermissions(ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyFragment,
            RATIONALE,
            TestFragment.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyFragment)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        verify(
            spyFragment,
            never()
        ).requestPermissions(any(Array<String>::class.java), anyInt())
        assertThat(integerCaptor.value).isEqualTo(TestFragment.REQUEST_CODE)
        assertThat(listCaptor.value).containsAtLeastElementsIn(ALL_PERMS)
    }

    @Test
    fun shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFragment() {
        grantPermissions(ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyFragment,
            RATIONALE,
            TestFragment.REQUEST_CODE,
            *ALL_PERMS
        )

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spyFragment, times(2)).afterPermissionGranted()
    }

    @Test
    fun shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromFragment() {
        grantPermissions(ONE_PERM)

        EasyPermissions.requestPermissions(
            spyFragment,
            RATIONALE,
            TestFragment.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyFragment, never()).afterPermissionGranted()
    }

    @Test
    fun shouldRequestPermissions_whenMissingPermissionsAndNotShowRationaleFromFragment() {
        grantPermissions(ONE_PERM)
        showRationale(false, *ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyFragment,
            RATIONALE,
            TestFragment.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyFragment).requestPermissions(ALL_PERMS, TestFragment.REQUEST_CODE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM)
        showRationale(true, *ALL_PERMS)

        EasyPermissions.requestPermissions(
            spyFragment,
            RATIONALE,
            TestFragment.REQUEST_CODE,
            *ALL_PERMS
        )

        assertThat(ShadowAlertDialog.getShownDialogs().size).isEqualTo(1)
        assertThatHasExpectedRationale(ShadowAlertDialog.getLatestDialog(), RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM)
        showRationale(true, *ALL_PERMS)

        val request = PermissionRequest.Builder(spyFragment.context)
            .theme(R.style.Theme_AppCompat)
            .code(TestFragment.REQUEST_CODE)
            .perms(ALL_PERMS)
            .rationale(RATIONALE)
            .positiveButtonText(POSITIVE)
            .negativeButtonText(NEGATIVE)
            .build()
        EasyPermissions.requestPermissions(spyFragment, request)

        assertThat(ShadowAlertDialog.getShownDialogs().size).isEqualTo(1)
        assertThatHasExpectedButtonsAndRationale(
            ShadowAlertDialog.getLatestDialog(),
            RATIONALE,
            POSITIVE,
            NEGATIVE
        )
    }

    @Test
    fun shouldHaveSomePermissionDenied_whenShowRationaleFromFragment() {
        showRationale(true, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionDenied(
                spyFragment,
                *ALL_PERMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromFragment() {
        showRationale(false, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionDenied(
                spyFragment,
                *ALL_PERMS
            )
        ).isFalse()
    }

    @Test
    fun shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromFragment() {
        showRationale(false, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionPermanentlyDenied(
                spyFragment,
                *ALL_PERMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromFragment() {
        showRationale(true, *ALL_PERMS)

        assertThat(
            EasyPermissions.somePermissionPermanentlyDenied(
                spyFragment,
                *ALL_PERMS
            )
        ).isFalse()
    }

    @Test
    fun shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromFragment() {
        showRationale(false, READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyFragment,
                READ_SMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromFragment() {
        showRationale(true, READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyFragment,
                READ_SMS
            )
        ).isFalse()
    }

    // ============================================================================================
    //  Private Methods
    // ============================================================================================

    @Suppress("SameParameterValue")
    private fun assertThatHasExpectedButtonsAndRationale(
        dialog: Dialog,
        rationale: Int,
        positive: Int,
        negative: Int
    ) {
        val dialogMessage = dialog.findViewById<TextView>(android.R.id.message)
        assertThat(dialogMessage.text.toString()).isEqualTo(app?.getString(rationale))
        val positiveMessage = dialog.findViewById<TextView>(android.R.id.button1)
        assertThat(positiveMessage.text.toString()).isEqualTo(app?.getString(positive))
        val negativeMessage = dialog.findViewById<TextView>(android.R.id.button2)
        assertThat(negativeMessage.text.toString()).isEqualTo(app?.getString(negative))
    }

    @Suppress("SameParameterValue")
    private fun assertThatHasExpectedButtonsAndRationale(
        dialog: Dialog,
        rationale: String,
        positive: String,
        negative: String
    ) {
        val dialogMessage = dialog.findViewById<TextView>(android.R.id.message)
        assertThat(dialogMessage.text.toString()).isEqualTo(rationale)
        val positiveMessage = dialog.findViewById<TextView>(android.R.id.button1)
        assertThat(positiveMessage.text.toString()).isEqualTo(positive)
        val negativeMessage = dialog.findViewById<TextView>(android.R.id.button2)
        assertThat(negativeMessage.text.toString()).isEqualTo(negative)
    }

    @Suppress("SameParameterValue")
    private fun assertThatHasExpectedRationale(dialog: Dialog, rationale: String) {
        val dialogMessage = dialog.findViewById<TextView>(android.R.id.message)
        assertThat(dialogMessage.text.toString()).isEqualTo(rationale)
    }

    private fun grantPermissions(perms: Array<String>) {
        shadowApp?.grantPermissions(*perms)
    }

    private fun showRationale(show: Boolean, vararg perms: String) {
        for (perm in perms) {
            `when`(spyActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show)
            `when`(spyFragmentActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show)
            `when`(spyAppCompatActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show)
            `when`(spyFragment.shouldShowRequestPermissionRationale(perm)).thenReturn(show)
        }
    }
}
