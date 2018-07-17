package pub.devrel.easypermissions;

import android.Manifest;
import android.app.Application;
import android.app.Fragment;

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

import java.util.ArrayList;

import pub.devrel.easypermissions.testhelper.TestActivity;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Basic Robolectric tests for {@link pub.devrel.easypermissions.EasyPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class EasyPermissionsTest {

    private static final int REQUEST_CODE = 10;
    private static final String RATIONALE = "some rationale";
    private Application app;
    private String[] allPerms;
    private String[] onePerm;
    private TestActivity spyActivity;
    @Captor private ArgumentCaptor<Integer> integerCaptor;
    @Captor private ArgumentCaptor<ArrayList<String>> listCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        app = RuntimeEnvironment.application;
        spyActivity = Mockito.spy(Robolectric.buildActivity(TestActivity.class).get());
        allPerms = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        onePerm = new String[]{
                Manifest.permission.READ_SMS,
        };
    }

    @Test
    public void shouldNotHavePermissions_whenNoPermissionsGranted() {
        assertThat(EasyPermissions.hasPermissions(app, allPerms)).isFalse();
    }

    @Test
    public void shouldNotHavePermissions_whenNotAllPermissionsGranted() {
        ShadowApplication.getInstance().grantPermissions(onePerm);
        assertThat(EasyPermissions.hasPermissions(app, allPerms)).isFalse();
    }

    @Test
    public void shouldHavePermissions_whenAllPermissionsGranted() {
        ShadowApplication.getInstance().grantPermissions(allPerms);
        assertThat(EasyPermissions.hasPermissions(app, allPerms)).isTrue();
    }

    @Test
    public void shouldThrowException_whenHasPermissionsWithNullContext() {
        try {
            EasyPermissions.hasPermissions(null, allPerms);
            fail("IllegalStateException expected because of null context.");
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat()
                    .isEqualTo("Can't check permissions for null context");
        }
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermission() {
        grantPermissions(allPerms);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, REQUEST_CODE, allPerms);

        verify(spyActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spyActivity, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(allPerms);
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionsAndShouldNotShowRationale() {
        grantPermissions(onePerm);
        showRationale(onePerm, false);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, REQUEST_CODE, allPerms);

        verify(spyActivity, times(1)).requestPermissions(allPerms, REQUEST_CODE);
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionsAndShouldShowRationale() {
        grantPermissions(onePerm);
        showRationale(onePerm, true);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, REQUEST_CODE, allPerms);

        Fragment dialog = spyActivity.getFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialog).isInstanceOf(RationaleDialogFragment.class);
    }

    private void grantPermissions(String[] perms) {
        ShadowApplication.getInstance().grantPermissions(perms);
    }

    private void showRationale(String[] perms, boolean shouldShow) {
        for (String perm : perms) {
            when(spyActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(shouldShow);
        }
    }
}
