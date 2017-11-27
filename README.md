# EasyPermissions [![Build Status][1]][2] [![Android Weekly][3]][4]

EasyPermissions is a wrapper library to simplify basic system permissions logic when targeting
Android M or higher.

## Installation

EasyPermissions is installed by adding the following dependency to your `build.gradle` file:

```groovy
dependencies {
    compile 'pub.devrel:easypermissions:1.0.1'
}
```

## Usage

### Basic

To begin using EasyPermissions, have your `Activity` (or `Fragment`) override the `onRequestPermissionsResult` method:

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
```

### Request Permissions

The example below shows how to request permissions for a method that requires both
`CAMERA` and `ACCESS_FINE_LOCATION` permissions. There are a few things to note:

  * Using `EasyPermissions#hasPermissions(...)` to check if the app already has the
    required permissions. This method can take any number of permissions as its final
    argument.
  * Requesting permissions with `EasyPermissions#requestPermissions`. This method
    will request the system permissions and show the rationale string provided if
    necessary. The request code provided should be unique to this request, and the method
    can take any number of permissions as its final argument.
  * Use of the `AfterPermissionGranted` annotation. This is optional, but provided for
    convenience. If all of the permissions in a given request are granted, any methods
    annotated with the proper request code will be executed. This is to simplify the common
    flow of needing to run the requesting method after all of its permissions have been granted.
    This can also be achieved by adding logic on the `onPermissionsGranted` callback.

```java
@AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
private void methodRequiresTwoPermission() {
    String[] perms = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
    if (EasyPermissions.hasPermissions(this, perms)) {
        // Already have permission, do the thing
        // ...
    } else {
        // Do not have permissions, request them now
        EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                RC_CAMERA_AND_LOCATION, perms);
    }
}
```

Optionally, for a finer control, you can have your `Activity` / `Fragment` implement
the `PermissionCallbacks` interface.

```java
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }
}
```

### Required Permissions

In some cases your app will not function properly without certain permissions. If the user
denies these permissions with the "Never Ask Again" option, you will be unable to request
these permissions from the user and they must be changed in app settings. You can use the
method `EasyPermissions.somePermissionPermanentlyDenied(...)` to display a dialog to the
user in this situation and direct them to the system setting screen for your app:

```java
@Override
public void onPermissionsDenied(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

    // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
    // This will display a dialog directing them to enable the permission in app settings.
    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
        new AppSettingsDialog.Builder(this).build().show();
    }
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
        // Do something after user returned from app settings screen, like showing a Toast.
        Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
                .show();
    }
}
```

## LICENSE

```
	Copyright 2017 Google

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

```

[1]: https://travis-ci.org/googlesamples/easypermissions.svg?branch=master
[2]: https://travis-ci.org/googlesamples/easypermissions
[3]: https://img.shields.io/badge/Android%20Weekly-%23185-2CB3E5.svg?style=flat
[4]: http://androidweekly.net/issues/issue-185
