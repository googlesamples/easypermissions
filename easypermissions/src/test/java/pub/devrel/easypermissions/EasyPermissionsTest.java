package pub.devrel.easypermissions;

import android.Manifest;
import android.app.Application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Basic Robolectric tests for {@link pub.devrel.easypermissions.EasyPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class EasyPermissionsTest {
    @Test
    public void testHasPermissions() {
        Application app = RuntimeEnvironment.application;

        String[] perms = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        // Wes should not have permissions before any are granted
        assertFalse(EasyPermissions.hasPermissions(app, perms));

        // Granting one permission should not make the whole set appear granted
        ShadowApplication.getInstance().grantPermissions(perms[0]);
        assertFalse(EasyPermissions.hasPermissions(app, perms));

        // Granting all permissions should make the whole set granted
        ShadowApplication.getInstance().grantPermissions(perms);
        assertTrue(EasyPermissions.hasPermissions(app, perms));
    }
}
