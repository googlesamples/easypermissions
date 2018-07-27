package pub.devrel.easypermissions;

import android.Manifest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentController;

import java.util.ArrayList;

import pub.devrel.easypermissions.testhelper.TestActivity;
import pub.devrel.easypermissions.testhelper.TestFragment;
import pub.devrel.easypermissions.testhelper.TestSupportFragment;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Low-API (SDK = 19) tests for {@link pub.devrel.easypermissions.EasyPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class EasyPermissionsLowApiTest {

    private static final String RATIONALE = "RATIONALE";
    private static final String[] ALL_PERMS = new String[]{
            Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION};

    private TestActivity spyActivity;
    private TestFragment spyFragment;
    private TestSupportFragment spySupportFragment;
    private ActivityController<TestActivity> activityController;
    private FragmentController<TestFragment> fragmentController;
    private SupportFragmentController<TestSupportFragment> supportFragmentController;
    @Captor
    private ArgumentCaptor<Integer> integerCaptor;
    @Captor
    private ArgumentCaptor<ArrayList<String>> listCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        setUpActivityAndFragment();
    }

    @After
    public void tearDown() {
        tearDownActivityAndFragment();
    }

    // ------ General tests ------

    @Test
    public void shouldHavePermission_whenHasPermissionsBeforeMarshmallow() {
        assertThat(EasyPermissions.hasPermissions(RuntimeEnvironment.application,
                Manifest.permission.ACCESS_COARSE_LOCATION)).isTrue();
    }

    // ------ From Activity ------

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromActivity() {
        EasyPermissions.requestPermissions(spyActivity, RATIONALE, TestActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    // ------ From Fragment ------

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromFragment() {
        EasyPermissions.requestPermissions(spyFragment, RATIONALE, TestFragment.REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    // ------ From Support Fragment ------

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromSupportedFragment() {
        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, TestSupportFragment.REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    private void setUpActivityAndFragment() {
        activityController = Robolectric.buildActivity(TestActivity.class)
                .create().start().resume();
        fragmentController = Robolectric.buildFragment(TestFragment.class)
                .create().start().resume();
        supportFragmentController = SupportFragmentController.of(new TestSupportFragment())
                .create().start().resume();

        spyActivity = Mockito.spy(activityController.get());
        spyFragment = Mockito.spy(fragmentController.get());
        spySupportFragment = Mockito.spy(supportFragmentController.get());
    }

    private void tearDownActivityAndFragment() {
        activityController.pause().stop().destroy();
        fragmentController.pause().stop().destroy();
        supportFragmentController.pause().stop().destroy();
    }
}
