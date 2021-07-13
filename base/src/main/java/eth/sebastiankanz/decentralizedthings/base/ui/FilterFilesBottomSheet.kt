package eth.sebastiankanz.decentralizedthings.base.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eth.sebastiankanz.decentralizedthings.base.databinding.LayoutFilterFilesDialogBinding

class FilterFilesBottomSheet(
    private val requestKey: String
) : BottomSheetDialogFragment() {
    private var _binding: LayoutFilterFilesDialogBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Trying to access the binding outside of the view lifecycle.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutFilterFilesDialogBinding.inflate(inflater, container, false)

        binding.saveButton.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    companion object {
        private const val FRAGMENT_TAG = "FilterFilesBottomSheet"

        fun show(fragmentManager: FragmentManager, requestKey: String): FilterFilesBottomSheet {
            val dialog = FilterFilesBottomSheet(requestKey)
            dialog.show(fragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }
}