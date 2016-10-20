package pub.devrel.easypermissions;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import java.util.Arrays;

/**
 * RationaleDialogFragment that is being used where the calling Activity/Fragment is not from the support Library
 */
@TargetApi(17)
public class RationaleDialogFragment extends DialogFragment implements Dialog.OnClickListener {

    private EasyPermissions.PermissionCallbacks permissionCallbacks;

    private int positiveButton;
    private int negativeButton;
    private int requestCode;
    private String rationaleMsg;
    private String[] permissions;

    static RationaleDialogFragment newInstance(@StringRes int positiveButton,
                                               @StringRes int negativeButton,
                                               @NonNull String rationaleMsg,
                                               int requestCode,
                                               @NonNull String[] permissions) {
        RationaleDialogFragment dialogFragment = new RationaleDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(RationaleConstants.KEY_POSITIVE_BUTTON, positiveButton);
        bundle.putInt(RationaleConstants.KEY_NEGATIVE_BUTTON, negativeButton);
        bundle.putString(RationaleConstants.KEY_RATIONALE_MESSAGE, rationaleMsg);
        bundle.putInt(RationaleConstants.KEY_REQUEST_CODE, requestCode);
        bundle.putStringArray(RationaleConstants.KEY_PERMISSIONS, permissions);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() != null && getParentFragment() instanceof EasyPermissions.PermissionCallbacks) {
            permissionCallbacks = (EasyPermissions.PermissionCallbacks) getParentFragment();
        } else if (context instanceof EasyPermissions.PermissionCallbacks) {
            permissionCallbacks = (EasyPermissions.PermissionCallbacks) context;
        } else {
            throw new RuntimeException("Activity or Fragment should implement PermissionCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        permissionCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("positiveButton", positiveButton);
        bundle.putInt("negativeButton", negativeButton);
        bundle.putInt("requestCode", requestCode);
        bundle.putStringArray("permissions", permissions);
        bundle.putString("rationaleMsg", rationaleMsg);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setupArguments();
        return new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setOnCancelListener(this)
                .setPositiveButton(positiveButton, this)
                .setNegativeButton(negativeButton, this)
                .setMessage(rationaleMsg).create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (permissionCallbacks != null) {
            permissionCallbacks.onPermissionsDenied(requestCode, Arrays.asList(permissions));
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == Dialog.BUTTON_POSITIVE) {
            EasyPermissions.executePermissionsRequest(this, permissions, requestCode);
        } else {
            permissionCallbacks.onPermissionsDenied(requestCode, Arrays.asList(permissions));
        }
        dismiss();
    }

    private void setupArguments() {
        Bundle bundle = getArguments();
        positiveButton = bundle.getInt(RationaleConstants.KEY_POSITIVE_BUTTON);
        negativeButton = bundle.getInt(RationaleConstants.KEY_NEGATIVE_BUTTON);
        rationaleMsg = bundle.getString(RationaleConstants.KEY_RATIONALE_MESSAGE);
        requestCode = bundle.getInt(RationaleConstants.KEY_REQUEST_CODE);
        permissions = bundle.getStringArray(RationaleConstants.KEY_PERMISSIONS);
    }
}
