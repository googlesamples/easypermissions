package pub.devrel.easypermissions;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;

import java.util.Arrays;

import pub.devrel.easypermissions.helper.PermissionHelper;

/**
 * An immutable model object that holds all of the parameters associated with a permission request,
 * such as the permissions, request code, and rationale.
 *
 * @see EasyPermissions#requestPermissions(PermissionRequest)
 * @see PermissionRequest.Builder
 */
public final class PermissionRequest {
    private final PermissionHelper mHelper;
    private final String[] mPerms;
    private final int mRequestCode;
    private final String mRationale;
    private final String mPositiveButtonText;
    private final String mNegativeButtonText;
    private final int mTheme;

    private PermissionRequest(PermissionHelper helper,
                              String[] perms,
                              int requestCode,
                              String rationale,
                              String positiveButtonText,
                              String negativeButtonText,
                              int theme) {
        mHelper = helper;
        mPerms = perms.clone();
        mRequestCode = requestCode;
        mRationale = rationale;
        mPositiveButtonText = positiveButtonText;
        mNegativeButtonText = negativeButtonText;
        mTheme = theme;
    }

    @NonNull
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public PermissionHelper getHelper() {
        return mHelper;
    }

    @NonNull
    public String[] getPerms() {
        return mPerms.clone();
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    @NonNull
    public String getRationale() {
        return mRationale;
    }

    @NonNull
    public String getPositiveButtonText() {
        return mPositiveButtonText;
    }

    @NonNull
    public String getNegativeButtonText() {
        return mNegativeButtonText;
    }

    @StyleRes
    public int getTheme() {
        return mTheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionRequest request = (PermissionRequest) o;

        return Arrays.equals(mPerms, request.mPerms) && mRequestCode == request.mRequestCode;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(mPerms);
        result = 31 * result + mRequestCode;
        return result;
    }

    @Override
    public String toString() {
        return "PermissionRequest{" +
                "mHelper=" + mHelper +
                ", mPerms=" + Arrays.toString(mPerms) +
                ", mRequestCode=" + mRequestCode +
                ", mRationale='" + mRationale + '\'' +
                ", mPositiveButtonText='" + mPositiveButtonText + '\'' +
                ", mNegativeButtonText='" + mNegativeButtonText + '\'' +
                ", mTheme=" + mTheme +
                '}';
    }

    /**
     * Builder to build a permission request with variable options.
     *
     * @see PermissionRequest
     */
    public static final class Builder {
        private final PermissionHelper mHelper;
        private final int mRequestCode;
        private final String[] mPerms;

        private String mRationale;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private int mTheme = -1;

        /**
         * Construct a new permission request builder with a host, request code, and the requested
         * permissions.
         *
         * @param activity    the permission request host
         * @param requestCode request code to track this request; must be &lt; 256
         * @param perms       the set of permissions to be requested
         */
        public Builder(@NonNull Activity activity, int requestCode,
                       @NonNull @Size(min = 1) String... perms) {
            mHelper = PermissionHelper.newInstance(activity);
            mRequestCode = requestCode;
            mPerms = perms;
        }

        /**
         * @see #Builder(Activity, int, String...)
         */
        public Builder(@NonNull Fragment fragment, int requestCode,
                       @NonNull @Size(min = 1) String... perms) {
            mHelper = PermissionHelper.newInstance(fragment);
            mRequestCode = requestCode;
            mPerms = perms;
        }

        /**
         * Set the rationale to display to the user if they don't allow your permissions on the
         * first try. This rationale will be shown as long as the user has denied your permissions
         * at least once, but has not yet permanently denied your permissions. Should the user
         * permanently deny your permissions, use the {@link AppSettingsDialog} instead.
         * <p>
         * The default rationale text is {@link R.string#rationale_ask}.
         *
         * @param rationale the rationale to be displayed to the user should they deny your
         *                  permission at least once
         */
        @NonNull
        public Builder setRationale(@Nullable String rationale) {
            mRationale = rationale;
            return this;
        }

        /**
         * @param resId the string resource to be used as a rationale
         * @see #setRationale(String)
         */
        @NonNull
        public Builder setRationale(@StringRes int resId) {
            mRationale = mHelper.getContext().getString(resId);
            return this;
        }

        /**
         * Set the positive button text for the rationale dialog should it be shown.
         * <p>
         * The default is {@link android.R.string#ok}
         */
        @NonNull
        public Builder setPositiveButtonText(@Nullable String positiveButtonText) {
            mPositiveButtonText = positiveButtonText;
            return this;
        }

        /**
         * @see #setPositiveButtonText(String)
         */
        @NonNull
        public Builder setPositiveButtonText(@StringRes int resId) {
            mPositiveButtonText = mHelper.getContext().getString(resId);
            return this;
        }

        /**
         * Set the negative button text for the rationale dialog should it be shown.
         * <p>
         * The default is {@link android.R.string#cancel}
         */
        @NonNull
        public Builder setNegativeButtonText(@Nullable String negativeButtonText) {
            mNegativeButtonText = negativeButtonText;
            return this;
        }

        /**
         * @see #setNegativeButtonText(String)
         */
        @NonNull
        public Builder setNegativeButtonText(@StringRes int resId) {
            mNegativeButtonText = mHelper.getContext().getString(resId);
            return this;
        }

        /**
         * Set the theme to be used for the rationale dialog should it be shown.
         *
         * @param theme a style resource
         */
        @NonNull
        public Builder setTheme(@StyleRes int theme) {
            mTheme = theme;
            return this;
        }

        /**
         * Build the permission request.
         *
         * @return the permission request
         * @see EasyPermissions#requestPermissions(PermissionRequest)
         * @see PermissionRequest
         */
        @NonNull
        public PermissionRequest build() {
            if (mRationale == null) {
                mRationale = mHelper.getContext().getString(R.string.rationale_ask);
            }
            if (mPositiveButtonText == null) {
                mPositiveButtonText = mHelper.getContext().getString(android.R.string.ok);
            }
            if (mNegativeButtonText == null) {
                mNegativeButtonText = mHelper.getContext().getString(android.R.string.cancel);
            }

            return new PermissionRequest(
                    mHelper,
                    mPerms,
                    mRequestCode,
                    mRationale,
                    mPositiveButtonText,
                    mNegativeButtonText,
                    mTheme);
        }
    }
}
