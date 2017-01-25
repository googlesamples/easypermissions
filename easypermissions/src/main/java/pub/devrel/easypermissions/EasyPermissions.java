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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to request and check System permissions for apps targeting Android M (API >= 23).
 */
public class EasyPermissions {

    public interface PermissionCallbacks extends ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

    }

    private static final String TAG = "EasyPermissions";
    private static final String DIALOG_TAG = "RationaleDialogFragmentCompat";

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context the calling context.
     * @param perms   one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission is not
     * yet granted.
     * @see Manifest.permission
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");

            // DANGER ZONE!!! Changing this will break the library.
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
     * Request a set of permissions, showing a rationale if the system requests it.
     *
     * @see #requestPermissions(Activity, String, int, int, int, String...)
     */
    public static void requestPermissions(@NonNull Activity activity,
                                          @NonNull String rationale,
                                          int requestCode,
                                          @NonNull String... perms) {
        requestPermissions(
                activity,
                rationale,
                android.R.string.ok,
                android.R.string.cancel,
                requestCode,
                perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param activity       {@link Activity} requesting permissions. Should implement {@link
     *                       ActivityCompat.OnRequestPermissionsResultCallback} or override {@link
     *                       FragmentActivity#onRequestPermissionsResult(int, String[], int[])} if
     *                       it extends from {@link FragmentActivity}.
     * @param rationale      a message explaining why the application needs this set of permissions,
     *                       will be displayed if the user rejects the request the first time.
     * @param positiveButton custom text for positive button
     * @param negativeButton custom text for negative button
     * @param requestCode    request code to track this request, must be < 256.
     * @param perms          a set of permissions to be requested.
     * @see Manifest.permission
     */
    @SuppressLint("NewApi")
    public static void requestPermissions(@NonNull Activity activity,
                                          @NonNull String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          int requestCode,
                                          @NonNull String... perms) {
        if (hasPermissions(activity, perms)) {
            notifyAlreadyHasPermissions(activity, requestCode, perms);
            return;
        }

        if (shouldShowRationale(activity, perms)) {
            showRationaleDialogFragment(
                    activity.getFragmentManager(),
                    rationale,
                    positiveButton,
                    negativeButton,
                    requestCode,
                    perms);
        } else {
            ActivityCompat.requestPermissions(activity, perms, requestCode);
        }
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @see #requestPermissions(Fragment, String, int, int, int, String...)
     */
    public static void requestPermissions(@NonNull Fragment fragment,
                                          @NonNull String rationale,
                                          int requestCode,
                                          @NonNull String... perms) {
        requestPermissions(
                fragment,
                rationale,
                android.R.string.ok,
                android.R.string.cancel,
                requestCode,
                perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param fragment {@link Fragment} requesting permissions. Should override {@link
     *                 Fragment#onRequestPermissionsResult(int, String[], int[])}.
     * @see #requestPermissions(Activity, String, int, int, int, String...)
     */
    @SuppressLint("NewApi")
    public static void requestPermissions(@NonNull Fragment fragment,
                                          @NonNull String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          int requestCode,
                                          @NonNull String... perms) {
        if (hasPermissions(fragment.getContext(), perms)) {
            notifyAlreadyHasPermissions(fragment, requestCode, perms);
            return;
        }

        if (shouldShowRationale(fragment, perms)) {
            RationaleDialogFragmentCompat
                    .newInstance(positiveButton, negativeButton, rationale, requestCode, perms)
                    .show(fragment.getChildFragmentManager(), DIALOG_TAG);
        } else {
            fragment.requestPermissions(perms, requestCode);
        }
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @see #requestPermissions(android.app.Fragment, String, int, int, int, String...)
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void requestPermissions(@NonNull android.app.Fragment fragment,
                                          @NonNull String rationale,
                                          int requestCode,
                                          @NonNull String... perms) {
        requestPermissions(
                fragment,
                rationale,
                android.R.string.ok,
                android.R.string.cancel,
                requestCode,
                perms);
    }

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param fragment {@link android.app.Fragment} requesting permissions. Should override {@link
     *                 android.app.Fragment#onRequestPermissionsResult(int, String[], int[])}.
     * @see #requestPermissions(Activity, String, int, int, int, String...)
     */
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void requestPermissions(@NonNull android.app.Fragment fragment,
                                          @NonNull String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          int requestCode,
                                          @NonNull String... perms) {
        if (hasPermissions(fragment.getActivity(), perms)) {
            notifyAlreadyHasPermissions(fragment, requestCode, perms);
            return;
        }

        if (shouldShowRationale(fragment, perms)) {
            showRationaleDialogFragment(
                    fragment.getChildFragmentManager(),
                    rationale,
                    positiveButton,
                    negativeButton,
                    requestCode,
                    perms);
        } else {
            fragment.requestPermissions(perms, requestCode);
        }
    }

    /**
     * Handle the result of a permission request, should be called from the calling {@link
     * Activity}'s {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int,
     * String[], int[])} method.
     * <p>
     * If any permissions were granted or denied, the {@code object} will receive the appropriate
     * callbacks through {@link PermissionCallbacks} and methods annotated with {@link
     * AfterPermissionGranted} will be run if appropriate.
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param receivers    an array of objects that have a method annotated with {@link
     *                     AfterPermissionGranted} or implement {@link PermissionCallbacks}.
     */
    public static void onRequestPermissionsResult(int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults,
                                                  @NonNull Object... receivers) {
        // Make a collection of granted and denied permissions from the request.
        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        // iterate through all receivers
        for (Object object : receivers) {
            // Report granted permissions, if any.
            if (!granted.isEmpty()) {
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsGranted(requestCode, granted);
                }
            }

            // Report denied permissions, if any.
            if (!denied.isEmpty()) {
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsDenied(requestCode, denied);
                }
            }

            // If 100% successful, call annotated methods
            if (!granted.isEmpty() && denied.isEmpty()) {
                runAnnotatedMethods(object, requestCode);
            }
        }
    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * @param activity          {@link Activity} requesting permissions.
     * @param deniedPermissions list of denied permissions, usually from {@link
     *                          PermissionCallbacks#onPermissionsDenied(int, List)}
     * @return {@code true} if at least one permission in the list was permanently denied.
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Activity activity,
                                                          @NonNull List<String> deniedPermissions) {
        for (String deniedPermission : deniedPermissions) {
            if (permissionPermanentlyDenied(activity, deniedPermission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * @see #somePermissionPermanentlyDenied(Activity, List)
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Fragment fragment,
                                                          @NonNull List<String> deniedPermissions) {
        for (String deniedPermission : deniedPermissions) {
            if (permissionPermanentlyDenied(fragment, deniedPermission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * @see #somePermissionPermanentlyDenied(Activity, List)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean somePermissionPermanentlyDenied(@NonNull android.app.Fragment fragment,
                                                          @NonNull List<String> deniedPermissions) {
        for (String deniedPermission : deniedPermissions) {
            if (permissionPermanentlyDenied(fragment, deniedPermission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @param activity         {@link Activity} requesting permissions.
     * @param deniedPermission denied permission.
     * @return {@code true} if the permissions has been permanently denied.
     */
    public static boolean permissionPermanentlyDenied(@NonNull Activity activity,
                                                      @NonNull String deniedPermission) {
        return !shouldShowRequestPermissionRationale(activity, deniedPermission);
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @see #permissionPermanentlyDenied(Activity, String)
     */
    public static boolean permissionPermanentlyDenied(@NonNull Fragment fragment,
                                                      @NonNull String deniedPermission) {
        return !shouldShowRequestPermissionRationale(fragment, deniedPermission);
    }

    /**
     * Check if a permission has been permanently denied (user clicked "Never ask again").
     *
     * @see #permissionPermanentlyDenied(Activity, String)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean permissionPermanentlyDenied(@NonNull android.app.Fragment fragment,
                                                      @NonNull String deniedPermission) {
        return !shouldShowRequestPermissionRationale(fragment, deniedPermission);
    }

    private static void notifyAlreadyHasPermissions(Object object,
                                                    int requestCode,
                                                    @NonNull String[] perms) {
        int[] grantResults = new int[perms.length];
        for (int i = 0; i < perms.length; i++) {
            grantResults[i] = PackageManager.PERMISSION_GRANTED;
        }

        onRequestPermissionsResult(requestCode, perms, grantResults, object);
    }

    /**
     * @param object Activity or Fragment
     * @return true if the user has previously denied any of the {@code perms} and we should show a
     * rationale, false otherwise.
     */
    private static boolean shouldShowRationale(@NonNull Object object, @NonNull String[] perms) {
        boolean shouldShowRationale = false;
        for (String perm : perms) {
            shouldShowRationale =
                    shouldShowRationale || shouldShowRequestPermissionRationale(object, perm);
        }
        return shouldShowRationale;
    }

    private static boolean shouldShowRequestPermissionRationale(@NonNull Object object,
                                                                @NonNull String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else if (object instanceof android.app.Fragment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
            } else {
                throw new IllegalArgumentException(
                        "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
            }
        } else {
            throw new IllegalArgumentException("Object was neither an Activity nor a Fragment.");
        }
    }

    /**
     * Show a {@link RationaleDialogFragment} explaining permission request rationale.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private static void showRationaleDialogFragment(@NonNull android.app.FragmentManager fragmentManager,
                                                    @NonNull String rationale,
                                                    @StringRes int positiveButton,
                                                    @StringRes int negativeButton,
                                                    int requestCode,
                                                    @NonNull String... perms) {
        RationaleDialogFragment
                .newInstance(positiveButton, negativeButton, rationale, requestCode, perms)
                .show(fragmentManager, DIALOG_TAG);
    }

    private static void runAnnotatedMethods(@NonNull Object object, int requestCode) {
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
                                "Cannot execute method " + method.getName() + " because it is non-void method and/or has input parameters.");
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

    private static boolean isUsingAndroidAnnotations(@NonNull Object object) {
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
