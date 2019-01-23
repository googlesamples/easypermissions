package pub.devrel.easypermissions.testhelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;

/**
 * Helper class to allow starting Fragments, similar to the old SupportFragmentController.
 */
public class FragmentController<T extends Fragment> {

    private FragmentScenario<T> scenario;

    public FragmentController(Class<T> clazz) {
        scenario = FragmentScenario.launch(clazz);
    }

    public synchronized T resume() {
        final CompletableFuture<T> fragmentFuture = new CompletableFuture<>();

        scenario.onFragment(new FragmentScenario.FragmentAction<T>() {
            @Override
            public void perform(@NonNull T fragment) {
                fragmentFuture.complete(fragment);
            }
        });

        try {
            return fragmentFuture.get();
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
