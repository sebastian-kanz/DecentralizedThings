package eth.sebastiankanz.decentralizedthings.filestorage.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import eth.sebastiankanz.decentralizedthings.filestorage.databinding.FragmentFileStorageDetailBinding

class FileStorageDetailFragment : Fragment() {

//    companion object {
//        private val LOGGER = Logger.getLogger("FileStorageFragment")
//        private const val PICKFILE_RESULT_CODE = 1234
//    }

//    private val storageViewModel: FileStorageViewModel by inject()

    private var _binding: FragmentFileStorageDetailBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Trying to access the binding outside of the view lifecycle.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentFileStorageDetailBinding.inflate(inflater, container, false).also { _binding = it }.root

}
