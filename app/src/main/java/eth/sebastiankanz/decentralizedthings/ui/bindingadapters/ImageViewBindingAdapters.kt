package eth.sebastiankanz.decentralizedthings.ui.bindingadapters

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import eth.sebastiankanz.decentralizedthings.R
import eth.sebastiankanz.decentralizedthings.data.model.SyncState

/**
 * Binds [Drawable] to [ImageView].
 */
@BindingAdapter("fileTypeImage")
fun ImageView.loadFileTypeImage(type: String) {
    val drawableID = getResources().getIdentifier("type_" + type, "drawable", context.getPackageName())
    if(drawableID == 0) {
        setImageDrawable(resources.getDrawable(R.drawable.loading, null))
    } else {
        setImageDrawable(resources.getDrawable(drawableID, null))
    }
}


@BindingAdapter("syncStateImage")
fun ImageView.loadSyncStateImage(state: SyncState) {
    val drawable = when(state) {
        SyncState.SYNCED -> resources.getDrawable(R.drawable.ic_sync, null)
        SyncState.UNSYNCED_ONLY_LOCAL -> resources.getDrawable(R.drawable.ic_sync_local, null)
        SyncState.UNSYNCED_ONLY_PARTLY -> resources.getDrawable(R.drawable.ic_sync_none, null)
        SyncState.UNSYNCED_ONLY_REMOTE -> resources.getDrawable(R.drawable.ic_sync_remote, null)
        SyncState.NONE -> resources.getDrawable(R.drawable.ic_sync_none, null)
    }
    setImageDrawable(drawable)
}
