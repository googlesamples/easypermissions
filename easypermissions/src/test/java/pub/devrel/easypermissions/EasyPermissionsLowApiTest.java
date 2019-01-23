package pub.devrel.easypermissions;

import android.Manifest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import androidx.test.core.app.ApplicationProvider;
import pub.devrel.easypermissions.testhelper.ActivityController;
import pub.devrel.easypermissions.testhelper.FragmentController;
import pub.devrel.easypermissions.testhelper.TestActivity;
import pub.devrel.easypermissions.testhelper.TestAppCompatActivity;
import pub.devrel.easypermissions.testhelper.TestFragment;
import pub.devrel.easypermissions.testhelper.TestSupportFragmentActivity;

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
    private TestSupportFragmentActivity spySupportFragmentActivity;
    private TestAppCompatActivity spyAppCompatActivity;
    private TestFragment spyFragment;
    private FragmentController<TestFragment> fragmentController;
    private ActivityController<TestActivity> activityController;
    private ActivityController<TestSupportFragmentActivity> supportFragmentActivityController;
    private ActivityController<TestAppCompatActivity> appCompatActivityController;
    @Captor
    private ArgumentCaptor<Integer> integerCaptor;
    @Captor
    private ArgumentCaptor<ArrayList<String>> listCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        activityController = new ActivityController<>(TestActivity.class);
        supportFragmentActivityController = new ActivityController<>(TestSupportFragmentActivity.class);
        appCompatActivityController = new ActivityController<>(TestAppCompatActivity.class);
        fragmentController = new FragmentController<>(TestFragment.class);

        spyActivity = Mockito.spy(activityController.resume());
        spySupportFragmentActivity = Mockito.spy(supportFragmentActivityController.resume());
        spyAppCompatActivity = Mockito.spy(appCompatActivityController.resume());
        spyFragment = Mockito.spy(fragmentController.resume());
    }

    // ------ General tests ------

    @Test
    public void shouldHavePermission_whenHasPermissionsBeforeMarshmallow() {
        assertThat(EasyPermissions.hasPermissions(ApplicationProvider.getApplicationContext(),
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

    // ------ From Support Activity ------

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromSupportFragmentActivity() {
        EasyPermissions.requestPermissions(spySupportFragmentActivity, RATIONALE, TestSupportFragmentActivity.REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragmentActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportFragmentActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }


    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromAppCompatActivity() {
        EasyPermissions.requestPermissions(spyAppCompatActivity, RATIONALE, TestAppCompatActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyAppCompatActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestAppCompatActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromFragment() {
        EasyPermissions.requestPermissions(spyFragment, RATIONALE, TestFragment.REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

}
