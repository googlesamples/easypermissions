package pub.devrel.easypermissions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

/**
 * Dialog to prompt the user to go to the app's settings screen and enable permissions. If the
 * user clicks 'OK' on the dialog, they are sent to the settings screen. The result is returned
 * to the Activity via {@link Activity#onActivityResult(int, int, Intent)}.
 * <p>
 * Use the {@link Builder} to create and display a dialog.
 */
public class AppSettingsDialog implements Parcelable, DialogInterface.OnClickListener {
    public static final int DEFAULT_SETTINGS_REQ_CODE = 16061;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static final Parcelable.Creator<AppSettingsDialog> CREATOR = new Parcelable.Creator<AppSettingsDialog>() {
        @Override
        public AppSettingsDialog createFromParcel(Parcel in) {
            return new AppSettingsDialog(in);
        }

        @Override
        public AppSettingsDialog[] newArray(int size) {
            return new AppSettingsDialog[size];
        }
    };

    static final String EXTRA_APP_SETTINGS = "extra_app_settings";

    private final String mRationale;
    private final String mTitle;
    private final String mPositiveButtonText;
    private final String mNegativeButtonText;
    private final int mRequestCode;
    private Context mContext;
    private Object mActivityOrFragment;
    private DialogInterface.OnClickListener mNegativeListener;

    private AppSettingsDialog(Parcel in) {
        mRationale = in.readString();
        mTitle = in.readString();
        mPositiveButtonText = in.readString();
        mNegativeButtonText = in.readString();
        mRequestCode = in.readInt();
    }

    private AppSettingsDialog(@NonNull final Object activityOrFragment,
                              @NonNull final Context context,
                              @Nullable String rationale,
                              @Nullable String title,
                              @Nullable String positiveButtonText,
                              @Nullable String negativeButtonText,
                              @Nullable DialogInterface.OnClickListener negativeListener,
                              int requestCode) {
        mActivityOrFragment = activityOrFragment;
        mContext = context;
        mRationale = rationale;
        mTitle = title;
        mPositiveButtonText = positiveButtonText;
        mNegativeButtonText = negativeButtonText;
        mNegativeListener = negativeListener;
        mRequestCode = requestCode;
    }

    void setActivityOrFragment(Object activityOrFragment) {
        mActivityOrFragment = activityOrFragment;
    }

    void setContext(Context context) {
        mContext = context;
    }

