package pub.devrel.easypermissions;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.widget.TextView;

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

    private static final int REQUEST_CODE = 5;
    private static final String RATIONALE = "RATIONALE";
    private static final String[] ONE_PERM = new String[]{Manifest.permission.READ_SMS};
    private static final String[] ALL_PERMS = new String[]{
            Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION};

    private Application app;
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
        app = RuntimeEnvironment.application;
    }

    @Test
    public void shouldNotHavePermissions_whenNoPermissionsGranted() {
        assertThat(EasyPermissions.hasPermissions(app, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldNotHavePermissions_whenNotAllPermissionsGranted() {
        ShadowApplication.getInstance().grantPermissions(ONE_PERM);
        assertThat(EasyPermissions.hasPermissions(app, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldHavePermissions_whenAllPermissionsGranted() {
        ShadowApplication.getInstance().grantPermissions(ALL_PERMS);
        assertThat(EasyPermissions.hasPermissions(app, ALL_PERMS)).isTrue();
    }

    @Test
    public void shouldThrowException_whenHasPermissionsWithNullContext() {
        try {
            EasyPermissions.hasPermissions(null, ALL_PERMS);
            fail("IllegalStateException expected because of null context.");
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat()
                    .isEqualTo("Can't check permissions for null context");
        }
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromActivity() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spyActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spyActivity, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionAndNotShowRationaleFromActivity() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, false);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spyActivity, times(1)).requestPermissions(ALL_PERMS, REQUEST_CODE);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromActivity() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, true);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyActivity.getFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @Test
    public void shouldShowCorrectDialogUsingDeprecated_whenMissingPermissionsAndShowRationaleFromActivity() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, true);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, android.R.string.ok,
                android.R.string.cancel, REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyActivity.getFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromFragment() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spyFragment, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionsAndNotShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, false);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1)).requestPermissions(ALL_PERMS, REQUEST_CODE);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, true);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @Test
    public void shouldShowCorrectDialogUsingDeprecated_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, true);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, android.R.string.ok,
                android.R.string.cancel, REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromSupportFragment() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spySupportFragment, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionsAndNotShowRationaleFromSupportFragment() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, false);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragment, times(1)).requestPermissions(ALL_PERMS, REQUEST_CODE);
    }

    @Test
    public void shouldShowCorrectDialogUsingDeprecated_whenMissingPermissionsAndShowRationaleFromSupportFragment() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, true);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, android.R.string.ok,
                android.R.string.cancel, REQUEST_CODE, ALL_PERMS);

        android.support.v4.app.Fragment dialogFragment = spySupportFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromSupportFragment() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, true);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, REQUEST_CODE, ALL_PERMS);

        android.support.v4.app.Fragment dialogFragment = spySupportFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @Test
    public void shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromSupportFragment() {
        grantPermissions(ONE_PERM);
        showRationale(ONE_PERM, true);

        PermissionRequest request = new PermissionRequest.Builder(spyFragment, REQUEST_CODE, ALL_PERMS)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setRationale(RATIONALE)
                .setTheme(R.style.Theme_AppCompat)
                .build();
        EasyPermissions.requestPermissions(request);

        Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE,
                android.R.string.ok, android.R.string.cancel);
    }

    private void assertThatHasExpectedButtonsAndRationale(Dialog dialog, String rationale,
                                                          int positive, int negative) {
        TextView dialogMessage = dialog.findViewById(android.R.id.message);
        assertThat(dialogMessage.getText().toString()).isEqualTo(rationale);
        TextView positiveMessage = dialog.findViewById(android.R.id.button1);
        assertThat(positiveMessage.getText().toString()).isEqualTo(app.getString(positive));
        TextView negativeMessage = dialog.findViewById(android.R.id.button2);
        assertThat(negativeMessage.getText().toString()).isEqualTo(app.getString(negative));

    }

    private void assertThatHasExpectedRationale(Dialog dialog, String rationale) {
        TextView dialogMessage = dialog.findViewById(android.R.id.message);
        assertThat(dialogMessage.getText().toString()).isEqualTo(rationale);
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

    private void grantPermissions(String[] perms) {
        ShadowApplication.getInstance().grantPermissions(perms);
    }

    private void showRationale(String[] perms, boolean show) {
        for (String perm : perms) {
            when(spyActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spyFragment.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spySupportFragment.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
        }
    }
}
