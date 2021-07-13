package eth.sebastiankanz.decentralizedthings.filestorage.ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.FileObserver
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.FileError
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSortingOrder
import eth.sebastiankanz.decentralizedthings.base.helpers.RecyclerTouchListener
import eth.sebastiankanz.decentralizedthings.base.helpers.RecyclerTouchListener.OnRowClickListener
import eth.sebastiankanz.decentralizedthings.base.ui.EditFileBottomSheet
import eth.sebastiankanz.decentralizedthings.base.ui.FileInfoBottomSheet
import eth.sebastiankanz.decentralizedthings.base.ui.FilterFilesBottomSheet
import eth.sebastiankanz.decentralizedthings.base.ui.SortFilesBottomSheet
import eth.sebastiankanz.decentralizedthings.filestorage.R
import eth.sebastiankanz.decentralizedthings.filestorage.databinding.FragmentFileStorageBinding
import eth.sebastiankanz.decentralizedthings.ui.MainActivity
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import java.util.logging.Logger

class FileStorageFragment : Fragment() {

    companion object {
        private val LOGGER = Logger.getLogger("FileStorageFragment")
        private const val PICKFILE_RESULT_CODE = 1234
    }

    private val storageViewModel: FileStorageViewModel by inject()

    private var _binding: FragmentFileStorageBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Trying to access the binding outside of the view lifecycle.")

    private lateinit var directoryObserver: FileObserver
    private lateinit var storageAdapter: StorageCardAdapter

