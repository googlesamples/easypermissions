package pub.devrel.easypermissions;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.widget.TextView;

import org.junit.After;
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
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.support.v4.SupportFragmentController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import pub.devrel.easypermissions.testhelper.TestActivity;
import pub.devrel.easypermissions.testhelper.TestFragment;
import pub.devrel.easypermissions.testhelper.TestSupportActivity;
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

    private static final String RATIONALE = "RATIONALE";
    private static final String POSITIVE = "POSITIVE";
    private static final String NEGATIVE = "NEGATIVE";
    private static final String[] ONE_PERM = new String[]{Manifest.permission.READ_SMS};
    private static final String[] ALL_PERMS = new String[]{
            Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int[] SMS_DENIED_RESULT = new int[]{
            PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED};
    private Application app;
    private TestActivity spyActivity;
    private TestSupportActivity spySupportActivity;
    private TestFragment spyFragment;
    private TestSupportFragment spySupportFragment;
    private ActivityController<TestActivity> activityController;
    private ActivityController<TestSupportActivity> supportActivityController;
    private FragmentController<TestFragment> fragmentController;
    private SupportFragmentController<TestSupportFragment> supportFragmentController;
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

    @After
    public void tearDown() {
        tearDownActivityAndFragment();
    }

    // ------ General tests ------

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

    @SuppressWarnings("ConstantConditions")
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

    // ------ From Activity ------

    @Test
    public void shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromActivity() {
        EasyPermissions.onRequestPermissionsResult(TestActivity.REQUEST_CODE, ALL_PERMS, SMS_DENIED_RESULT, spyActivity);

        verify(spyActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)));

        verify(spyActivity, times(1))
                .onPermissionsDenied(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.READ_SMS)));

        verify(spyActivity, never()).afterPermissionGranted();
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromActivity() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, TestActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spyActivity, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(TestActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFromActivity() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, TestActivity.REQUEST_CODE, ALL_PERMS);

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spyActivity, times(2)).afterPermissionGranted();
    }

    @Test
    public void shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromActivity() {
        grantPermissions(ONE_PERM);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, TestActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyActivity, never()).afterPermissionGranted();
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionAndNotShowRationaleFromActivity() {
        grantPermissions(ONE_PERM);
        showRationale(false, ALL_PERMS);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, TestActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyActivity, times(1))
                .requestPermissions(ALL_PERMS, TestActivity.REQUEST_CODE);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, TestActivity.REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyActivity.getFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldShowCorrectDialogUsingDeprecated_whenMissingPermissionsAndShowRationaleFromActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spyActivity, RATIONALE, android.R.string.ok,
                android.R.string.cancel, TestActivity.REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyActivity.getFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        PermissionRequest request = new PermissionRequest.Builder(spyActivity, TestActivity.REQUEST_CODE, ALL_PERMS)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setRationale(android.R.string.unknownName)
                .setTheme(R.style.Theme_AppCompat)
                .build();
        EasyPermissions.requestPermissions(request);

        Fragment dialogFragment = spyActivity.getFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, android.R.string.unknownName,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldHaveSomePermissionDenied_whenShowRationaleFromActivity() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spyActivity, ALL_PERMS)).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromActivity() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spyActivity, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromActivity() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spyActivity, Arrays.asList(ALL_PERMS))).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromActivity() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spyActivity, Arrays.asList(ALL_PERMS))).isFalse();
    }

    @Test
    public void shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromActivity() {
        showRationale(false, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spyActivity, Manifest.permission.READ_SMS)).isTrue();
    }

    @Test
    public void shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromActivity() {
        showRationale(true, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spyActivity, Manifest.permission.READ_SMS)).isFalse();
    }

    @Test
    public void shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromSupportActivity() {
        EasyPermissions.onRequestPermissionsResult(TestSupportActivity.REQUEST_CODE, ALL_PERMS, SMS_DENIED_RESULT, spySupportActivity);

        verify(spySupportActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)));

        verify(spySupportActivity, times(1))
                .onPermissionsDenied(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.READ_SMS)));

        verify(spySupportActivity, never()).afterPermissionGranted();
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromSupportActivity() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportActivity, RATIONALE, TestSupportActivity.REQUEST_CODE, ALL_PERMS);

        verify(spySupportActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spySupportActivity, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFromSupportActivity() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportActivity, RATIONALE, TestSupportActivity.REQUEST_CODE, ALL_PERMS);

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spySupportActivity, times(2)).afterPermissionGranted();
    }

    @Test
    public void shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromSupportActivity() {
        grantPermissions(ONE_PERM);

        EasyPermissions.requestPermissions(spySupportActivity, RATIONALE, TestSupportActivity.REQUEST_CODE, ALL_PERMS);

        verify(spySupportActivity, never()).afterPermissionGranted();
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionAndNotShowRationaleFromSupportActivity() {
        grantPermissions(ONE_PERM);
        showRationale(false, ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportActivity, RATIONALE, TestSupportActivity.REQUEST_CODE, ALL_PERMS);

        verify(spySupportActivity, times(1))
                .requestPermissions(ALL_PERMS, TestSupportActivity.REQUEST_CODE);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromSupportActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportActivity, RATIONALE, TestSupportActivity.REQUEST_CODE, ALL_PERMS);

        android.support.v4.app.Fragment dialogFragment = spySupportActivity.getSupportFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldShowCorrectDialogUsingDeprecated_whenMissingPermissionsAndShowRationaleFromSupportActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportActivity, RATIONALE, android.R.string.ok,
                android.R.string.cancel, TestSupportActivity.REQUEST_CODE, ALL_PERMS);

        android.support.v4.app.Fragment dialogFragment = spySupportActivity.getSupportFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromSupportActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        PermissionRequest request = new PermissionRequest.Builder(spySupportActivity, TestSupportActivity.REQUEST_CODE, ALL_PERMS)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setRationale(android.R.string.unknownName)
                .setTheme(R.style.Theme_AppCompat)
                .build();
        EasyPermissions.requestPermissions(request);

        android.support.v4.app.Fragment dialogFragment = spySupportActivity.getSupportFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, android.R.string.unknownName,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldHaveSomePermissionDenied_whenShowRationaleFromSupportActivity() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spySupportActivity, ALL_PERMS)).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromSupportActivity() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spySupportActivity, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromSupportActivity() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spySupportActivity, Arrays.asList(ALL_PERMS))).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromSupportActivity() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spySupportActivity, Arrays.asList(ALL_PERMS))).isFalse();
    }

    @Test
    public void shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromSupportActivity() {
        showRationale(false, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spySupportActivity, Manifest.permission.READ_SMS)).isTrue();
    }

    @Test
    public void shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromSupportActivity() {
        showRationale(true, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spySupportActivity, Manifest.permission.READ_SMS)).isFalse();
    }

    // ------ From Fragment ------

    @Test
    public void shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromFragment() {
        EasyPermissions.onRequestPermissionsResult(TestFragment.REQUEST_CODE, ALL_PERMS, SMS_DENIED_RESULT, spyFragment);

        verify(spyFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)));

        verify(spyFragment, times(1))
                .onPermissionsDenied(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.READ_SMS)));

        verify(spyFragment, never()).afterPermissionGranted();
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromFragment() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, TestFragment.REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spyFragment, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(TestFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFromFragment() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, TestFragment.REQUEST_CODE, ALL_PERMS);

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spyFragment, times(2)).afterPermissionGranted();
    }

    @Test
    public void shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromFragment() {
        grantPermissions(ONE_PERM);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, TestFragment.REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, never()).afterPermissionGranted();
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionsAndNotShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(false, ALL_PERMS);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, TestFragment.REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1))
                .requestPermissions(ALL_PERMS, TestFragment.REQUEST_CODE);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, TestFragment.REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldShowCorrectDialogUsingDeprecated_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spyFragment, RATIONALE, android.R.string.ok,
                android.R.string.cancel, TestFragment.REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        PermissionRequest request = new PermissionRequest.Builder(spyFragment, TestFragment.REQUEST_CODE, ALL_PERMS)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setRationale(android.R.string.unknownName)
                .setTheme(R.style.Theme_AppCompat)
                .build();
        EasyPermissions.requestPermissions(request);

        Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, android.R.string.unknownName,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldHaveSomePermissionDenied_whenShowRationaleFromFragment() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spyFragment, ALL_PERMS)).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromFragment() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spyFragment, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromFragment() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spyFragment, Arrays.asList(ALL_PERMS))).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromFragment() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spyFragment, Arrays.asList(ALL_PERMS))).isFalse();
    }

    @Test
    public void shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromFragment() {
        showRationale(false, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spyFragment, Manifest.permission.READ_SMS)).isTrue();
    }

    @Test
    public void shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromFragment() {
        showRationale(true, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spyFragment, Manifest.permission.READ_SMS)).isFalse();
    }

    // ------ From Support Fragment  ------

    @Test
    public void shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromSupportFragment() {
        EasyPermissions.onRequestPermissionsResult(TestSupportFragment.REQUEST_CODE, ALL_PERMS, SMS_DENIED_RESULT, spySupportFragment);

        verify(spySupportFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)));

        verify(spySupportFragment, times(1))
                .onPermissionsDenied(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.READ_SMS)));

        verify(spySupportFragment, never()).afterPermissionGranted();
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromSupportFragment() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE,
                TestSupportFragment.REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spySupportFragment, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(TestSupportFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsSupportFragment() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, TestSupportFragment.REQUEST_CODE, ALL_PERMS);

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spySupportFragment, times(2)).afterPermissionGranted();
    }

    @Test
    public void shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromSupportFragment() {
        grantPermissions(ONE_PERM);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, TestSupportFragment.REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragment, never()).afterPermissionGranted();
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionsAndNotShowRationaleFromSupportFragment() {
        grantPermissions(ONE_PERM);
        showRationale(false, ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, TestSupportFragment.REQUEST_CODE, ALL_PERMS);

        verify(spySupportFragment, times(1))
                .requestPermissions(ALL_PERMS, TestSupportFragment.REQUEST_CODE);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldShowCorrectDialogUsingDeprecated_whenMissingPermissionsAndShowRationaleFromSupportFragment() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, android.R.string.ok,
                android.R.string.cancel, TestSupportFragment.REQUEST_CODE, ALL_PERMS);

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
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportFragment, RATIONALE, TestSupportFragment.REQUEST_CODE, ALL_PERMS);

        android.support.v4.app.Fragment dialogFragment = spySupportFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @Test
    public void shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromSupportFragment() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        PermissionRequest request = new PermissionRequest.Builder(spySupportFragment, TestSupportFragment.REQUEST_CODE, ALL_PERMS)
                .setPositiveButtonText(POSITIVE)
                .setNegativeButtonText(NEGATIVE)
                .setRationale(RATIONALE)
                .setTheme(R.style.Theme_AppCompat)
                .build();
        EasyPermissions.requestPermissions(request);

        android.support.v4.app.Fragment dialogFragment = spySupportFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE, POSITIVE, NEGATIVE);
    }

    @Test
    public void shouldHaveSomePermissionDenied_whenShowRationaleFromSupportFragment() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spySupportFragment, ALL_PERMS)).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromSupportFragment() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spySupportFragment, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromSupportFragment() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spySupportFragment, Arrays.asList(ALL_PERMS))).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromSupportFragment() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spySupportFragment, Arrays.asList(ALL_PERMS))).isFalse();
    }


    @Test
    public void shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromSupportFragment() {
        showRationale(false, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spySupportFragment, Manifest.permission.READ_SMS)).isTrue();
    }

    @Test
    public void shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromSupportFragment() {
        showRationale(true, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spySupportFragment, Manifest.permission.READ_SMS)).isFalse();
    }

    private void assertThatHasExpectedButtonsAndRationale(Dialog dialog, int rationale,
                                                          int positive, int negative) {
        TextView dialogMessage = dialog.findViewById(android.R.id.message);
        assertThat(dialogMessage.getText().toString()).isEqualTo(app.getString(rationale));
        TextView positiveMessage = dialog.findViewById(android.R.id.button1);
        assertThat(positiveMessage.getText().toString()).isEqualTo(app.getString(positive));
        TextView negativeMessage = dialog.findViewById(android.R.id.button2);
        assertThat(negativeMessage.getText().toString()).isEqualTo(app.getString(negative));
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

    private void assertThatHasExpectedButtonsAndRationale(Dialog dialog, String rationale,
                                                          String positive, String negative) {
        TextView dialogMessage = dialog.findViewById(android.R.id.message);
        assertThat(dialogMessage.getText().toString()).isEqualTo(rationale);
        TextView positiveMessage = dialog.findViewById(android.R.id.button1);
        assertThat(positiveMessage.getText().toString()).isEqualTo(positive);
        TextView negativeMessage = dialog.findViewById(android.R.id.button2);
        assertThat(negativeMessage.getText().toString()).isEqualTo(negative);
    }

    private void assertThatHasExpectedRationale(Dialog dialog, String rationale) {
        TextView dialogMessage = dialog.findViewById(android.R.id.message);
        assertThat(dialogMessage.getText().toString()).isEqualTo(rationale);
    }

    private void setUpActivityAndFragment() {
        activityController = Robolectric.buildActivity(TestActivity.class)
                .create().start().resume();
        supportActivityController = Robolectric.buildActivity(TestSupportActivity.class)
                .create().start().resume();
        fragmentController = Robolectric.buildFragment(TestFragment.class)
                .create().start().resume();
        supportFragmentController = SupportFragmentController.of(new TestSupportFragment())
                .create().start().resume();

        spyActivity = Mockito.spy(activityController.get());
        spySupportActivity = Mockito.spy(supportActivityController.get());
        spyFragment = Mockito.spy(fragmentController.get());
        spySupportFragment = Mockito.spy(supportFragmentController.get());
    }

    private void tearDownActivityAndFragment() {
        activityController.pause().stop().destroy();
        supportActivityController.pause().stop().destroy();
        fragmentController.pause().stop().destroy();
        supportFragmentController.pause().stop().destroy();
    }

    private void grantPermissions(String[] perms) {
        ShadowApplication.getInstance().grantPermissions(perms);
    }

    private void showRationale(boolean show, String... perms) {
        for (String perm : perms) {
            when(spyActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spySupportActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spyFragment.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spySupportFragment.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
        }
    }
}
