package pub.devrel.easypermissions;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import androidx.fragment.app.Fragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class RationaleDialogClickListenerTest {

    private static final int REQUEST_CODE = 5;
    private static final String[] PERMS = new String[]{
            Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION};
    @Mock
    private RationaleDialogFragment dialogFragment;
    @Mock
    private RationaleDialogFragmentCompat dialogFragmentCompat;
    @Mock
    private RationaleDialogConfig dialogConfig;
    @Mock
    private EasyPermissions.PermissionCallbacks permissionCallbacks;
    @Mock
    private EasyPermissions.RationaleCallbacks rationaleCallbacks;
    @Mock
    private DialogInterface dialogInterface;
    @Mock
    private Activity activity;
    @Mock
    private Fragment fragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(dialogFragment.getActivity()).thenReturn(activity);
        dialogConfig.requestCode = REQUEST_CODE;
        dialogConfig.permissions = PERMS;
    }

    @Test
    public void shouldOnRationaleAccepted_whenPositiveButtonWithRationaleCallbacks() {
        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragment, dialogConfig,
                permissionCallbacks, rationaleCallbacks);
        listener.onClick(dialogInterface, Dialog.BUTTON_POSITIVE);

        verify(rationaleCallbacks, times(1)).onRationaleAccepted(REQUEST_CODE);
    }

    @Test
    public void shouldNotOnRationaleAccepted_whenPositiveButtonWithoutRationaleCallbacks() {
        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragment, dialogConfig,
                permissionCallbacks, null);
        listener.onClick(dialogInterface, Dialog.BUTTON_POSITIVE);

        verify(rationaleCallbacks, never()).onRationaleAccepted(anyInt());
    }

    @Test
    public void shouldRequestPermissions_whenPositiveButtonFromActivity() {
        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragment, dialogConfig,
                permissionCallbacks, rationaleCallbacks);
        listener.onClick(dialogInterface, Dialog.BUTTON_POSITIVE);

        verify(activity, times(1)).requestPermissions(PERMS, REQUEST_CODE);
    }

    @Test
    public void shouldRequestPermissions_whenPositiveButtonFromFragment() {
        when(dialogFragmentCompat.getParentFragment()).thenReturn(fragment);

        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragmentCompat, dialogConfig,
                permissionCallbacks, rationaleCallbacks);
        listener.onClick(dialogInterface, Dialog.BUTTON_POSITIVE);

        verify(fragment, times(1)).requestPermissions(PERMS, REQUEST_CODE);
    }

    @Test
    public void shouldOnRationaleDenied_whenNegativeButtonWithRationaleCallbacks() {
        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragment, dialogConfig,
                permissionCallbacks, rationaleCallbacks);
        listener.onClick(dialogInterface, Dialog.BUTTON_NEGATIVE);

        verify(rationaleCallbacks, times(1)).onRationaleDenied(REQUEST_CODE);
    }

    @Test
    public void shouldNotOnRationaleDenied_whenNegativeButtonWithoutRationaleCallbacks() {
        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragment, dialogConfig,
                permissionCallbacks, null);
        listener.onClick(dialogInterface, Dialog.BUTTON_NEGATIVE);

        verify(rationaleCallbacks, never()).onRationaleDenied(anyInt());
    }

    @Test
    public void shouldOnPermissionsDenied_whenNegativeButtonWithPermissionCallbacks() {
        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragment, dialogConfig,
                permissionCallbacks, rationaleCallbacks);
        listener.onClick(dialogInterface, Dialog.BUTTON_NEGATIVE);

        verify(permissionCallbacks, times(1))
                .onPermissionsDenied(REQUEST_CODE, Arrays.asList(PERMS));
    }

    @Test
    public void shouldNotOnPermissionsDenied_whenNegativeButtonWithoutPermissionCallbacks() {
        RationaleDialogClickListener listener = new RationaleDialogClickListener(dialogFragment, dialogConfig,
                null, rationaleCallbacks);
        listener.onClick(dialogInterface, Dialog.BUTTON_NEGATIVE);

        verify(permissionCallbacks, never()).onPermissionsDenied(anyInt(), ArgumentMatchers.<String>anyList());
    }
}
