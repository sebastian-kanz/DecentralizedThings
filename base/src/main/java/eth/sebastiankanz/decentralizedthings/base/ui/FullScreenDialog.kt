package eth.sebastiankanz.decentralizedthings.base.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import eth.sebastiankanz.decentralizedthings.base.R
import eth.sebastiankanz.decentralizedthings.base.databinding.LayoutFullScreenDialogBinding

open class FullScreenDialog : DialogFragment() {
    private var _binding: LayoutFullScreenDialogBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Trying to access the binding outside of the view lifecycle.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutFullScreenDialogBinding.inflate(inflater, container, false)
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun getTheme(): Int {
        return when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> R.style.DialogThemeDark
            Configuration.UI_MODE_NIGHT_NO -> R.style.DialogThemeLight
            else -> R.style.DialogThemeLight
        }
    }

    companion object {
        private const val FRAGMENT_TAG = "FullScreenDialog"

        fun show(fragmentManager: FragmentManager): FullScreenDialog {
            val dialog = FullScreenDialog()
            dialog.show(fragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }
}