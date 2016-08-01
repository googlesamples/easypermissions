# EasyPermissions
This project is forked from EasyPermission, modified some few codes to make it 
more easier to use, which lets you more focus on your business logic.
本项目修改自EasyPermission,修改后方便使用,让你更专注于你的业务逻辑.

## 特点

- 删除注解,只使用回调来处理权限
- 像PermissionGen那样申请权限,清晰明了一点

## 使用示例

这个示例代码是标准版代码,在SDK23以下的机器也这么写.

```java
/*
    1. 在申请权限类中必须实现EasyPermission.PermissionCallback接口
*/
public class MainFragment extends Fragment implements EasyPermission.PermissionCallback {
      @Override 
      public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            申请权限,使用Build模式更清晰 
        */
        EasyPermission.with(this)
            .addRequestCode(RC_SMS_PERM)
            .permissions(Manifest.permission.READ_SMS)
            .rationale(getString(R.string.rationale_sms))
            .request();
    }

    @Override 
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        /*
            传递到EasyPermission中
        */
        EasyPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override 
    public void onPermissionGranted(int requestCode, List<String> perms) {
        /*
            用户授予所有权限,继续你的业务逻辑
        */
        Toast.makeText(getActivity(), "TODO: SMS Granted", Toast.LENGTH_SHORT).show();
    }

    @Override 
    public void onPermissionDenied(int requestCode, List<String> perms) {
        /*
            用户拒绝至少一个权限
        */
        Toast.makeText(getActivity(), "TODO: SMS Denied", Toast.LENGTH_SHORT).show();

        // 弹出对话框,让用户去设置界面授予权限.
        // 根据业务需求可以不添加此代码
        EasyPermission.checkDeniedPermissionsNeverAskAgain(this, "cannot work without permissions", perms);
    }

    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
            从Settings界面返回
        */
        if (requestCode == EasyPermission.SETTINGS_REQ_CODE) {
            //判断是否授予权限
            if(EasyPermission.hasPermissions(getContext(), Manifest.permission.READ_SMS)){
                // 授予了权限,继续你的业务逻辑
            }else{
                // 还是没有权限,弹出提示信息
                Toast.makeText(getContext(),"no permission, can not work",Toast.LENGTH_SHORT).show();
            }
        }
    }
}

```

## 博客文章
[Android 6.0权限管理及其封装](http://blog.csdn.net/u014099894/article/details/51896832)

