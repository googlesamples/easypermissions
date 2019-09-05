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
package pub.devrel.easypermissions

import android.Manifest
import android.app.Application
import android.app.Dialog
import android.content.pm.PackageManager
import android.widget.TextView
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
import pub.devrel.easypermissions.components.TestActivity
import pub.devrel.easypermissions.components.TestAppCompatActivity
import pub.devrel.easypermissions.components.TestFragment
import pub.devrel.easypermissions.components.TestSupportFragmentActivity
import pub.devrel.easypermissions.controllers.ActivityController
import pub.devrel.easypermissions.controllers.FragmentController
import java.util.*
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.capture
import junit.framework.Assert.fail
import org.mockito.Mockito.*
import org.robolectric.Shadows.shadowOf
import pub.devrel.easypermissions.facade.EasyPermissions

private const val RATIONALE = "RATIONALE"
private const val NEGATIVE = "NEGATIVE"
private val ONE_PERM = arrayOf(Manifest.permission.READ_SMS)
private val ALL_PERMS =
    arrayOf(Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION)
private val SMS_DENIED_RESULT =
    intArrayOf(PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
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

        verify<TestActivity>(spyActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(Manifest.permission.ACCESS_FINE_LOCATION)))

        verify<TestActivity>(spyActivity)
            .onPermissionsDenied(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(Manifest.permission.READ_SMS)))

        verify<TestActivity>(spyActivity, never()).afterPermissionGranted()
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

        verify<TestActivity>(spyActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        verify<TestActivity>(
            spyActivity,
            never()
        ).requestPermissions(any<Array<String>>(Array<String>::class.java), anyInt())
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
        verify<TestActivity>(spyActivity, times(2)).afterPermissionGranted()
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

        verify<TestActivity>(spyActivity, never()).afterPermissionGranted()
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

        verify<TestActivity>(spyActivity).requestPermissions(ALL_PERMS, TestActivity.REQUEST_CODE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromActivity() {
//        grantPermissions(ONE_PERM)
//        showRationale(true, *ALL_PERMS)
//
//        EasyPermissions.requestPermissions(
//            spyActivity,
//            RATIONALE,
//            TestActivity.REQUEST_CODE,
//            *ALL_PERMS
//        )
//
//        val dialogFragment = spyActivity!!.getFragmentManager()
//            .findFragmentByTag(RationaleDialogFragment.TAG)
//        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment::class.java!!)
//
//        val dialog = (dialogFragment as RationaleDialogFragment).getDialog()
//        assertThatHasExpectedRationale(dialog, RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromActivity() {
//        grantPermissions(ONE_PERM)
//        showRationale(true, *ALL_PERMS)
//
//        val request = PermissionRequest.Builder(spyActivity, TestActivity.REQUEST_CODE, ALL_PERMS)
//            .setPositiveButtonText(android.R.string.ok)
//            .setNegativeButtonText(android.R.string.cancel)
//            .setRationale(android.R.string.unknownName)
//            .setTheme(R.style.Theme_AppCompat)
//            .build()
//        EasyPermissions.requestPermissions(request)
//
//        val dialogFragment = spyActivity!!.getFragmentManager()
//            .findFragmentByTag(RationaleDialogFragment.TAG)
//        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment::class.java!!)
//
//        val dialog = (dialogFragment as RationaleDialogFragment).getDialog()
//        assertThatHasExpectedButtonsAndRationale(
//            dialog, android.R.string.unknownName,
//            android.R.string.ok, android.R.string.cancel
//        )
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
        showRationale(false, Manifest.permission.READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyActivity,
                Manifest.permission.READ_SMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromActivity() {
        showRationale(true, Manifest.permission.READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyActivity,
                Manifest.permission.READ_SMS
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

        verify<TestAppCompatActivity>(spyAppCompatActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestAppCompatActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(Manifest.permission.ACCESS_FINE_LOCATION)))

        verify<TestAppCompatActivity>(spyAppCompatActivity)
            .onPermissionsDenied(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestAppCompatActivity.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(Manifest.permission.READ_SMS)))

        verify<TestAppCompatActivity>(spyAppCompatActivity, never()).afterPermissionGranted()
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

        verify<TestAppCompatActivity>(spyAppCompatActivity)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        verify<TestAppCompatActivity>(spyAppCompatActivity, never()).requestPermissions(
            any<Array<String>>(
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
        verify<TestAppCompatActivity>(spyAppCompatActivity, times(2)).afterPermissionGranted()
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

        verify<TestAppCompatActivity>(spyAppCompatActivity, never()).afterPermissionGranted()
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

        verify<TestAppCompatActivity>(spyAppCompatActivity)
            .requestPermissions(ALL_PERMS, TestAppCompatActivity.REQUEST_CODE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromAppCompatActivity() {
//        grantPermissions(ONE_PERM)
//        showRationale(true, *ALL_PERMS)
//
//        EasyPermissions.requestPermissions(
//            spyAppCompatActivity,
//            RATIONALE,
//            TestAppCompatActivity.REQUEST_CODE,
//            ALL_PERMS
//        )
//
//        val dialogFragment = spyAppCompatActivity!!.getSupportFragmentManager()
//            .findFragmentByTag(RationaleDialogFragmentCompat.TAG)
//        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat::class.java!!)
//
//        val dialog = (dialogFragment as RationaleDialogFragmentCompat).getDialog()
//        assertThatHasExpectedRationale(dialog, RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromSupportFragmentActivity() {
//        grantPermissions(ONE_PERM)
//        showRationale(true, *ALL_PERMS)
//
//        EasyPermissions.requestPermissions(
//            spyFragmentActivity,
//            RATIONALE,
//            TestSupportFragmentActivity.REQUEST_CODE,
//            ALL_PERMS
//        )
//
//        val dialogFragment = spyFragmentActivity!!.getFragmentManager()
//            .findFragmentByTag(RationaleDialogFragment.TAG)
//        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment::class.java!!)
//
//        val dialog = (dialogFragment as RationaleDialogFragment).getDialog()
//        assertThatHasExpectedRationale(dialog, RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromAppCompatActivity() {
//        grantPermissions(ONE_PERM)
//        showRationale(true, *ALL_PERMS)
//
//        val request = PermissionRequest.Builder(
//            spyAppCompatActivity,
//            TestAppCompatActivity.REQUEST_CODE,
//            ALL_PERMS
//        )
//            .setPositiveButtonText(android.R.string.ok)
//            .setNegativeButtonText(android.R.string.cancel)
//            .setRationale(android.R.string.unknownName)
//            .setTheme(R.style.Theme_AppCompat)
//            .build()
//        EasyPermissions.requestPermissions(request)
//
//        val dialogFragment = spyAppCompatActivity!!.getSupportFragmentManager()
//            .findFragmentByTag(RationaleDialogFragmentCompat.TAG)
//        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat::class.java!!)
//
//        val dialog = (dialogFragment as RationaleDialogFragmentCompat).getDialog()
//        assertThatHasExpectedButtonsAndRationale(
//            dialog, android.R.string.unknownName,
//            android.R.string.ok, android.R.string.cancel
//        )
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
        showRationale(false, Manifest.permission.READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyAppCompatActivity,
                Manifest.permission.READ_SMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromAppCompatActivity() {
        showRationale(true, Manifest.permission.READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyAppCompatActivity,
                Manifest.permission.READ_SMS
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

        verify<TestFragment>(spyFragment)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestFragment.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(Manifest.permission.ACCESS_FINE_LOCATION)))

        verify<TestFragment>(spyFragment)
            .onPermissionsDenied(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestFragment.REQUEST_CODE)
        assertThat(listCaptor.value)
            .containsAtLeastElementsIn(ArrayList(listOf(Manifest.permission.READ_SMS)))

        verify<TestFragment>(spyFragment, never()).afterPermissionGranted()
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

        verify<TestFragment>(spyFragment)
            .onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        verify<TestFragment>(
            spyFragment,
            never()
        ).requestPermissions(any<Array<String>>(Array<String>::class.java), anyInt())
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
        verify<TestFragment>(spyFragment, times(2)).afterPermissionGranted()
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

        verify<TestFragment>(spyFragment, never()).afterPermissionGranted()
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

        verify<TestFragment>(spyFragment).requestPermissions(ALL_PERMS, TestFragment.REQUEST_CODE)
    }

    @Test
    fun shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromFragment() {
        // TODO

//        grantPermissions(ONE_PERM)
//        showRationale(true, *ALL_PERMS)
//
//        EasyPermissions.requestPermissions(
//            spyFragment,
//            RATIONALE,
//            TestFragment.REQUEST_CODE,
//            *ALL_PERMS
//        )
//        
//
//        val dialogFragment = spyFragment.childFragmentManager
//            .findFragmentByTag(RationaleDialogFragmentCompat.TAG)
//        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat::class.java!!)
//
//        val dialog = (dialogFragment as RationaleDialogFragmentCompat).getDialog()
//        assertThatHasExpectedRationale(dialog, RATIONALE)
    }

    @Test
    fun shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromFragment() {
        // TODO

//        grantPermissions(ONE_PERM)
//        showRationale(true, *ALL_PERMS)
//
//        val request = PermissionRequest.Builder(spyFragment, TestFragment.REQUEST_CODE, ALL_PERMS)
//            .setPositiveButtonText(POSITIVE)
//            .setNegativeButtonText(NEGATIVE)
//            .setRationale(RATIONALE)
//            .setTheme(R.style.Theme_AppCompat)
//            .build()
//        EasyPermissions.requestPermissions(request)
//
//        val dialogFragment = spyFragment!!.getChildFragmentManager()
//            .findFragmentByTag(RationaleDialogFragmentCompat.TAG)
//        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat::class.java!!)
//
//        val dialog = (dialogFragment as RationaleDialogFragmentCompat).getDialog()
//        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE, POSITIVE, NEGATIVE)
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
        showRationale(false, Manifest.permission.READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyFragment,
                Manifest.permission.READ_SMS
            )
        ).isTrue()
    }

    @Test
    fun shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromFragment() {
        showRationale(true, Manifest.permission.READ_SMS)

        assertThat(
            EasyPermissions.permissionPermanentlyDenied(
                spyFragment,
                Manifest.permission.READ_SMS
            )
        ).isFalse()
    }

    // ============================================================================================
    //  Private Methods
    // ============================================================================================

    private fun assertThatHasExpectedButtonsAndRationale(
        dialog: Dialog,
        rationale: Int,
        positive: Int,
        negative: Int
    ) {
        val dialogMessage = dialog.findViewById<TextView>(android.R.id.message)
        assertThat(dialogMessage.text.toString()).isEqualTo(app!!.getString(rationale))
        val positiveMessage = dialog.findViewById<TextView>(android.R.id.button1)
        assertThat(positiveMessage.text.toString()).isEqualTo(app!!.getString(positive))
        val negativeMessage = dialog.findViewById<TextView>(android.R.id.button2)
        assertThat(negativeMessage.text.toString()).isEqualTo(app!!.getString(negative))
    }

    private fun assertThatHasExpectedButtonsAndRationale(
        dialog: Dialog,
        rationale: String,
        positive: Int,
        negative: Int
    ) {
        val dialogMessage = dialog.findViewById<TextView>(android.R.id.message)
        assertThat(dialogMessage.text.toString()).isEqualTo(rationale)
        val positiveMessage = dialog.findViewById<TextView>(android.R.id.button1)
        assertThat(positiveMessage.text.toString()).isEqualTo(app!!.getString(positive))
        val negativeMessage = dialog.findViewById<TextView>(android.R.id.button2)
        assertThat(negativeMessage.text.toString()).isEqualTo(app!!.getString(negative))
    }

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
