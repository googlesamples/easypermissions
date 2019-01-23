package pub.devrel.easypermissions;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowIntent;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ApplicationProvider;
import pub.devrel.easypermissions.testhelper.ActivityController;
import pub.devrel.easypermissions.testhelper.FragmentController;
import pub.devrel.easypermissions.testhelper.TestActivity;
import pub.devrel.easypermissions.testhelper.TestFragment;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static pub.devrel.easypermissions.AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class AppSettingsDialogTest {

    private static final String TITLE = "TITLE";
    private static final String RATIONALE = "RATIONALE";
    private static final String NEGATIVE = "NEGATIVE";
    private static final String POSITIVE = "POSITIVE";
    private ShadowApplication shadowApp;
    private TestActivity spyActivity;
    private TestFragment spyFragment;
    private FragmentController<TestFragment> fragmentController;
    private ActivityController<TestActivity> activityController;
    @Mock
    private DialogInterface.OnClickListener positiveListener;
    @Mock
    private DialogInterface.OnClickListener negativeListener;
    @Captor
    private ArgumentCaptor<Integer> integerCaptor;
    @Captor
    private ArgumentCaptor<Intent> intentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        shadowApp = shadowOf((Application) ApplicationProvider.getApplicationContext());

        activityController = new ActivityController<>(TestActivity.class);
        fragmentController = new FragmentController<>(TestFragment.class);

        spyActivity = Mockito.spy(activityController.resume());
        spyFragment = Mockito.spy(fragmentController.resume());
    }

    // ------ From Activity ------

    @Test
    public void shouldShowExpectedSettingsDialog_whenBuildingFromActivity() {
        new AppSettingsDialog.Builder(spyActivity)
                .setTitle(android.R.string.dialog_alert_title)
                .setRationale(android.R.string.unknownName)
                .setPositiveButton(android.R.string.ok)
                .setNegativeButton(android.R.string.cancel)
                .setThemeResId(R.style.Theme_AppCompat)
                .build()
                .show();

        verify(spyActivity, times(1))
                .startActivityForResult(intentCaptor.capture(), integerCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(DEFAULT_SETTINGS_REQ_CODE);
        assertThat(Objects.requireNonNull(intentCaptor.getValue().getComponent()).getClassName())
                .isEqualTo(AppSettingsDialogHolderActivity.class.getName());

        Intent startedIntent = shadowApp.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getIntentClass()).isEqualTo(AppSettingsDialogHolderActivity.class);
    }

    @Test
    public void shouldPositiveListener_whenClickingPositiveButtonFromActivity() {
        AlertDialog alertDialog = new AppSettingsDialog.Builder(spyActivity)
                .setTitle(TITLE)
                .setRationale(RATIONALE)
                .setPositiveButton(POSITIVE)
                .setNegativeButton(NEGATIVE)
                .setThemeResId(R.style.Theme_AppCompat)
                .build()
                .showDialog(positiveListener, negativeListener);
        Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.performClick();

        verify(positiveListener, times(1))
                .onClick(any(DialogInterface.class), anyInt());
    }

    @Test
    public void shouldNegativeListener_whenClickingPositiveButtonFromActivity() {
        AlertDialog alertDialog = new AppSettingsDialog.Builder(spyActivity)
                .setTitle(TITLE)
                .setRationale(RATIONALE)
                .setPositiveButton(POSITIVE)
                .setNegativeButton(NEGATIVE)
                .setThemeResId(R.style.Theme_AppCompat)
                .build()
                .showDialog(positiveListener, negativeListener);
        Button positive = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        positive.performClick();

        verify(negativeListener, times(1))
                .onClick(any(DialogInterface.class), anyInt());
    }

    @Test
    public void shouldShowExpectedSettingsDialog_whenBuildingFromSupportFragment() {
        new AppSettingsDialog.Builder(spyFragment)
                .setTitle(android.R.string.dialog_alert_title)
                .setRationale(android.R.string.unknownName)
                .setPositiveButton(android.R.string.ok)
                .setNegativeButton(android.R.string.cancel)
                .setThemeResId(R.style.Theme_AppCompat)
                .build()
                .show();

        verify(spyFragment, times(1))
                .startActivityForResult(intentCaptor.capture(), integerCaptor.capture());
        assertThat(integerCaptor.getValue()).isEqualTo(DEFAULT_SETTINGS_REQ_CODE);
        assertThat(Objects.requireNonNull(intentCaptor.getValue().getComponent()).getClassName())
                .isEqualTo(AppSettingsDialogHolderActivity.class.getName());

        Intent startedIntent = shadowApp.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getIntentClass()).isEqualTo(AppSettingsDialogHolderActivity.class);
    }

    @Test
    public void shouldPositiveListener_whenClickingPositiveButtonFromSupportFragment() {
        AlertDialog alertDialog = new AppSettingsDialog.Builder(spyFragment)
                .setTitle(TITLE)
                .setRationale(RATIONALE)
                .setPositiveButton(POSITIVE)
                .setNegativeButton(NEGATIVE)
                .setThemeResId(R.style.Theme_AppCompat)
                .build()
                .showDialog(positiveListener, negativeListener);
        Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.performClick();

        verify(positiveListener, times(1))
                .onClick(any(DialogInterface.class), anyInt());
    }

    @Test
    public void shouldNegativeListener_whenClickingPositiveButtonFromSupportFragment() {
        AlertDialog alertDialog = new AppSettingsDialog.Builder(spyFragment)
                .setTitle(TITLE)
                .setRationale(RATIONALE)
                .setPositiveButton(POSITIVE)
                .setNegativeButton(NEGATIVE)
                .setThemeResId(R.style.Theme_AppCompat)
                .build()
                .showDialog(positiveListener, negativeListener);
        Button positive = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        positive.performClick();

        verify(negativeListener, times(1))
                .onClick(any(DialogInterface.class), anyInt());
    }

}
