package eth.sebastiankanz.decentralizedthings.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@ExperimentalUnsignedTypes // just to make it clear that the experimental unsigned types are used
fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

fun String.isValidIPFSHash(): Boolean {
    return this != ""
}

fun Double.format(digits: Int) = String.Companion.format(
    java.util.Locale.GERMAN,
    "%#,.${digits}f",
    this
)
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun String.getMetaFileNameFromFileName() = "${this.substringBeforeLast(".")}.meta.json"