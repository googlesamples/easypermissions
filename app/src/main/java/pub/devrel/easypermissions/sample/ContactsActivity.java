package pub.devrel.easypermissions.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ContactsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    private static final  int RC_CONTACTS_PERM = 126;
    TextView message;
    private static final String TAG = "ContactsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        message = findViewById(R.id.textPermission);
        contactsTask();
        contactsMessage();
    }


    @Override
    protected void onResume() {
        super.onResume();
        contactsMessage();


    }

    public void contactsMessage(){
        if(hasContactsPermission()){
            message.setText(getString(R.string.permission_accepted));
        }else{
            message.setText(getString(R.string.permission_denied));
        }
    }


    private boolean hasContactsPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS);
    }

    @AfterPermissionGranted(RC_CONTACTS_PERM)
    public void contactsTask() {
        if (hasContactsPermission()) {
            // Have permissions, do the thing!
            Toast.makeText(this, "TODO: Contacts things", Toast.LENGTH_LONG).show();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_contacts),
                    RC_CONTACTS_PERM,
                    Manifest.permission.READ_CONTACTS);
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }*/

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
       /* if(requestCode == RC_CONTACTS_PERM){
            contactsMessage();
        }*/
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }

        /*if(requestCode == RC_CONTACTS_PERM){
            contactsMessage(getString(R.string.permission_denied));
        }*/
    }


    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.d(TAG, "onRationaleAccepted:" + requestCode);
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.d(TAG, "onRationaleDenied:" + requestCode);
    }
}