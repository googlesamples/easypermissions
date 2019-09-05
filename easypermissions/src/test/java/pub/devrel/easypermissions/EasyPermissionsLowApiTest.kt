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
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pub.devrel.easypermissions.components.TestActivity
import pub.devrel.easypermissions.components.TestAppCompatActivity
import pub.devrel.easypermissions.components.TestFragment
import pub.devrel.easypermissions.components.TestSupportFragmentActivity
import pub.devrel.easypermissions.controllers.ActivityController
import pub.devrel.easypermissions.controllers.FragmentController

import com.google.common.truth.Truth.assertThat
import org.mockito.*
import pub.devrel.easypermissions.facade.EasyPermissions
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import com.nhaarman.mockitokotlin2.capture

private const val RATIONALE = "RATIONALE"
private val ALL_PERMS = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [19])
class EasyPermissionsLowApiTest {

    private lateinit var spyActivity: TestActivity
    private lateinit var spySupportFragmentActivity: TestSupportFragmentActivity
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

        activityController = ActivityController(TestActivity::class.java)
        fragmentActivityController = ActivityController(TestSupportFragmentActivity::class.java)
        appCompatActivityController = ActivityController(TestAppCompatActivity::class.java)
        fragmentController = FragmentController(TestFragment::class.java)

        spyActivity = spy(activityController.resume())
        spySupportFragmentActivity = spy(fragmentActivityController.resume())
        spyAppCompatActivity = spy(appCompatActivityController.resume())
        spyFragment = spy(fragmentController.resume())
    }

    @Test
    fun shouldHavePermission_whenHasPermissionsBeforeMarshmallow() {
        assertThat(
            EasyPermissions.hasPermissions(
                ApplicationProvider.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ).isTrue()
    }

    @Test
    fun shouldCallbackOnPermissionGranted_whenRequestFromActivity() {
        EasyPermissions.requestPermissions(
            spyActivity,
            RATIONALE,
            TestActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyActivity).onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestActivity.REQUEST_CODE)
        assertThat(listCaptor.value).containsAtLeastElementsIn(ALL_PERMS)
    }

    @Test
    fun shouldCallbackOnPermissionGranted_whenRequestFromSupportFragmentActivity() {
        EasyPermissions.requestPermissions(
            spySupportFragmentActivity,
            RATIONALE,
            TestSupportFragmentActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spySupportFragmentActivity).onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestSupportFragmentActivity.REQUEST_CODE)
        assertThat(listCaptor.value).containsAtLeastElementsIn(ALL_PERMS)
    }

    @Test
    fun shouldCallbackOnPermissionGranted_whenRequestFromAppCompatActivity() {
        EasyPermissions.requestPermissions(
            spyAppCompatActivity,
            RATIONALE,
            TestAppCompatActivity.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyAppCompatActivity).onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestAppCompatActivity.REQUEST_CODE)
        assertThat(listCaptor.value).containsAtLeastElementsIn(ALL_PERMS)
    }

    @Test
    fun shouldCallbackOnPermissionGranted_whenRequestFromFragment() {
        EasyPermissions.requestPermissions(
            spyFragment,
            RATIONALE,
            TestFragment.REQUEST_CODE,
            *ALL_PERMS
        )

        verify(spyFragment).onPermissionsGranted(capture(integerCaptor), capture(listCaptor))
        assertThat(integerCaptor.value).isEqualTo(TestFragment.REQUEST_CODE)
        assertThat(listCaptor.value).containsAtLeastElementsIn(ALL_PERMS)
    }
}
