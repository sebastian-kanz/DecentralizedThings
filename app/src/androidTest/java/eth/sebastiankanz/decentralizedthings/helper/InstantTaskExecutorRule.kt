package eth.sebastiankanz.decentralizedthings.helper

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import org.junit.runner.Description

@SuppressLint("VisibleForTests")
class InstantTaskExecutorRule : androidx.arch.core.executor.testing.InstantTaskExecutorRule() {

    private val testThreadUtils = object : IThreadUtils {
        override fun postToMainThread(function: () -> Unit) {
            function()
        }

        override fun postDelayedToMainThread(delayMilliseconds: Long, function: () -> Unit) {
            Thread {
                Thread.sleep(delayMilliseconds)
                function()
            }.start()
        }

        override fun isMainThread(): Boolean =
            true
    }

    override fun starting(description: Description?) {
        super.starting(description)
        ThreadUtils.setDelegate(testThreadUtils)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        ThreadUtils.setDelegate(null)
    }
}

/**
 * For the purpose of testing we need to avoid calling Looper.getMainLooper() directly.
 * This class can delegate this call at test time to a delegate. Use the InstantTaskExecutorRule for that.
 */
object ThreadUtils : IThreadUtils {

    private var delegate: IThreadUtils = AndroidThreadUtils

    @VisibleForTesting
    fun setDelegate(threadUtils: IThreadUtils?) {
        delegate = threadUtils ?: AndroidThreadUtils
    }

    override fun isMainThread(): Boolean =
        delegate.isMainThread()

    override fun postToMainThread(function: () -> Unit) =
        delegate.postToMainThread(function)

    fun postToMainThread(function: Runnable) = postToMainThread { function.run() }

    override fun postDelayedToMainThread(delayMilliseconds: Long, function: () -> Unit) =
        delegate.postDelayedToMainThread(delayMilliseconds, function)
}

private object AndroidThreadUtils : IThreadUtils {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun postToMainThread(function: () -> Unit) {
        handler.post(function)
    }

    override fun postDelayedToMainThread(delayMilliseconds: Long, function: () -> Unit) {
        handler.postDelayed(function, delayMilliseconds)
    }

    override fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}

interface IThreadUtils {
    fun isMainThread(): Boolean
    fun postToMainThread(function: () -> Unit)
    fun postDelayedToMainThread(delayMilliseconds: Long, function: () -> Unit)
}
