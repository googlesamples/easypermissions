package pub.devrel.easypermissions;

import android.app.Application;

/**
 * Simple Application is needed for hosting Activities (e.g. {@link TestActivity}) for use with
 * Robolectric.
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.Theme_AppCompat);
    }
}
