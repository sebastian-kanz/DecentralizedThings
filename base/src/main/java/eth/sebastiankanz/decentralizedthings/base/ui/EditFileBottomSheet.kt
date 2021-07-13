package eth.sebastiankanz.decentralizedthings.base.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eth.sebastiankanz.decentralizedthings.base.R
import eth.sebastiankanz.decentralizedthings.base.data.model.SyncState
import eth.sebastiankanz.decentralizedthings.base.databinding.LayoutEditFileDialogBinding
import eth.sebastiankanz.decentralizedthings.base.features.filestorage.model.File
import kotlinx.serialization.json.Json

class EditFileBottomSheet(
    private val file: File,
    private val requestKey: String
) : BottomSheetDialogFragment() {
    private var _binding: LayoutEditFileDialogBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Trying to access the binding outside of the view lifecycle.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutEditFileDialogBinding.inflate(inflater, container, false)
        binding.editTextInput.editText?.setText(file.name.substringBeforeLast("."))
        binding.editTextInput.suffixText = "." + file.name.substringAfterLast(".")
        if (file.syncState == SyncState.SYNCED) {
            binding.radioGroup.check(R.id.rb_syncFile)
        } else {
            binding.radioGroup.check(R.id.rb_unsyncFile)
        }
        binding.saveButton.setOnClickListener {
            val newName = binding.editTextInput.editText?.text.toString() + binding.editTextInput.suffixText
            val newSyncState = if (binding.rbSyncFile.isChecked) SyncState.SYNCED else SyncState.UNSYNCED_ONLY_REMOTE
            val result = Json.encodeToString(File.serializer(), file.copy(name = newName, syncState = newSyncState))
            parentFragmentManager.setFragmentResult(requestKey, bundleOf(requestKey to result))
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    companion object {
        private const val FRAGMENT_TAG = "EditFileBottomSheet"

        fun show(fragmentManager: FragmentManager, file: File, requestKey: String): EditFileBottomSheet {
            val dialog = EditFileBottomSheet(file, requestKey)
            dialog.show(fragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }
}