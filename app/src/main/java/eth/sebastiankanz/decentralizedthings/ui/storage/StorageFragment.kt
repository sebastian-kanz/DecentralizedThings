package eth.sebastiankanz.decentralizedthings.ui.storage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileObserver
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import eth.sebastiankanz.decentralizedthings.R
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.databinding.FragmentStorageBinding
import eth.sebastiankanz.decentralizedthings.helpers.zipLiveData
import kotlinx.android.synthetic.main.dialog_filter_layout.view.*
import kotlinx.android.synthetic.main.dialog_input_layout.view.*
import kotlinx.android.synthetic.main.dialog_sorting_layout.view.*
import kotlinx.android.synthetic.main.dialog_sorting_layout.view.radioGroup
import kotlinx.android.synthetic.main.fragment_storage.view.*
import org.koin.android.ext.android.inject
import java.util.logging.Logger

class StorageFragment : Fragment() {

    companion object {
        private val LOGGER = Logger.getLogger("StorageFragment")

        private const val PICKFILE_RESULT_CODE = 1234

        private val isSyncedFilter: (File) -> Boolean = { it.syncState == SyncState.SYNCED }
        private val isUnsyncedOnlyLocalFilter: ((File)) -> Boolean = { it.syncState == SyncState.UNSYNCED_ONLY_LOCAL }
        private val isUnsyncedOnlyRemoteFilter: ((File)) -> Boolean = { it.syncState != SyncState.UNSYNCED_ONLY_REMOTE }
        private val isFirstVersionFilter: ((File)) -> Boolean = { it.version == 1 }
        private val isFileFilter: ((File)) -> Boolean = { it.files.isEmpty() }
        private val isDirectoryFilter: ((File)) -> Boolean = { it.files.isNotEmpty() }

        private val allFilters = mapOf(
            FILTER.NONE to { true },
            FILTER.SYNCED to isSyncedFilter,
            FILTER.UNSYNCED_ONLY_LOCAL to isUnsyncedOnlyLocalFilter,
            FILTER.UNSYNCED_ONLY_REMOTE to isUnsyncedOnlyRemoteFilter,
            FILTER.FIRST_VERSION to isFirstVersionFilter,
            FILTER.FILE to isFileFilter,
            FILTER.DIRECTORY to isDirectoryFilter
        )

        enum class FILTER {
            NONE, SYNCED, UNSYNCED_ONLY_LOCAL, UNSYNCED_ONLY_REMOTE, FIRST_VERSION, FILE, DIRECTORY
        }

        private val sortByName: (File) -> String = { it.name }
        private val sortByDate: (File) -> String = { it.timestamp.toString() }
        private val sortBySize: (File) -> String = { it.decryptedSize.toString() }
        private val sortByFilesCount: (File) -> String = { it.files.size.toString() }
        private val sortBySyncState: (File) -> String = { it.syncState.state.toString() }

        private val allSortings = mapOf(
            SORTING.NAME to sortByName,
            SORTING.DATE to sortByDate,
            SORTING.SIZE to sortBySize,
            SORTING.FILE_COUNT to sortByFilesCount,
            SORTING.SYNC_STATE to sortBySyncState
        )

        enum class SORTING {
            NAME, DATE, SIZE, FILE_COUNT, SYNC_STATE
        }
    }

    private val storageViewModel: StorageViewModel by inject()
    private lateinit var binding: FragmentStorageBinding

    private lateinit var directoryObserver: FileObserver

    private lateinit var storageAdapter: StorageCardAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var currentAsset: File? = null

    private var isCurrentlyModifying = false

    private val currentFilters = MutableLiveData<List<FILTER>>().apply { postValue(listOf(FILTER.NONE)) }
    private val currentSorting = MutableLiveData<Pair<Boolean, SORTING>>().apply { postValue(Pair(true, SORTING.NAME)) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_storage, container, false)
        val view = binding.root

        zipLiveData(storageViewModel.latestAllFilesLiveData, currentFilters, currentSorting).observe(
            viewLifecycleOwner,
            { (files, filters, sortingPair) ->
                storageAdapter.apply {
                    var filtered = files
                    filters.forEach { filter ->
                        filtered = filtered.filter(allFilters.getValue(filter))
                    }
                    val sorting = allSortings.getValue(sortingPair.second)
                    val filteredAndOrdered = if (sortingPair.first) {
                        filtered.sortedBy(sorting)
                    } else {
                        filtered.sortedByDescending(sorting)
                    }
                    setItems(filteredAndOrdered)
                }
            }
        )

        initRecyclerView(emptyList())

