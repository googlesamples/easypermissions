package pub.devrel.easypermissions;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import java.util.Arrays;

import pub.devrel.easypermissions.helper.PermissionHelper;

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
        mPerms = perms;
        mRequestCode = requestCode;
        mRationale = rationale;
        mPositiveButtonText = positiveButtonText;
        mNegativeButtonText = negativeButtonText;
        mTheme = theme;
    }

    @NonNull
    public PermissionHelper getHelper() {
        return mHelper;
    }

    @NonNull
    public String[] getPerms() {
        return mPerms;
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

    public static final class Builder {
        private final PermissionHelper mHelper;
        private final int mRequestCode;
        private final String[] mPerms;

        private String mRationale;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private int mTheme = -1;

        public Builder(@NonNull Activity activity, int requestCode,
                       @NonNull @Size(min = 1) String... perms) {
            mHelper = PermissionHelper.newInstance(activity);
            mRequestCode = requestCode;
            mPerms = perms;
        }

        public Builder(@NonNull Fragment fragment, int requestCode,
                       @NonNull @Size(min = 1) String... perms) {
            mHelper = PermissionHelper.newInstance(fragment);
            mRequestCode = requestCode;
            mPerms = perms;
        }

        public Builder(@NonNull android.app.Fragment fragment, int requestCode,
                       @NonNull @Size(min = 1) String... perms) {
            mHelper = PermissionHelper.newInstance(fragment);
            mRequestCode = requestCode;
            mPerms = perms;
        }

        @NonNull
        public Builder setRationale(@Nullable String rationale) {
            mRationale = rationale;
            return this;
        }

        @NonNull
        public Builder setRationale(@StringRes int resId) {
            mRationale = mHelper.getContext().getString(resId);
            return this;
        }

        @NonNull
        public Builder setPositiveButtonText(@Nullable String positiveButtonText) {
            mPositiveButtonText = positiveButtonText;
            return this;
        }

        @NonNull
        public Builder setPositiveButtonText(@StringRes int resId) {
            mPositiveButtonText = mHelper.getContext().getString(resId);
            return this;
        }

        @NonNull
        public Builder setNegativeButtonText(@Nullable String negativeButtonText) {
            mNegativeButtonText = negativeButtonText;
            return this;
        }

        @NonNull
        public Builder setNegativeButtonText(@StringRes int resId) {
            mNegativeButtonText = mHelper.getContext().getString(resId);
            return this;
        }

        @NonNull
        public Builder setTheme(@StyleRes int theme) {
            mTheme = theme;
            return this;
        }

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
