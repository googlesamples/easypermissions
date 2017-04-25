package pub.devrel.easypermissions.sample;

import android.Manifest;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import pub.devrel.easypermissions.EasyPermissions;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Basic Robolectric tests for {@link pub.devrel.easypermissions.EasyPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class EasyPermissionsTest {

    private MainActivity mActivity;
    private ShadowApplication mApplication;

    @Before
    public void setup() {
        mActivity = Robolectric.buildActivity(MainActivity.class).get();
        mApplication = Shadows.shadowOf(mActivity.getApplication());
    }

    @Test
    public void testHasPermissions() {
        Context context = mApplication.getApplicationContext();

        String[] perms = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        // Wes should not have permissions before any are granted
        assertFalse(EasyPermissions.hasPermissions(context, perms));

        // Granting one permission should not make the whole set appear granted
        mApplication.grantPermissions(perms[0]);
        assertFalse(EasyPermissions.hasPermissions(context, perms));

        // Granting all permissions should make the whole set granted
        mApplication.grantPermissions(perms);
        assertTrue(EasyPermissions.hasPermissions(context, perms));
    }
}