        view.fab.setOnClickListener { root ->
            binding.root.loadingBar.visibility = View.VISIBLE
            var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a file")
            startActivityForResult(chooseFile, 1234)
        }
        directoryObserver = object : FileObserver(context?.getExternalFilesDir(null)?.path, ALL_EVENTS) {
            override fun onEvent(event: Int, path: String?) {
                if (!isCurrentlyModifying) {
                    when (event) {
                        DELETE -> {
                            Snackbar.make(binding.root, "File content deleted: " + path.toString(), Snackbar.LENGTH_LONG)
                                .setAnchorView(view.fab)
                                .show()
                            val deletedLocalFile =
                                storageViewModel.allFilesLiveData.value?.find { it.localPath == context?.getExternalFilesDir(null)?.path + "/" + path }
                            storageViewModel.onFileDeletedExternally(deletedLocalFile)
                        }
                        MODIFY -> {
                            Snackbar.make(binding.root, "File content modified: " + path.toString(), Snackbar.LENGTH_LONG)
                                .setAnchorView(view.fab)
                                .show()
                            val modifiedLocalFile =
                                storageViewModel.allFilesLiveData.value?.find { it.localPath == context?.getExternalFilesDir(null)?.path + "/" + path }
                            storageViewModel.onFileModifiedExternally(modifiedLocalFile)
                        }
                    }
                }
            }
        }

        directoryObserver.startWatching()

        initBottomSheet()

        initTopAppBar()

        initBottomAppBar()

