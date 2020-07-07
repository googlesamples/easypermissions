package pub.devrel.easypermissions.testhelper;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Helper class to allow starting Activity, similar to the Robolectric ActivityConroller.
 */
public class ActivityController<T extends Activity> {

    private ActivityScenario<T> scenario;

    public ActivityController(Class<T> clazz) {
        scenario = ActivityScenario.launch(clazz);
    }

    public synchronized T resume() {
        final CompletableFuture<T> ActivityFuture = new CompletableFuture<>();

        scenario.onActivity(new ActivityScenario.ActivityAction<T>() {
            @Override
            public void perform(@NonNull T activity) {
                ActivityFuture.complete(activity);
            }
        });

        try {
            return ActivityFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        scenario.recreate();
    }

}
