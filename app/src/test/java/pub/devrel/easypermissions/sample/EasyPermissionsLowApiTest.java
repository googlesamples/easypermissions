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

import static junit.framework.Assert.assertTrue;

/**
 * Low-API (SDK = 19) tests for {@link pub.devrel.easypermissions.EasyPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class EasyPermissionsLowApiTest {

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

        // On low-API devices, we should always get 'true' when we call 'hasPermissions'
        assertTrue(EasyPermissions.hasPermissions(context,
                Manifest.permission.ACCESS_COARSE_LOCATION));
    }
}
