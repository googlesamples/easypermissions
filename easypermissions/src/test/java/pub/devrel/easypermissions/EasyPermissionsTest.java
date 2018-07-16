package pub.devrel.easypermissions;

import android.Manifest;
import android.app.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static com.google.common.truth.Truth.assertThat;

/**
 * Basic Robolectric tests for {@link pub.devrel.easypermissions.EasyPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class EasyPermissionsTest {

    private Application app;
    private String[] perms;

    @Before
    public void setUp() {
        app = RuntimeEnvironment.application;
        perms = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    @Test
    public void shouldNotHavePermissions_whenNoPermissionsGranted() {
        assertThat(EasyPermissions.hasPermissions(app, perms)).isFalse();
    }


    @Test
    public void shouldNotHavePermissions_whenNotAllPermissionsGranted() {
        ShadowApplication.getInstance().grantPermissions(perms[0]);
        assertThat(EasyPermissions.hasPermissions(app, perms)).isFalse();
    }

    @Test
    public void shouldHavePermissions_whenAllPermissionsGranted() {
        ShadowApplication.getInstance().grantPermissions(perms);
        assertThat(EasyPermissions.hasPermissions(app, perms)).isTrue();
    }
}
