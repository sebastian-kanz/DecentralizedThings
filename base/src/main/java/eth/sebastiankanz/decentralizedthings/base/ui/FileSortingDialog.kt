package eth.sebastiankanz.decentralizedthings.base.ui

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import eth.sebastiankanz.decentralizedthings.base.databinding.LayoutFileSortingDialogBinding
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSorting
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.operators.FileSortingOrder
import kotlinx.serialization.json.Json

class FileSortingDialog(
    private val currentSorting: FileSorting,
    private val currentSortingOrder: FileSortingOrder,
    private val requestKey: String
) : FullScreenDialog() {
    private var _binding: LayoutFileSortingDialogBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Trying to access the binding outside of the view lifecycle.")

    private val allSortings = mapOf(
        toFileSortingMapPair(FileSorting.FileNameSorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileNameSorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileTimestampSorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileSizeSorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileContentHashSorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileIsDirectorySorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileMetaHashSorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileSyncStateSorting(FileSortingOrder.ASC)),
        toFileSortingMapPair(FileSorting.FileVersionSorting(FileSortingOrder.ASC)),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutFileSortingDialogBinding.inflate(inflater, container, false)

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            allSortings.keys.toTypedArray()
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.sortingSpinner.adapter = it
            binding.sortingSpinner.setSelection(it.getPosition(toFileSortingMapPair(currentSorting).first))
            binding.switchAsc.isChecked = currentSortingOrder == FileSortingOrder.ASC
        }

        binding.saveButton.setOnClickListener {
            val sorting = allSortings.getOrDefault(binding.sortingSpinner.selectedItem as String, allSortings.values.first())
            sorting.order = if (binding.switchAsc.isEnabled) FileSortingOrder.ASC else FileSortingOrder.DESC
            val result = Json.encodeToString(FileSorting.serializer(), sorting)
            parentFragmentManager.setFragmentResult(requestKey, bundleOf(requestKey to result))
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun toFileSortingMapPair(fileSorting: FileSorting): Pair<String, FileSorting> {
        return Pair(fileSorting::class.java.simpleName.substringBefore("Sorting").replace("File", ""), fileSorting)
    }

    companion object {
        private const val FRAGMENT_TAG = "FileSortingDialog"

        fun show(fragmentManager: FragmentManager, requestKey: String, sorting: FileSorting, order: FileSortingOrder): FileSortingDialog {
            val dialog = FileSortingDialog(sorting, order, requestKey)
            dialog.show(fragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }
}