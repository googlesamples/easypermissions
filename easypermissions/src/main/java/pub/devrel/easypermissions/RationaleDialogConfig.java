package pub.devrel.easypermissions;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

/**
 * Configuration for either {@link RationaleDialogFragment} or {@link RationaleDialogFragmentCompat}.
 */
class RationaleDialogConfig {

    private static final String KEY_POSITIVE_BUTTON = "positiveButton";
    private static final String KEY_NEGATIVE_BUTTON = "negativeButton";
    private static final String KEY_RATIONALE_MESSAGE = "rationaleMsg";
    private static final String KEY_THEME = "theme";
    private static final String KEY_REQUEST_CODE = "requestCode";
    private static final String KEY_PERMISSIONS = "permissions";

    String positiveButton;
    String negativeButton;
    int theme;
    int requestCode;
    String rationaleMsg;
    String[] permissions;

    RationaleDialogConfig(@NonNull String positiveButton,
                          @NonNull String negativeButton,
                          @NonNull String rationaleMsg,
                          @StyleRes int theme,
                          int requestCode,
                          @NonNull String[] permissions) {

        this.positiveButton = positiveButton;
        this.negativeButton = negativeButton;
        this.rationaleMsg = rationaleMsg;
        this.theme = theme;
        this.requestCode = requestCode;
        this.permissions = permissions;
    }

    RationaleDialogConfig(Bundle bundle) {
        positiveButton = bundle.getString(KEY_POSITIVE_BUTTON);
        negativeButton = bundle.getString(KEY_NEGATIVE_BUTTON);
        rationaleMsg = bundle.getString(KEY_RATIONALE_MESSAGE);
        theme = bundle.getInt(KEY_THEME);
        requestCode = bundle.getInt(KEY_REQUEST_CODE);
        permissions = bundle.getStringArray(KEY_PERMISSIONS);
    }

    Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_POSITIVE_BUTTON, positiveButton);
        bundle.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        bundle.putString(KEY_RATIONALE_MESSAGE, rationaleMsg);
        bundle.putInt(KEY_THEME, theme);
        bundle.putInt(KEY_REQUEST_CODE, requestCode);
        bundle.putStringArray(KEY_PERMISSIONS, permissions);

        return bundle;
    }

    AlertDialog createSupportDialog(Context context, Dialog.OnClickListener listener) {
        AlertDialog.Builder builder;
        if (theme > 0) {
            builder = new AlertDialog.Builder(context, theme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        return builder
                .setCancelable(false)
                .setPositiveButton(positiveButton, listener)
                .setNegativeButton(negativeButton, listener)
                .setMessage(rationaleMsg)
                .create();
    }

    android.app.AlertDialog createFrameworkDialog(Context context, Dialog.OnClickListener listener) {
        android.app.AlertDialog.Builder builder;
        if (theme > 0) {
            builder = new android.app.AlertDialog.Builder(context, theme);
        } else {
            builder = new android.app.AlertDialog.Builder(context);
        }
        return builder
                .setCancelable(false)
                .setPositiveButton(positiveButton, listener)
                .setNegativeButton(negativeButton, listener)
                .setMessage(rationaleMsg)
                .create();
    }

}
