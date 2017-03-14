# EasyPermissions

EasyPermissions 是基于 Android M 以上系统的一个简化的权限处理库。

## 安装

EasyPermissions 可以在 `build.gradle` 中被引用

```groovy
dependencies {
    compile 'pub.devrel:easypermissions:0.3.0'
}
```

## 用法

### 基础

开始使用 EasyPermissions 前， 让你的 `Activity` (or `Fragment`) 重写 `onRequestPermissionsResult` 方法：

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

### 请求权限

下面的例子展示了一个同时请求 `CAMERA` 和 `CHANGE_WIFI_STATE` 2个权限， 以下是一些注意点：

  * 使用 `EasyPermissions#hasPermissions(...)` 检查app使用已经有权限。 这个方法可以同时检查
    多个权限。
  * 使用 `EasyPermissions#requestPermissions` 请求权限。 这个方法将请求系统权限并且如果需要显示
    权限申请的理由（在第一次拒绝权限后， 第二次再次申请时会弹出理由）
    Request Code 在权限请求中要保持唯一。 可以同时请求多个权限。
  * 使用 `AfterPermissionGranted` 注解。 这是可选的，但是非常方便。当所有权限都请求成功，被注解的方法将被执行。
    也可以通过 `onPermissionsGranted` 回调获取结果。

```java
@AfterPermissionGranted(RC_CAMERA_AND_WIFI)
private void methodRequiresTwoPermission() {
    String[] perms = {Manifest.permission.CAMERA, Manifest.permission.CHANGE_WIFI_STATE};
    if (EasyPermissions.hasPermissions(this, perms)) {
        // Already have permission, do the thing
        // ...
    } else {
        // Do not have permissions, request them now
        EasyPermissions.requestPermissions(this, getString(R.string.camera_and_wifi_rationale),
                RC_CAMERA_AND_WIFI, perms);
    }
}
```

让你的 `Activity` / `Fragment` 继承 `PermissionCallbacks` 接口

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
    public void onPermissionsGranted(int requestCode, List<String> allPerms) {
        // All permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> deniedPerms, List<String> grantedPerms) {
        // Some permissions have been denied
        // ...
    }
}
```

### 获取权限结果

某些情况下，如果用户拒绝了权限并且勾选了 "不再询问"， 你将在之后的每次请求权限都返回拒绝，并且不会弹出任何提示框，只有当用户去 "设置" 中改变权限。
这很不友好。你可以使用 `EasyPermissions.somePermissionPermanentlyDenied(...)` 判断是否有权限已经被永久拒绝，
如果有可以弹出提示框提示用户去设置里开启权限。

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

### 更简单的权限处理（二次封装一行代码处理权限）

你可以把大部分权限处理代码放到 `BaseActivity/BaseFragment` 中， 然后调用 `performCodeWithPermission` 方法即可实现一行代码申请权限：

```java
String[] perms = {Manifest.permission.CAMERA};
performCodeWithPermission(getString(R.string.rationale_camera), RC_CAMERA_PERM, perms, new PermissionCallback() {
    @Override
    public void hasPermission(List<String> allPerms) {
        Toast.makeText(SimplePermActivity.this, "TODO: Camera things", Toast.LENGTH_LONG).show();
    }

    @Override
    public void noPermission(List<String> deniedPerms, List<String> grantedPerms, Boolean hasPermanentlyDenied) {
        if (hasPermanentlyDenied) {
            alertAppSetPermission(getString(R.string.rationale_ask_again), RC_SETTINGS_SCREEN);
        }
    }
});
```

更详细的代码可以参见 `BaseActivity` 和 `SimplPermActivity`