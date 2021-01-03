package eth.sebastiankanz.decentralizedthings.ui.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import eth.sebastiankanz.decentralizedthings.R
import eth.sebastiankanz.decentralizedthings.data.model.File
import eth.sebastiankanz.decentralizedthings.databinding.FragmentStorageDetailBinding
import org.koin.android.ext.android.inject

class StorageDetailFragment(
    file: File
) : Fragment() {

    private val storageDetailViewModel: StorageDetailViewModel by inject()
    private lateinit var binding: FragmentStorageDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_storage_detail, container, false)

        val view = binding.root

        return view
    }
}