    void setNegativeListener(DialogInterface.OnClickListener negativeListener) {
        mNegativeListener = negativeListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void startForResult(Intent intent) {
        if (mActivityOrFragment instanceof Activity) {
            ((Activity) mActivityOrFragment).startActivityForResult(intent, mRequestCode);
        } else if (mActivityOrFragment instanceof Fragment) {
            ((Fragment) mActivityOrFragment).startActivityForResult(intent, mRequestCode);
        } else if (mActivityOrFragment instanceof android.app.Fragment) {
            ((android.app.Fragment) mActivityOrFragment).startActivityForResult(intent,
                                                                                mRequestCode);
        }
    }

    /**
     * Display the built dialog.
     */
    public void show() {
        if (mNegativeListener == null) {
            //noinspection NewApi The Builder constructor prevents this
            startForResult(AppSettingsDialogHolderActivity.createShowDialogIntent(mContext, this));
        } else {
            // We can't pass the cancel listener to an activity so we default to old behavior it there is one.
            // This ensures backwards compatibility.
            showDialog();
        }
    }

    /**
     * Show the dialog. {@link #show()} is a wrapper to ensure backwards compatibility
     */
    void showDialog() {
        new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setTitle(mTitle)
                .setMessage(mRationale)
                .setPositiveButton(mPositiveButtonText, this)
                .setNegativeButton(mNegativeButtonText, mNegativeListener)
                .create()
                .show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Create app settings intent
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
        intent.setData(uri);

        // Start for result
        //noinspection NewApi The Builder constructor prevents this
        startForResult(intent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mRationale);
        dest.writeString(mTitle);
        dest.writeString(mPositiveButtonText);
        dest.writeString(mNegativeButtonText);
        dest.writeInt(mRequestCode);
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
         *
         * @param activity  the {@link Activity} in which to display the dialog.
         * @param rationale text explaining why the user should launch the app settings screen.
         * @deprecated Use {@link #Builder(Activity)} with {@link #setRationale(String)} or {@link
         * #setRationale(int)}.
         */
        @Deprecated
        public Builder(@NonNull Activity activity, @NonNull String rationale) {
            mActivityOrFragment = activity;
            mContext = activity;
            mRationale = rationale;
        }

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         *
         * @param fragment  the {@link Fragment} in which to display the dialog.
         * @param rationale text explaining why the user should launch the app settings screen.
         * @deprecated Use {@link #Builder(Fragment)} with {@link #setRationale(String)} or {@link
         * #setRationale(int)}.
         */
        @Deprecated
        public Builder(@NonNull Fragment fragment, @NonNull String rationale) {
            mActivityOrFragment = fragment;
            mContext = fragment.getContext();
            mRationale = rationale;
        }

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         *
         * @param fragment  the {@link android.app.Fragment} in which to display the dialog.
         * @param rationale text explaining why the user should launch the app settings screen.
         * @deprecated Use {@link #Builder(android.app.Fragment)} with {@link #setRationale(String)}
         * or {@link #setRationale(int)}.
         */
        @Deprecated
        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        public Builder(@NonNull android.app.Fragment fragment, @NonNull String rationale) {
            mActivityOrFragment = fragment;
            mContext = fragment.getActivity();
            mRationale = rationale;
        }

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         *
         * @param activity the {@link Activity} in which to display the dialog.
         */
        public Builder(@NonNull Activity activity) {
            mActivityOrFragment = activity;
            mContext = activity;
        }

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         *
         * @param fragment the {@link Fragment} in which to display the dialog.
         */
        public Builder(@NonNull Fragment fragment) {
            mActivityOrFragment = fragment;
            mContext = fragment.getContext();
        }

        /**
         * Create a new Builder for an {@link AppSettingsDialog}.
         *
         * @param fragment the {@link android.app.Fragment} in which to display the dialog.
         */
        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        public Builder(@NonNull android.app.Fragment fragment) {
            mActivityOrFragment = fragment;
            mContext = fragment.getActivity();
        }


        /**
         * Set the title dialog. Default is "Permissions Required".
         */
        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        /**
         * Set the title dialog. Default is "Permissions Required".
         */
        public Builder setTitle(@StringRes int title) {
            mTitle = mContext.getString(title);
            return this;
        }

        /**
         * Set the rationale dialog. Default is
         * "This app may not work correctly without the requested permissions.
         * Open the app settings screen to modify app permissions."
         */
        public Builder setRationale(String rationale) {
            mRationale = rationale;
            return this;
        }

        /**
         * Set the rationale dialog. Default is
         * "This app may not work correctly without the requested permissions.
         * Open the app settings screen to modify app permissions."
         */
        public Builder setRationale(@StringRes int rationale) {
            mRationale = mContext.getString(rationale);
            return this;
        }

        /**
         * Set the positive button text, default is {@link android.R.string#ok}.
         */
        public Builder setPositiveButton(String positiveButton) {
            mPositiveButton = positiveButton;
            return this;
        }

        /**
         * Set the positive button text, default is {@link android.R.string#ok}.
         */
        public Builder setPositiveButton(@StringRes int positiveButton) {
            mPositiveButton = mContext.getString(positiveButton);
            return this;
        }

        /**
         * Set the negative button text and click listener, default text is
         * {@link android.R.string#cancel}.
         *
         * @deprecated To set the title of the cancel button, use {@link #setNegativeButton(String)}.
         * <p>
         * To know if a user cancelled the request, check if your permissions were given with {@link
         * EasyPermissions#hasPermissions(Context, String...)} in {@link
         * Activity#onActivityResult(int, int, Intent)}. If you still don't have the right
         * permissions, then the request was cancelled.
         */
        @Deprecated
        public Builder setNegativeButton(String negativeButton,
                                         DialogInterface.OnClickListener negativeListener) {
            mNegativeButton = negativeButton;
            mNegativeListener = negativeListener;
            return this;
        }

        /**
         * Set the negative button text, default is {@link android.R.string#cancel}.
         */
        public Builder setNegativeButton(String negativeButton) {
            mNegativeButton = negativeButton;
            return this;
        }

        /**
         * Set the negative button text, default is {@link android.R.string#cancel}.
         */
        public Builder setNegativeButton(@StringRes int negativeButton) {
            mNegativeButton = mContext.getString(negativeButton);
            return this;
        }

        /**
         * Set the request code use when launching the Settings screen for result, can be retrieved
         * in the calling Activity's {@link Activity#onActivityResult(int, int, Intent)} method.
         * Default is {@link #DEFAULT_SETTINGS_REQ_CODE}.
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
            mRationale = TextUtils.isEmpty(mRationale) ?
                    mContext.getString(R.string.rationale_ask_again) : mRationale;
            mTitle = TextUtils.isEmpty(mTitle) ?
                    mContext.getString(R.string.title_settings_dialog) : mTitle;
            mPositiveButton = TextUtils.isEmpty(mPositiveButton) ?
                    mContext.getString(android.R.string.ok) : mPositiveButton;
            mNegativeButton = TextUtils.isEmpty(mNegativeButton) ?
                    mContext.getString(android.R.string.cancel) : mNegativeButton;
            mRequestCode = mRequestCode > 0 ? mRequestCode : DEFAULT_SETTINGS_REQ_CODE;

            return new AppSettingsDialog(
                    mActivityOrFragment,
                    mContext,
                    mRationale,
                    mTitle,
                    mPositiveButton,
                    mNegativeButton,
                    mNegativeListener,
                    mRequestCode);
        }

    }

}
