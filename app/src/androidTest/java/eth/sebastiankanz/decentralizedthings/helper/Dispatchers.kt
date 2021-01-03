package eth.sebastiankanz.decentralizedthings.helper

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO_PARALLELISM_PROPERTY_NAME
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.coroutines.ContinuationInterceptor

object Dispatchers {

    private var delegate: Delegate = DefaultDelegate

    /**
     * Sets a delegate to replace [CoroutineDispatcher]s for testing.
     *
     * Calling this method with null sets it to the default Kotlin coroutines [Dispatchers].
     *
     * @param delegate The alternative [CoroutineDispatcher]s.
     */
    @VisibleForTesting
    fun setDelegate(delegate: Delegate?) {
        this.delegate = when {
            delegate != null -> delegate
            else -> DefaultDelegate
        }
    }

    /**
     * The default [CoroutineDispatcher] that is used by all standard builders like
     * [launch][CoroutineScope.launch], [async][CoroutineScope.async], etc
     * if no dispatcher nor any other [ContinuationInterceptor] is specified in their context.
     *
     * It is backed by a shared pool of threads on JVM. By default, the maximal number of threads used
     * by this dispatcher is equal to the number CPU cores, but is at least two.
     */
    val Default: CoroutineDispatcher
        get() = delegate.Default

    /**
     * A coroutine dispatcher that is confined to the Main thread operating with UI objects.
     * Usually such dispatcher is single-threaded.
     *
     * Access to this property may throw [IllegalStateException] if no main thread dispatchers are present in the classpath.
     *
     * Depending on platform and classpath it can be mapped to different dispatchers:
     * - On JS and Native it is equivalent of [Dispatchers.Default] dispatcher.
     * - On JVM it either Android main thread dispatcher, JavaFx or Swing EDT dispatcher. It is chosen by
     *   [`ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).
     *
     * In order to work with `Main` dispatcher, following artifact should be added to project runtime dependencies:
     *  - `kotlinx-coroutines-android` for Android Main thread dispatcher
     *  - `kotlinx-coroutines-javafx` for JavaFx Application thread dispatcher
     *  - `kotlinx-coroutines-swing` for Swing EDT dispatcher
     *
     * Implementation note: [MainCoroutineDispatcher.immediate] is not supported on Native and JS platforms.
     */
    val Main: CoroutineDispatcher
        get() = delegate.Main

    /**
     * The [CoroutineDispatcher] that is designed for offloading blocking IO tasks to a shared pool of threads.
     *
     * Additional threads in this pool are created and are shutdown on demand.
     * The number of threads used by this dispatcher is limited by the value of
     * "`kotlinx.coroutines.io.parallelism`" ([IO_PARALLELISM_PROPERTY_NAME]) system property.
     * It defaults to the limit of 64 threads or the number of cores (whichever is larger).
     *
     * This dispatcher shares threads with a [Default][Dispatchers.Default] dispatcher, so using
     * `withContext(Dispatchers.IO) { ... }` does not lead to an actual switching to another thread &mdash;
     * typically execution continues in the same thread.
     */
    val IO: CoroutineDispatcher
        get() = delegate.IO

    /**
     * A coroutine dispatcher that is not confined to any specific thread.
     * It executes initial continuation of the coroutine _immediately_ in the current call-frame
     * and lets the coroutine resume in whatever thread that is used by the corresponding suspending function, without
     * mandating any specific threading policy.
     * **Note: use with extreme caution, not for general code**.
     *
     * Note, that if you need your coroutine to be confined to a particular thread or a thread-pool after resumption,
     * but still want to execute it in the current call-frame until its first suspension, then you can use
     * an optional [CoroutineStart] parameter in coroutine builders like
     * [launch][CoroutineScope.launch] and [async][CoroutineScope.async] setting it to the
     * the value of [CoroutineStart.UNDISPATCHED].
     *
     * **Note: This is an experimental api.**
     * Semantics, order of execution, and particular implementation details of this dispatcher may change in the future.
     */
    @ExperimentalCoroutinesApi
    val Unconfined: CoroutineDispatcher
        get() = delegate.Unconfined

    private object DefaultDelegate : Delegate {
        override val Default = kotlinx.coroutines.Dispatchers.Default
        override val Main = kotlinx.coroutines.Dispatchers.Main
        override val IO = kotlinx.coroutines.Dispatchers.IO
        override val Unconfined = kotlinx.coroutines.Dispatchers.Unconfined
    }

    @VisibleForTesting
    interface Delegate {
        val Default: CoroutineDispatcher
        val Main: CoroutineDispatcher
        val IO: CoroutineDispatcher
        val Unconfined: CoroutineDispatcher
    }
}
