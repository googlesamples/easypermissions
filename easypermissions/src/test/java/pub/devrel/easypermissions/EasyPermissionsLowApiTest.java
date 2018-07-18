package pub.devrel.easypermissions;

import android.Manifest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

/**
 * Low-API (SDK = 19) tests for {@link pub.devrel.easypermissions.EasyPermissions}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class EasyPermissionsLowApiTest {

    @Test
    public void shouldHavePermission_whenHasPermissionsBeforeMarshmallow() {
        // On low-API devices, we should always get 'true' when we call 'hasPermissions'
        assertThat(EasyPermissions.hasPermissions(RuntimeEnvironment.application,
                Manifest.permission.ACCESS_COARSE_LOCATION)).isTrue();
    }

    //TODO: call all needed methods and expect IllegalStateException
}