        return view
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root.bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // handle onSlide
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // handle onStateChanged
            }
        })

        binding.buttonAction1.setOnClickListener {
            onBottomSheetAction1()
        }
        binding.buttonAction2.setOnClickListener {
            onBottomSheetAction2()
        }
        binding.buttonAction3.setOnClickListener {
            onBottomSheetAction3()
        }
        binding.buttonAction4.setOnClickListener {
            onBottomSheetAction4()
        }
    }

    fun toggleBottomSheet(file: File) {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            if (currentAsset == file) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                currentAsset = file
            }
        } else {
            currentAsset = file
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun onBottomSheetAction1() {
        onModifyingStarted()
        currentAsset?.let { syncFileContent(it) }
        onBottomSheetActionCompleted()
    }

    private fun onBottomSheetAction2() {
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_input_layout, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attention")
            .setMessage("Please enter new file name:")
            .setView(dialogView)
            .setNegativeButton("Cancel") { dialog, _ ->
                // Respond to negative button press
                dialog.dismiss()
            }
            .setPositiveButton("Rename") { _, _ ->
                onModifyingStarted()
                currentAsset?.let {
                    val newName = dialogView.input1.text.toString() + "." + it.name.substringAfterLast(".")
                    storageViewModel.renameFileLocally(it, newName).observe(viewLifecycleOwner, { file ->
                        if (file != null) {
                            onModifyingCompleted()
                        }
                    })
                }
                onBottomSheetActionCompleted()
            }
            .show()
    }

    private fun onBottomSheetAction3() {
        onModifyingStarted()
        currentAsset?.let { storageViewModel.deleteFileLocally(it) }
        onBottomSheetActionCompleted()
        onModifyingCompleted()
    }

    private fun onBottomSheetAction4() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attention")
            .setMessage("Do you really want to delete this file completely? This process cannot be reverted.")
            .setNegativeButton("Cancel") { dialog, _ ->
                // Respond to negative button press
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                // Respond to positive button press
                onModifyingStarted()
                currentAsset?.let { storageViewModel.deleteFile(it) }
                onBottomSheetActionCompleted()
                onModifyingCompleted()
            }
            .show()
    }

    private fun initTopAppBar() {
        binding.root.topAppBar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.account -> {
                    Snackbar.make(binding.root, "Account...", Snackbar.LENGTH_SHORT)
                        .setAnchorView(binding.root.fab)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun initBottomAppBar() {
        binding.root.bottomAppBar.setNavigationOnClickListener {
            binding.root.drawerLayout.openDrawer(GravityCompat.START, true)
        }

        binding.root.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    Snackbar.make(binding.root, "Searching...", Snackbar.LENGTH_SHORT)
                        .setAnchorView(binding.root.fab)
                        .show()
                    true
                }
                R.id.filter -> {
                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_filter_layout, null)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Filter")
                        .setMessage("Select a filter:")
                        .setView(dialogView)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setNeutralButton("Clear Filter") { _, _ ->
                            currentFilters.postValue(listOf(FILTER.NONE))
                        }
                        .setPositiveButton("Apply Filter") { _, _ ->
                            val filters = mutableListOf<FILTER>()
                            if (dialogView.radioGroup.cBisSyncedFilter.isChecked) filters.add(FILTER.SYNCED)
                            if (dialogView.radioGroup.cBisUnsyncedOnlyLocalFilter.isChecked) filters.add(FILTER.UNSYNCED_ONLY_LOCAL)
                            if (dialogView.radioGroup.cBisUnsyncedOnlyRemoteFilter.isChecked) filters.add(FILTER.UNSYNCED_ONLY_REMOTE)
                            if (dialogView.radioGroup.cBisFirstVersionFilter.isChecked) filters.add(FILTER.FIRST_VERSION)
                            if (dialogView.radioGroup.cBisFileFilter.isChecked) filters.add(FILTER.FILE)
                            if (dialogView.radioGroup.cBisDirectoryFilter.isChecked) filters.add(FILTER.DIRECTORY)
                            currentFilters.postValue(filters)
                        }
                        .show()
                    true
                }
                R.id.sort -> {
                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_sorting_layout, null)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Sorting")
                        .setMessage("Select a sorting strategy:")
                        .setView(dialogView)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Apply Sorting") { _, _ ->
                            val sorting = when (dialogView.radioGroup.checkedRadioButtonId) {
                                R.id.rBsortByDate -> SORTING.DATE
                                R.id.rBsortBySize -> SORTING.SIZE
                                R.id.rBsortByFilesCount -> SORTING.FILE_COUNT
                                R.id.rBsortbySyncState -> SORTING.SYNC_STATE
                                else -> SORTING.NAME
                            }
                            val sortAscending = dialogView.cBsortAscending.isChecked
                            currentSorting.postValue(Pair(sortAscending, sorting))
                        }
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun onModifyingStarted() {
        isCurrentlyModifying = true
    }

    private fun onModifyingCompleted() {
        isCurrentlyModifying = false
    }

    private fun onBottomSheetActionCompleted() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        currentAsset = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        directoryObserver.stopWatching()
    }

    private fun initRecyclerView(files: List<File>) {
        storageAdapter = StorageCardAdapter(
            storageViewModel,
            this,
            files
        ).apply {
            registerAdapterDataObserver(object :
                RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                    Log.i("test123", "onItemRangeInserted " + positionStart.toString())
                    binding.storageRecyclerView.scrollToPosition(positionStart)
                }
            })
        }

        binding.storageRecyclerView.apply {
            adapter = storageAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator().apply {
                supportsChangeAnimations = false
            }
        }
    }

    private fun syncFileContent(file: File) {
        when (file.syncState) {
            SyncState.UNSYNCED_ONLY_REMOTE -> storageViewModel.syncFileContentFromIPFS(file).observe(viewLifecycleOwner, {
                if (it?.syncState == SyncState.SYNCED) {
                    onModifyingCompleted()
                }
            })
            SyncState.UNSYNCED_ONLY_LOCAL -> storageViewModel.syncFileContentToIPFS(file).observe(viewLifecycleOwner, {
                if (it?.syncState == SyncState.SYNCED) {
                    onModifyingCompleted()
                }
            })
            else -> LOGGER.warning("Nothing to sync.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICKFILE_RESULT_CODE ->
                if (resultCode == -1) {
                    data?.data?.let { returnUri ->
                        val istream = context?.contentResolver?.openInputStream(returnUri)
                        val fileContent = istream?.readBytes() ?: ByteArray(0)
                        istream?.close()
                        context?.contentResolver?.query(returnUri, null, null, null, null)?.let { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            val fileName = cursor.getString(nameIndex).substringBeforeLast(".")
                            val fileType = cursor.getString(nameIndex).substringAfterLast(".")
                            cursor.close()
                            createFile(fileName, fileType, fileContent, returnUri)
                        }
                    }
                } else {
                    binding.root.loadingBar.visibility = View.GONE
                }
        }
    }

    private fun createFile(fileName: String, fileType: String, fileContent: ByteArray, localFileUri: Uri) {
        storageViewModel.createFile(fileName, fileType, fileContent).observe(viewLifecycleOwner, {
            binding.root.loadingBar.visibility = View.GONE
            if (it == null) {
                Snackbar.make(binding.root, "File could not be created!", Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.fab)
                    .setAction("action") {
                        // Responds to click on the action
                    }
                    .show()
            }
        })
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attention")
            .setMessage("Do you want to import the file or keep a local copy?")
            .setNeutralButton("Import") { _, _ ->
                //val test = java.io.File(localFileUri.pat)
                //Log.i("test123", test.toString())
                //Log.i("test123", test.delete().toString())
            }
            .setPositiveButton("Copy") { _, _ ->

            }
            .show()
    }
}
