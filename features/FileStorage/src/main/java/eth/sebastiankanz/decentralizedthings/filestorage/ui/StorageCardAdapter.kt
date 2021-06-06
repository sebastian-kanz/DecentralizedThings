package eth.sebastiankanz.decentralizedthings.filestorage.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.filestorage.R
import eth.sebastiankanz.decentralizedthings.filestorage.databinding.StorageCardViewBinding
import kotlinx.android.synthetic.main.storage_card_view.view.*

class StorageCardAdapter(
    private val viewModel: FileStorageViewModel,
    private val fragment: FileStorageFragment,
    private var files: List<File>
) : RecyclerView.Adapter<StorageCardAdapter.StorageListViewHolder>() {

    inner class StorageListViewHolder(
        private val binding: StorageCardViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            with(binding) {
                root.fileName.text = file.name
                size = file.size
                creation = file.timestamp
                val image = when (file.syncState) {
                    SyncState.NONE -> R.drawable.ic_sync_none
                    SyncState.UNSYNCED_ONLY_PARTLY -> R.drawable.ic_sync_none
                    SyncState.UNSYNCED_ONLY_REMOTE -> R.drawable.ic_sync_remote
                    SyncState.UNSYNCED_ONLY_LOCAL -> R.drawable.ic_sync_local
                    SyncState.SYNCED -> R.drawable.ic_sync
                }
                root.syncImage.setImageResource(image)
                type = file.name.substringAfter(".")
                syncState = file.syncState
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StorageListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StorageCardViewBinding.inflate(inflater)
        return StorageListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StorageListViewHolder, position: Int) = holder.bind(files[position])

    override fun getItemCount() = files.size

    fun setItems(allFiles: List<File>) {
        files = allFiles
        notifyDataSetChanged()
    }

    fun getItem(index: Int) = files[index]
}