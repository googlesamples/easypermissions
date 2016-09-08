package pub.devrel.easypermissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

/**
 * Dialog to prompt the user to go to the app's settings screen and enable permissions. If the
 * user clicks 'OK' on the dialog, they are sent to the settings screen. The result is returned
 * to the Activity via {@link Activity#onActivityResult(int, int, Intent)}.
 *
 * Use {@link Builder} to create and display a dialog.
 */
public class AppSettingsDialog {

    public static final int DEFAULT_SETTINGS_REQ_CODE = 16061;

    private AlertDialog mAlertDialog;

    private AppSettingsDialog(@NonNull final Object activityOrFragment,
                              @NonNull final Context context,
                              @NonNull String rationale,
                              @Nullable String title,
                              @Nullable String positiveButton,
                              @Nullable String negativeButton,
                              @Nullable DialogInterface.OnClickListener negativeListener,
                              int requestCode) {

        // Create empty builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        // Set rationale
        dialogBuilder.setMessage(rationale);

        // Set title
        dialogBuilder.setTitle(title);

        // Positive button text, or default
        String positiveButtonText = TextUtils.isEmpty(positiveButton) ?
                context.getString(android.R.string.ok) : positiveButton;

        // Negative button text, or default
        String negativeButtonText = TextUtils.isEmpty(positiveButton) ?
                context.getString(android.R.string.cancel) : negativeButton;

        // Request code, or default
        final int settingsRequestCode = requestCode > 0 ? requestCode : DEFAULT_SETTINGS_REQ_CODE;

        // Positive click listener, launches app screen
        dialogBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Create app settings intent
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);

                // Start for result
                startForResult(activityOrFragment, intent, settingsRequestCode);
            }
        });

        // Negative click listener, dismisses dialog
        dialogBuilder.setNegativeButton(negativeButtonText, negativeListener);

        // Build dialog
        mAlertDialog = dialogBuilder.create();
    }

    @TargetApi(11)
    private void startForResult(Object object, Intent intent, int requestCode) {
        if (object instanceof Activity) {
            ((Activity) object).startActivityForResult(intent, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).startActivityForResult(intent, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Display the built dialog.
     */
    public void show() {
        mAlertDialog.show();
    }

    /**
     * Builder for an {@link AppSettingsDialog}.
     */
    public static class Builder {

        private Object mActivityOrFragment;
        private Context mContext;
        private String mRationale;
        private String mTitle;
        private String mPositiveButton;
        private String mNegativeButton;
        private DialogInterface.OnClickListener mNegativeListener;
        private int mRequestCode = -1;

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         * @param activity the Activity in which to display the dialog.
         * @param rationale text explaining why the user should launch the app settings screen.
         */
        public Builder(@NonNull Activity activity, @NonNull String rationale) {
            mActivityOrFragment = activity;
            mContext = activity;
            mRationale = rationale;
        }

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         * @param fragment the Fragment in which to display the dialog.
         * @param rationale text explaining why the user should launch the app settings screen.
         */
        public Builder(@NonNull android.support.v4.app.Fragment fragment, @NonNull String rationale) {
            mActivityOrFragment = fragment;
            mContext = fragment.getContext();
            mRationale = rationale;
        }

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         * @param fragment the Fragment in which to display the dialog.
         * @param rationale text explaining why the user should launch the app settings screen.
         */
        @TargetApi(11)
        public Builder(@NonNull android.app.Fragment fragment, @NonNull String rationale) {
            mActivityOrFragment = fragment;
            mContext = fragment.getActivity();
            mRationale = rationale;
        }


        /**
         * Set the title dialog. Default is no title.
         */
        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        /**
         * Set the positive button text, default is {@code android.R.string.ok}.
         */
        public Builder setPositiveButton(String positiveButton) {
            mPositiveButton = positiveButton;
            return this;
        }

        /**
         * Set the negative button text and click listener, default text is
         * {@code android.R.string.cancel}.
         */
        public Builder setNegativeButton(String negativeButton,
                                         DialogInterface.OnClickListener negativeListener) {
            mNegativeButton = negativeButton;
            mNegativeListener = negativeListener;
            return this;
        }

        /**
         * Set the request code use when launching the Settings screen for result, can be
         * retrieved in the calling Activity's {@code onActivityResult} method. Default is
         * {@link #DEFAULT_SETTINGS_REQ_CODE}.
         */
        public Builder setRequestCode(int requestCode) {
            mRequestCode = requestCode;
            return this;
        }

        /**
         * Build the {@link AppSettingsDialog} from the specified options. Generally followed by a
         * call to {@link AppSettingsDialog#show()}.
         */
        public AppSettingsDialog build() {
            return new AppSettingsDialog(mActivityOrFragment, mContext, mRationale, mTitle,
                    mPositiveButton, mNegativeButton, mNegativeListener, mRequestCode);
        }

    }

}
