package eth.sebastiankanz.decentralizedthings.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import eth.sebastiankanz.decentralizedthings.R
import eth.sebastiankanz.decentralizedthings.databinding.FragmentGalleryBinding
import org.koin.android.ext.android.inject

class GalleryFragment : Fragment() {

    private val galleryViewModel: GalleryViewModel by inject()

    private lateinit var binding: FragmentGalleryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)

        val view = binding.root
        val textView: TextView = view.findViewById(R.id.text_gallery)

        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return view
    }
}