    private var fabOpenAnimation: Animation? = null
    private var fabCloseAnimation: Animation? = null
    private var isFabMenuOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentFileStorageBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).onFragmentLoaded()
        collapseFabMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        storageViewModel.latestAllFilesLiveData.observe(viewLifecycleOwner) { files ->
            if(files.isEmpty()) {
                binding.emptyContainer.visibility = View.VISIBLE
                binding.storageRecyclerView.visibility = View.GONE
            } else {
                binding.emptyContainer.visibility = View.GONE
                binding.storageRecyclerView.visibility = View.VISIBLE
                storageAdapter.apply {
                    setItems(files)
                }
            }
        }
    }

    private fun init() {
        initErrorHandling()
        initRecyclerView(emptyList())
        initDirectoryObserver()
        initFab()
        initLoadingBar()
    }

    private fun initLoadingBar() {
        storageViewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            if (isProcessing) {
                binding.loadingBar.visibility = View.VISIBLE
            } else {
                binding.loadingBar.visibility = View.GONE
            }
        }
    }

    private fun initDirectoryObserver() {
        //todo: add directory observers for all directories not only for the root directory
        directoryObserver = object : FileObserver(context?.getExternalFilesDir(null)?.path, ALL_EVENTS) {
            override fun onEvent(event: Int, path: String?) {
                when (event) {
                    DELETE -> {
//                        Snackbar.make(binding.root, "File content deleted: " + path.toString(), Snackbar.LENGTH_SHORT)
//                            .setAnchorView(binding.fab)
//                            .show()
                        val deletedLocalFile =
                            storageViewModel.allFilesLiveData.value?.find { it.localPath == context?.getExternalFilesDir(null)?.path + "/" + path }
                        storageViewModel.onFileDeletedExternally(deletedLocalFile)
                    }
                    MODIFY -> {
//                        Snackbar.make(binding.root, "File content modified: " + path.toString(), Snackbar.LENGTH_SHORT)
//                            .setAnchorView(binding.fab)
//                            .show()
                        val modifiedLocalFile =
                            storageViewModel.allFilesLiveData.value?.find { it.localPath == context?.getExternalFilesDir(null)?.path + "/" + path }
                        storageViewModel.onFileModifiedExternally(modifiedLocalFile)
                    }
                    CREATE -> {
                        //todo: if created asset is directory, add directory observer
                    }
                }
            }
        }

        directoryObserver.startWatching()
    }

    private fun initFab() {

        fabOpenAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_open)
        fabCloseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_close)
        val fabHandler = FabHandler()
        binding.fab.setOnClickListener {
            fabHandler.onBaseFabClick(it)
        }
        binding.createFab.setOnClickListener {
            fabHandler.onCreateFabClick(it)
            collapseFabMenu()
        }
        binding.filterFab.setOnClickListener {
            fabHandler.onFilterFabClick(it)
            collapseFabMenu()
        }
        binding.searchFab.setOnClickListener {
            fabHandler.onSearchFabClick(it)
            collapseFabMenu()
        }
        binding.sortFab.setOnClickListener {
            fabHandler.onSortFabClick(it)
            collapseFabMenu()
        }
    }

    private fun initErrorHandling() {
        storageViewModel.error.observe(viewLifecycleOwner, {
            when (it) {
                is FileError.GeneralFileError -> showErrorMessage(it.msg)
                else -> showErrorMessage("Unknown error.")
            }
            binding.loadingBar.visibility = View.GONE
        })
    }

    private fun showErrorMessage(message: String?) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage("An error occurred: $message")
            .setNeutralButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showHint(message: String?) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hint")
            .setMessage("Please note: $message")
            .setNeutralButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteDialog(file: File) {
        val singleItems = arrayOf("only locally", "completely (irreversible!)")
        val checkedItem = 0
        var onlyLocally = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Are you sure you want to delete this file?")
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                storageViewModel.deleteFile(file, onlyLocally)
            }
            .setSingleChoiceItems(singleItems, checkedItem) { _, which ->
                when (which) {
                    0 -> onlyLocally = true
                    1 -> onlyLocally = false
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                    binding.storageRecyclerView.scrollToPosition(positionStart)
                }
            })
        }
        val recyclerView = binding.storageRecyclerView.apply {
            adapter = storageAdapter
//            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator().apply {
                supportsChangeAnimations = false
            }
        }
        val itemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        context?.apply {
            itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)
        }
        recyclerView.addItemDecoration(itemDecoration)

        val touchListener = RecyclerTouchListener(activity, recyclerView)
        touchListener
            .setClickable(object : OnRowClickListener {
                override fun onRowClicked(position: Int) {
                    openFile(storageAdapter.getItem(position))
                }

                override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
            })
            .setSwipeOptionViews(R.id.delete_task, R.id.edit_task, R.id.info_task)
            .setSwipeable(R.id.itemContainer, R.id.rowBG) { viewID, position ->
                val affectedFile = storageAdapter.getItem(position)
                when (viewID) {
                    R.id.delete_task -> {
                        showDeleteDialog(affectedFile)
                    }
                    R.id.edit_task -> {
                        requireActivity().supportFragmentManager.setFragmentResultListener(affectedFile.metaHash, viewLifecycleOwner) { requestKey, bundle ->
                            if (requestKey == affectedFile.metaHash) {
                                val result = bundle.getString(requestKey, "")
                                val file = Json.decodeFromString(File.serializer(), result)
                                storageViewModel.editFile(affectedFile, file)
                            }
                        }
                        EditFileBottomSheet.show(requireActivity().supportFragmentManager, affectedFile, affectedFile.metaHash)
//                        EditFileDialog.show(requireActivity().supportFragmentManager, affectedFile, affectedFile.metaHash)
                    }
                    R.id.info_task -> {
                        (activity as MainActivity).showFragment(FileStorageDetailFragment())
                    }
                }
            }
            .setLongClickable(true) { position ->
                val affectedFile = storageAdapter.getItem(position)
                storageViewModel.saveQRCode(requireContext().getExternalFilesDir(null).toString(), affectedFile)
                LOGGER.info(Json.encodeToString(File.serializer(), affectedFile))
            }

        recyclerView.addOnItemTouchListener(touchListener)
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
                    binding.loadingBar.visibility = View.GONE
                }
        }
    }

    fun openFile(file: File) {
        if (file.syncState == SyncState.SYNCED) {
            val context = requireContext()
            val fileToOpen = java.io.File(context.getExternalFilesDir(null), file.name)
            val data = FileProvider.getUriForFile(context, "eth.sebastiankanz.myprovider", fileToOpen)
            context.grantUriPermission(context.packageName, data, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, "*/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } else {
            showHint("Please sync file before opening!")
        }
    }

    private fun createFile(fileName: String, fileType: String, fileContent: ByteArray, localFileUri: Uri) {
        storageViewModel.createFile(fileName, fileType, fileContent).observe(viewLifecycleOwner, {
            binding.loadingBar.visibility = View.GONE
        })
    }

    private fun expandFabMenu() {
        if(!isFabMenuOpen) {
            ViewCompat.animate(binding.fab).rotation(180.0f).withLayer().setDuration(800).setInterpolator(OvershootInterpolator(5.0f)).start()
            binding.createLayout.startAnimation(fabOpenAnimation)
            binding.filterLayout.startAnimation(fabOpenAnimation)
            binding.searchLayout.startAnimation(fabOpenAnimation)
            binding.sortLayout.startAnimation(fabOpenAnimation)
            binding.createFab.isClickable = true
            binding.filterFab.isClickable = true
            binding.searchFab.isClickable = true
            binding.sortFab.isClickable = true
            isFabMenuOpen = true
        }
    }

    private fun collapseFabMenu() {
        if(isFabMenuOpen) {
            ViewCompat.animate(binding.fab).rotation(0.0f).withLayer().setDuration(800).setInterpolator(OvershootInterpolator(5.0f)).start()
            binding.createLayout.startAnimation(fabCloseAnimation)
            binding.filterLayout.startAnimation(fabCloseAnimation)
            binding.searchLayout.startAnimation(fabCloseAnimation)
            binding.sortLayout.startAnimation(fabCloseAnimation)
            binding.createFab.isClickable = false
            binding.filterFab.isClickable = false
            binding.searchFab.isClickable = false
            binding.sortFab.isClickable = false
            isFabMenuOpen = false
        }
    }

    inner class FabHandler {
        fun onBaseFabClick(view: View?) {
            if (isFabMenuOpen) collapseFabMenu() else expandFabMenu()
        }

        fun onCreateFabClick(view: View?) {
            binding.loadingBar.visibility = View.VISIBLE
            var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a file")
            startActivityForResult(chooseFile, 1234)
        }

        fun onFilterFabClick(view: View?) {
//            Snackbar.make(binding.root, "Filter FAB tapped", Snackbar.LENGTH_SHORT).show()
//            MaterialAlertDialogBuilder(requireContext())
//                .setTitle("Filter files")
//                .setMessage("Choose a filter to change the files displayed.")
//                .setNeutralButton("Cancel") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .setPositiveButton("Apply") { _, _ ->
////                    val currentFileFilters = storageViewModel.fileFilters.value?.toMutableList() ?: mutableListOf()
////                    currentFileFilters.add(FileFilter.FileSyncStateFilter(SyncState.SYNCED))
////                    storageViewModel.fileFilters.postValue(currentFileFilters)
//                    storageViewModel.showLatestFiles(false)
//                }
//                .show()

            FilterFilesBottomSheet.show(
                requireActivity().supportFragmentManager,
                "requestKey",
            )
        }

        fun onSearchFabClick(view: View?) {
            Snackbar.make(binding.root, "Search FAB tapped", Snackbar.LENGTH_SHORT).show()
        }

        fun onSortFabClick(view: View?) {
            val requestKey = "SortingFiles"
            requireActivity().supportFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { key, bundle ->
                if (key == requestKey) {
                    val result = bundle.getString(requestKey, "")
                    val sorting = Json.decodeFromString(FileSorting.serializer(), result)
                    storageViewModel.fileSorting.postValue(sorting)
                }
            }
            SortFilesBottomSheet.show(
                requireActivity().supportFragmentManager,
                requestKey,
                storageViewModel.fileSorting.value ?: FileSorting.FileNameSorting(FileSortingOrder.ASC),
                storageViewModel.fileSorting.value?.order ?: FileSortingOrder.ASC
            )
        }
    }
}
