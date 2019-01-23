package pub.devrel.easypermissions;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import androidx.test.core.app.ApplicationProvider;
import pub.devrel.easypermissions.testhelper.ActivityController;
import pub.devrel.easypermissions.testhelper.FragmentController;
import pub.devrel.easypermissions.testhelper.TestActivity;
import pub.devrel.easypermissions.testhelper.TestAppCompatActivity;
import pub.devrel.easypermissions.testhelper.TestFragment;
import pub.devrel.easypermissions.testhelper.TestSupportFragmentActivity;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

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

    private ShadowApplication shadowApp;
    private Application app;
    private TestActivity spyActivity;
    private TestSupportFragmentActivity spySupportFragmentActivity;
    private TestAppCompatActivity spyAppCompatActivity;
    private TestFragment spyFragment;
    private FragmentController<TestFragment> fragmentController;
    private ActivityController<TestActivity> activityController;
    private ActivityController<TestSupportFragmentActivity> supportFragmentActivityController;
    private ActivityController<TestAppCompatActivity> appCompatActivityController;
    @Captor
    private ArgumentCaptor<Integer> integerCaptor;
    @Captor
    private ArgumentCaptor<ArrayList<String>> listCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        app = ApplicationProvider.getApplicationContext();
        shadowApp = shadowOf(app);

        activityController = new ActivityController<>(TestActivity.class);
        supportFragmentActivityController = new ActivityController<>(TestSupportFragmentActivity.class);
        appCompatActivityController = new ActivityController<>(TestAppCompatActivity.class);
        fragmentController = new FragmentController<>(TestFragment.class);

        spyActivity = Mockito.spy(activityController.resume());
        spySupportFragmentActivity = Mockito.spy(supportFragmentActivityController.resume());
        spyAppCompatActivity = Mockito.spy(appCompatActivityController.resume());
        spyFragment = Mockito.spy(fragmentController.resume());
    }

    // ------ General tests ------

    @Test
    public void shouldNotHavePermissions_whenNoPermissionsGranted() {
        assertThat(EasyPermissions.hasPermissions(app, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldNotHavePermissions_whenNotAllPermissionsGranted() {
        shadowApp.grantPermissions(ONE_PERM);
        assertThat(EasyPermissions.hasPermissions(app, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldHavePermissions_whenAllPermissionsGranted() {
        shadowApp.grantPermissions(ALL_PERMS);
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
    public void shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromAppCompatActivity() {
        EasyPermissions.onRequestPermissionsResult(TestAppCompatActivity.REQUEST_CODE, ALL_PERMS, SMS_DENIED_RESULT, spyAppCompatActivity);

        verify(spyAppCompatActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestAppCompatActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)));

        verify(spyAppCompatActivity, times(1))
                .onPermissionsDenied(integerCaptor.capture(), listCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(TestAppCompatActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue())
                .containsAllIn(new ArrayList<>(Collections.singletonList(Manifest.permission.READ_SMS)));

        verify(spyAppCompatActivity, never()).afterPermissionGranted();
    }

    @Test
    public void shouldCallbackOnPermissionGranted_whenRequestAlreadyGrantedPermissionsFromAppCompatActivity() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyAppCompatActivity, RATIONALE, TestAppCompatActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyAppCompatActivity, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spyAppCompatActivity, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(TestAppCompatActivity.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFromAppCompatActivity() {
        grantPermissions(ALL_PERMS);

        EasyPermissions.requestPermissions(spyAppCompatActivity, RATIONALE, TestAppCompatActivity.REQUEST_CODE, ALL_PERMS);

        // Called 2 times because this is a spy and library implementation invokes super classes annotated methods as well
        verify(spyAppCompatActivity, times(2)).afterPermissionGranted();
    }

    @Test
    public void shouldNotCallbackAfterPermissionGranted_whenRequestNotGrantedPermissionsFromAppCompatActivity() {
        grantPermissions(ONE_PERM);

        EasyPermissions.requestPermissions(spyAppCompatActivity, RATIONALE, TestAppCompatActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyAppCompatActivity, never()).afterPermissionGranted();
    }

    @Test
    public void shouldRequestPermissions_whenMissingPermissionAndNotShowRationaleFromAppCompatActivity() {
        grantPermissions(ONE_PERM);
        showRationale(false, ALL_PERMS);

        EasyPermissions.requestPermissions(spyAppCompatActivity, RATIONALE, TestAppCompatActivity.REQUEST_CODE, ALL_PERMS);

        verify(spyAppCompatActivity, times(1))
                .requestPermissions(ALL_PERMS, TestAppCompatActivity.REQUEST_CODE);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromAppCompatActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spyAppCompatActivity, RATIONALE, TestAppCompatActivity.REQUEST_CODE, ALL_PERMS);

        androidx.fragment.app.Fragment dialogFragment = spyAppCompatActivity.getSupportFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @Test
    public void shouldShowCorrectDialog_whenMissingPermissionsAndShowRationaleFromSupportFragmentActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        EasyPermissions.requestPermissions(spySupportFragmentActivity, RATIONALE, TestSupportFragmentActivity.REQUEST_CODE, ALL_PERMS);

        Fragment dialogFragment = spySupportFragmentActivity.getFragmentManager()
                .findFragmentByTag(RationaleDialogFragment.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragment.class);

        Dialog dialog = ((RationaleDialogFragment) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @Test
    public void shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromAppCompatActivity() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        PermissionRequest request = new PermissionRequest.Builder(spyAppCompatActivity, TestAppCompatActivity.REQUEST_CODE, ALL_PERMS)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setRationale(android.R.string.unknownName)
                .setTheme(R.style.Theme_AppCompat)
                .build();
        EasyPermissions.requestPermissions(request);

        androidx.fragment.app.Fragment dialogFragment = spyAppCompatActivity.getSupportFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, android.R.string.unknownName,
                android.R.string.ok, android.R.string.cancel);
    }

    @Test
    public void shouldHaveSomePermissionDenied_whenShowRationaleFromAppCompatActivity() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spyAppCompatActivity, ALL_PERMS)).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionDenied_whenNotShowRationaleFromAppCompatActivity() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionDenied(spyAppCompatActivity, ALL_PERMS)).isFalse();
    }

    @Test
    public void shouldHaveSomePermissionPermanentlyDenied_whenNotShowRationaleFromAppCompatActivity() {
        showRationale(false, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spyAppCompatActivity, Arrays.asList(ALL_PERMS))).isTrue();
    }

    @Test
    public void shouldNotHaveSomePermissionPermanentlyDenied_whenShowRationaleFromAppCompatActivity() {
        showRationale(true, ALL_PERMS);

        assertThat(EasyPermissions.somePermissionPermanentlyDenied(spyAppCompatActivity, Arrays.asList(ALL_PERMS))).isFalse();
    }

    @Test
    public void shouldHavePermissionPermanentlyDenied_whenNotShowRationaleFromAppCompatActivity() {
        showRationale(false, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spyAppCompatActivity, Manifest.permission.READ_SMS)).isTrue();
    }

    @Test
    public void shouldNotHavePermissionPermanentlyDenied_whenShowRationaleFromAppCompatActivity() {
        showRationale(true, Manifest.permission.READ_SMS);

        assertThat(EasyPermissions.permissionPermanentlyDenied(spyAppCompatActivity, Manifest.permission.READ_SMS)).isFalse();
    }

    @Test
    public void shouldCorrectlyCallback_whenOnRequestPermissionResultCalledFromFragment() {
        EasyPermissions.onRequestPermissionsResult(TestFragment.REQUEST_CODE, ALL_PERMS, SMS_DENIED_RESULT,
                spyFragment);

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

        EasyPermissions.requestPermissions(spyFragment, RATIONALE,
                TestFragment.REQUEST_CODE, ALL_PERMS);

        verify(spyFragment, times(1))
                .onPermissionsGranted(integerCaptor.capture(), listCaptor.capture());
        verify(spyFragment, never()).requestPermissions(any(String[].class), anyInt());
        assertThat(integerCaptor.getValue()).isEqualTo(TestFragment.REQUEST_CODE);
        assertThat(listCaptor.getValue()).containsAllIn(ALL_PERMS);
    }

    @Test
    public void shouldCallbackAfterPermissionGranted_whenRequestAlreadyGrantedPermissionsFragment() {
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

        androidx.fragment.app.Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedRationale(dialog, RATIONALE);
    }

    @Test
    public void shouldShowCorrectDialogUsingRequest_whenMissingPermissionsAndShowRationaleFromFragment() {
        grantPermissions(ONE_PERM);
        showRationale(true, ALL_PERMS);

        PermissionRequest request = new PermissionRequest.Builder(spyFragment, TestFragment.REQUEST_CODE, ALL_PERMS)
                .setPositiveButtonText(POSITIVE)
                .setNegativeButtonText(NEGATIVE)
                .setRationale(RATIONALE)
                .setTheme(R.style.Theme_AppCompat)
                .build();
        EasyPermissions.requestPermissions(request);

        androidx.fragment.app.Fragment dialogFragment = spyFragment.getChildFragmentManager()
                .findFragmentByTag(RationaleDialogFragmentCompat.TAG);
        assertThat(dialogFragment).isInstanceOf(RationaleDialogFragmentCompat.class);

        Dialog dialog = ((RationaleDialogFragmentCompat) dialogFragment).getDialog();
        assertThatHasExpectedButtonsAndRationale(dialog, RATIONALE, POSITIVE, NEGATIVE);
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

    private void grantPermissions(String[] perms) {
        shadowApp.grantPermissions(perms);
    }

    private void showRationale(boolean show, String... perms) {
        for (String perm : perms) {
            when(spyActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spySupportFragmentActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spyAppCompatActivity.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
            when(spyFragment.shouldShowRequestPermissionRationale(perm)).thenReturn(show);
        }
    }
}
