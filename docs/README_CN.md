# EasyPermissions [![Build Status][1]][2] [![Android Weekly][3]][4]

EasyPermissions是一个封装的类库，为开发者简化了在android M（或以上）申请系统权限的逻辑。

## 安装

开发者可以通过在 `build.gradle` 文件中添加如下的依赖项来使用EasyPermissions:

```groovy
dependencies {
    implementation 'pub.devrel:easypermissions:1.1.1'
}
```

注意：EasyPermissions依赖于android支持库 `27.0.1`，所以你需要将 `compileSdkVersion` 调整到 `27` 或更高。 这个变化应该是安全的， `compileSdkVersion` 不会改变app的行为。

## 使用

### 基本

使用EasyPermissions的第一步， 在你的 `Activiey` （或 `Fragment`）里面重写 `onRequestPermissionsResult` 方法：

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

        // 转发结果到EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
```

### 请求权限

下面的例子展示了如何在一个方法里面同时申请 `CAMERA` 和 `ACCESS_FINE_LOCATION` 的权限，有些地方需要注意下：

  * 使用 `EasyPermissions.hasPermissions(...)` 检查app是否已经拥有所需的权限。这个方法的最后
    一个参数可以传入任意数量的权限。
  * 使用 `EasyPermissions.requestPermissions` 申请权限。这个方法将会申请系统权限，并且会在必须
    的时候展示你提供的说明文本（`rationale`）。对于这个请求，你提供的请求码（`requestCode`）应该
    是唯一的，这个方法的最后一个参数可以传入任意数量的权限。
  * `AfterPermissionGranted`注解的使用。这个是可选的，但提供它是为了方便。如果一个请求里面的所有
    权限都被授予了，*所有*带有这个注解和相同请求码的方法都会被执行（确认请求码是唯一的）。带有这个注
    解的方法必须是返回void并且没有参数（你可以使用*onSaveInstanceState*来保持你需要的参数）。这是
    为了简化需要在所有权限被授予后运行某些方法的普通流程。在 `onPermissionsGranted` 回调里面添加逻
    辑代码也可以实现相同的作用。

```java
@AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
private void methodRequiresTwoPermission() {
    String[] perms = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};
    if (EasyPermissions.hasPermissions(this, perms)) {
        // 已经有权限，做你该做的事
        // ...
    } else {
        // 没有权限，现在申请
        EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                RC_CAMERA_AND_LOCATION, perms);
    }
}
```

或者显示说明文本的对话框，使用一个 `PermissionRequest`：


```java
EasyPermissions.requestPermissions(
        new PermissionRequest.Builder(this, RC_CAMERA_AND_LOCATION, perms)
                .setRationale(R.string.camera_and_location_rationale)
                .setPositiveButtonText(R.string.rationale_ask_ok)
                .setNegativeButtonText(R.string.rationale_ask_cancel)
                .setTheme(R.style.my_fancy_style)
                .build());
```

可选地，为了控制说明显示的对话框，你可以让你的`Activity` / `Fragment`实现`PermissionCallbacks`接口。

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

        // 转发结果给 EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // 有些权限被授予了
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // 有些权限被拒绝了
        // ...
    }
}
```

### 必须的权限

一些情况下，你的app没有权限的话将无法正常运行。如果用户拒绝了这些权限并且勾选了 “不再询问” ("Never Ask Again")，
你将不能再向用户请求这些权限，必须用户在设置里面改变。你可以使用 `EasyPermissions.somePermissionPermanentlyDenied(...)`
这个方法来显示一个对话框，在这种情况下指引用户到系统设置页面：

```java
@Override
public void onPermissionsDenied(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

    // (可选) 检查是否用户拒绝了并且勾选了"不再询问"
    // 这将显示一个对话框并且指引用户去设置里面启用权限。
    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
        new AppSettingsDialog.Builder(this).build().show();
    }
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
        // 在用户从设置页面回来后做些事情，比如显示一个Toast。
        Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
                .show();
    }
}
```

## 许可

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
