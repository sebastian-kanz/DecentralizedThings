package eth.sebastiankanz.decentralizedthings.helper

import android.annotation.SuppressLint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
@SuppressLint("VisibleForTests")
class InstantDispatchersRule : TestWatcher() {

    private val testDelegate = object : Dispatchers.Delegate {
        override val Default = kotlinx.coroutines.Dispatchers.Unconfined
        override val Main = kotlinx.coroutines.Dispatchers.Unconfined
        override val IO = kotlinx.coroutines.Dispatchers.Unconfined
        override val Unconfined = kotlinx.coroutines.Dispatchers.Unconfined
    }

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setDelegate(testDelegate)
        // when using the new viewModelScope, we cannot avoid using the original Dispatchers.Main, so we need to replace it.
        kotlinx.coroutines.Dispatchers.setMain(testDelegate.Main)
    }

    override fun finished(description: Description?) {
        super.finished(description)

        Dispatchers.setDelegate(null)
    }
}