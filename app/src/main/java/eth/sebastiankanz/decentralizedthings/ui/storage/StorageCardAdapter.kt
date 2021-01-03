package eth.sebastiankanz.decentralizedthings.ui.storage

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eth.sebastiankanz.decentralizedthings.R
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.databinding.StorageCardViewBinding

class StorageCardAdapter(
    private val viewModel: StorageViewModel,
    private val fragment: StorageFragment,
    private var files: List<File>
) : RecyclerView.Adapter<StorageCardAdapter.StorageListViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class StorageListViewHolder(
        val binding: StorageCardViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setCardClickListener(listener: View.OnClickListener) {
            binding.storageCardView.setOnClickListener(listener)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StorageListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // set the view's size, margins, paddings and layout parameters

        return StorageListViewHolder(
            StorageCardViewBinding.inflate(
                inflater,
                parent,
                false
            )
        )
            .apply {
                // do some initial setup that is identical for all elements
            }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: StorageListViewHolder, position: Int) {
        holder.setCardClickListener(View.OnClickListener {
            val clickedItem = files.get(position)
            Log.i("test123", clickedItem.toString())
            //fragment.syncFileContent(clickedItem)
            fragment.toggleBottomSheet(clickedItem)
            //fragment.showFileDetails(clickedItem)
        })

        holder.binding.fileName.text = files.get(position).name
        holder.binding.size = files.get(position).decryptedSize
        holder.binding.creation = files.get(position).timestamp
        val syncImage = when (files.get(position).syncState) {
            SyncState.NONE -> R.drawable.ic_sync_none
            SyncState.UNSYNCED_ONLY_PARTLY -> R.drawable.ic_sync_none
            SyncState.UNSYNCED_ONLY_REMOTE -> R.drawable.ic_sync_remote
            SyncState.UNSYNCED_ONLY_LOCAL -> R.drawable.ic_sync_local
            SyncState.SYNCED -> R.drawable.ic_sync
        }
        holder.binding.syncImage.setImageResource(syncImage)
        holder.binding.type = files.get(position).name.substringAfter(".")
        holder.binding.syncState = files.get(position).syncState
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = files.size

    fun setItems(allFiles: List<File>) {
        files = allFiles
        notifyDataSetChanged()
    }

    fun getItemIndex(file: File): Int {
        val index = files.indexOf(file)
        return if(index < 0) 0 else index
    }
}