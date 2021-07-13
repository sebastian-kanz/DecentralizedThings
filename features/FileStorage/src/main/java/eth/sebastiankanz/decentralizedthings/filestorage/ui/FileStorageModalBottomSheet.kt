package eth.sebastiankanz.decentralizedthings.filestorage.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eth.sebastiankanz.decentralizedthings.filestorage.R

class FileStorageModalBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_filter_layout, container, false)
//    ): View? = inflater.inflate(R.layout.modal_bottom_sheet, container, false)

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}