package pub.devrel.easypermissions;

import android.Manifest;

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
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
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

    private static final int REQUEST_CODE = 5;
    private static final String RATIONALE = "RATIONALE";
    private static final String[] ALL_PERMS = new String[]{
            Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION};

    private TestActivity spyActivity;
    private TestFragment spyFragment;
    private TestSupportFragment spySupportFragment;
    @Captor
    private ArgumentCaptor<Integer> integerCaptor;
    @Captor
    private ArgumentCaptor<ArrayList<String>> listCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        setUpActivityAndFragment();
    }

    @Test
    public void shouldHavePermission_whenHasPermissionsBeforeMarshmallow() {
        assertThat(EasyPermissions.hasPermissions(RuntimeEnvironment.application,
                Manifest.permission.ACCESS_COARSE_LOCATION)).isTrue();
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromActivity() {
        EasyPermissions.requestPermissions(spyActivity, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spyActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromFragment() {
        EasyPermissions.requestPermissions(spyFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestFromSupportedFragment() {
        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    private void setUpActivityAndFragment() {
        TestActivity activity = Robolectric.buildActivity(TestActivity.class)
                .create().start().resume().get();
        TestFragment fragment = Robolectric.buildFragment(TestFragment.class)
                .create().start().resume().get();
        TestSupportFragment supportFragment = SupportFragmentController.of(new TestSupportFragment())
                .create().start().resume().get();

        spyActivity = Mockito.spy(activity);
        spyFragment = Mockito.spy(fragment);
        spySupportFragment = Mockito.spy(supportFragment);
    }
}
