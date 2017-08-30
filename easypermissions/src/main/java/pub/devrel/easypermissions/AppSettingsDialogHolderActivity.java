package pub.devrel.easypermissions;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RestrictTo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AppSettingsDialogHolderActivity extends AppCompatActivity implements DialogInterface.OnClickListener {
    private static final int APP_SETTINGS_RC = 7534;

    private AlertDialog mDialog;

    public static Intent createShowDialogIntent(Context context, AppSettingsDialog dialog) {
        return new Intent(context, AppSettingsDialogHolderActivity.class)
                .putExtra(AppSettingsDialog.EXTRA_APP_SETTINGS, dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = AppSettingsDialog.fromIntent(getIntent(), this).showDialog(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == Dialog.BUTTON_POSITIVE) {
            startActivityForResult(
                    new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", getPackageName(), null)),
                    APP_SETTINGS_RC);
        } else if (which == Dialog.BUTTON_NEGATIVE) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else {
            throw new IllegalStateException("Unknown button type: " + which);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode, data);
        finish();
    }
}
