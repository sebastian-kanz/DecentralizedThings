package eth.sebastiankanz.decentralizedthings.ui.bindingadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import eth.sebastiankanz.decentralizedthings.extensions.format
import java.text.SimpleDateFormat
import java.util.Locale

@BindingAdapter("fileCreation")
fun TextView.setfileCreation(timestamp: Long) {
    val date = java.util.Date(timestamp)
    val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    setText(simpleDateFormat.format(date).replace(" ", "\n"))
}

@BindingAdapter("fileSize")
fun TextView.setFileSize(size: Long) {
    val size = (size.toDouble() / 1024.0 / 1024.0).format(2)
    setText(size + " MB")
}
