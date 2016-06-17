/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easypermissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to request and check System permissions for apps targeting Android M (API >= 23).
 */
public class EasyPermissions {

    private static final String TAG = "EasyPermissions";

    public interface PermissionCallbacks extends
                                         ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context the calling context.
     * @param perms   one ore more permissions, such as {@code android.Manifest.permission.CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission
     * is not yet granted.
     */
    public static boolean hasPermissions(Context context, String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions 
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) ==
                    PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param object      Activity or Fragment requesting permissions. Should implement
     *                    {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback}
     *                    or
     *                    {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
     * @param rationale   a message explaining why the application needs this set of permissions, will
     *                    be displayed if the user rejects the request the first time.
     * @param requestCode request code to track this request, must be < 256.
     * @param perms       a set of permissions to be requested.
     */
    public static void requestPermissions(final Object object, String rationale,
                                          final int requestCode, final String... perms) {
        requestPermissions(object, rationale,
                           android.R.string.ok,
                           android.R.string.cancel,
                           requestCode, perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param object         Activity or Fragment requesting permissions. Should implement
     *                       {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback}
     *                       or
     *                       {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
     * @param rationale      a message explaining why the application needs this set of permissions, will
     *                       be displayed if the user rejects the request the first time.
     * @param positiveButton custom text for positive button
     * @param negativeButton custom text for negative button
     * @param requestCode    request code to track this request, must be < 256.
     * @param perms          a set of permissions to be requested.
     */
    public static void requestPermissions(final Object object, String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          final int requestCode, final String... perms) {

        checkCallingObjectSuitability(object);
        final PermissionCallbacks callbacks = (PermissionCallbacks) object;

        boolean shouldShowRationale = false;
        for (String perm : perms) {
            shouldShowRationale =
                    shouldShowRationale || shouldShowRequestPermissionRationale(object, perm);
        }

        if (shouldShowRationale) {
            Activity activity = getActivity(object);
            if (null == activity) {
                return;
            }

            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setMessage(rationale)
                    .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executePermissionsRequest(object, perms, requestCode);
                        }
                    })
                    .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // act as if the permissions were denied
                            callbacks.onPermissionsDenied(requestCode, Arrays.asList(perms));
                        }
                    }).create();
            dialog.show();
        } else {
            executePermissionsRequest(object, perms, requestCode);
        }
    }

    /**
     * Handle the result of a permission request, should be called from the calling Activity's
     * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}
     * method.
     * <p>
     * If any permissions were granted or denied, the Activity will receive the appropriate
     * callbacks through {@link PermissionCallbacks} and methods annotated with
     * {@link AfterPermissionGranted} will be run if appropriate.
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param object       the calling Activity or Fragment.
     * @throws IllegalArgumentException if the calling Activity does not implement
     *                                  {@link PermissionCallbacks}.
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                  int[] grantResults, Object object) {

        checkCallingObjectSuitability(object);
        PermissionCallbacks callbacks = (PermissionCallbacks) object;

        // Make a collection of granted and denied permissions from the request.
        ArrayList<String> granted = new ArrayList<>();
        ArrayList<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        // Report granted permissions, if any.
        if (!granted.isEmpty()) {
            // Notify callbacks
            callbacks.onPermissionsGranted(requestCode, granted);
        }

        // Report denied permissions, if any.
        if (!denied.isEmpty()) {
            callbacks.onPermissionsDenied(requestCode, denied);
        }

        // If 100% successful, call annotated methods
        if (!granted.isEmpty() && denied.isEmpty()) {
            runAnnotatedMethods(object, requestCode);
        }
    }

    /**
     * If user denied permissions with the flag NEVER ASK AGAIN, open a dialog explaining the
     * permissions rationale again and directing the user to the app settings.
     *
     * NOTE: use of this method is optional, should be called from
     * {@link PermissionCallbacks#onPermissionsDenied(int, List)}
     *
     * @param object      the calling Activity or Fragment.
     * @param deniedPerms the set of denied permissions.
     * @return {@code true} if user denied at least one permission with the flag NEVER ASK AGAIN.
     */
    public static boolean checkDeniedPermissionsNeverAskAgain(Object object, String rationale,
                                                           @StringRes int positiveButton,
                                                           @StringRes int negativeButton,
                                                           List<String> deniedPerms) {
        boolean shouldShowRationale;
        for (String perm : deniedPerms) {
            shouldShowRationale = shouldShowRequestPermissionRationale(object, perm);
            if (!shouldShowRationale) {
                final Activity activity = getActivity(object);
                if (null == activity) {
                    return true;
                }

                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setMessage(rationale)
                        .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                intent.setData(uri);
                                activity.startActivity(intent);
                            }
                        })
                        .setNegativeButton(negativeButton, null)
                        .create();
                dialog.show();

                return true;
            }
        }

        return false;
    }

    @TargetApi(23)
    private static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else {
            return false;
        }
    }

    @TargetApi(23)
    private static void executePermissionsRequest(Object object, String[] perms, int requestCode) {
        checkCallingObjectSuitability(object);

        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).requestPermissions(perms, requestCode);
        }
    }

    @TargetApi(11)
    private static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).getActivity();
        } else {
            return null;
        }
    }

    private static void runAnnotatedMethods(Object object, int requestCode) {
        Class clazz = object.getClass();
        if (isUsingAndroidAnnotations(object)) {
            clazz = clazz.getSuperclass();
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AfterPermissionGranted.class)) {
                // Check for annotated methods with matching request code.
                AfterPermissionGranted ann = method.getAnnotation(AfterPermissionGranted.class);
                if (ann.value() == requestCode) {
                    // Method must be void so that we can invoke it
                    if (method.getParameterTypes().length > 0) {
                        throw new RuntimeException(
                                "Cannot execute non-void method " + method.getName());
                    }

                    try {
                        // Make method accessible if private
                        if (!method.isAccessible()) {
                            method.setAccessible(true);
                        }
                        method.invoke(object);
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, "runDefaultMethod:IllegalAccessException", e);
                    } catch (InvocationTargetException e) {
                        Log.e(TAG, "runDefaultMethod:InvocationTargetException", e);
                    }
                }
            }
        }
    }

    private static void checkCallingObjectSuitability(Object object) {
        // Make sure Object is an Activity or Fragment
        boolean isActivity = object instanceof Activity;
        boolean isSupportFragment = object instanceof Fragment;
        boolean isAppFragment = object instanceof android.app.Fragment;
        boolean isMinSdkM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

        if (!(isSupportFragment || isActivity || (isAppFragment && isMinSdkM))) {
            if (isAppFragment) {
                throw new IllegalArgumentException(
                        "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
            } else {
                throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
            }
        }

        // Make sure Object implements callbacks
        if (!(object instanceof PermissionCallbacks)) {
            throw new IllegalArgumentException("Caller must implement PermissionCallbacks.");
        }
    }

    private static boolean isUsingAndroidAnnotations(Object object) {
        if (!object.getClass().getSimpleName().endsWith("_")) {
            return false;
        }

        try {
            Class clazz = Class.forName("org.androidannotations.api.view.HasViews");
            return clazz.isInstance(object);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
