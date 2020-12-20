/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easypermissions.controllers

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import pub.devrel.easypermissions.annotations.Mockable
import java.util.concurrent.CompletableFuture

/**
 * Controller class to allow starting Fragments, similar to the old SupportFragmentController.
 */
@Mockable
class FragmentController<T : Fragment>(clazz: Class<T>) {

    private val scenario: FragmentScenario<T> = FragmentScenario.launch(clazz)

    @Synchronized
    fun resume(): T {
        val fragmentFuture = CompletableFuture<T>()
        scenario.onFragment { fragmentFuture.complete(it) }
        return fragmentFuture.get()
    }
}
