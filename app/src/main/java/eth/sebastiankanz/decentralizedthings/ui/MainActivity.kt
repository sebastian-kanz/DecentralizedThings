package eth.sebastiankanz.decentralizedthings.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import eth.sebastiankanz.decentralizedthings.R
import eth.sebastiankanz.decentralizedthings.databinding.ActivityMainBinding
import eth.sebastiankanz.decentralizedthings.features.FeatureId
import eth.sebastiankanz.decentralizedthings.features.Features
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Logger

// AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
// AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentActiveFeature: FeatureId? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
//            setLogo(R.drawable.ic_more)
//            setDisplayUseLogoEnabled(true)
        }

        Logger.getLogger("test123").info("test123 " + applicationContext.getExternalFilesDir(null).toString())
        init()
        showDefaultFeature()
    }

    fun onFragmentLoaded() {
        binding.loadingContainer.visibility = View.GONE
        binding.fragment.visibility = View.VISIBLE
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().addToBackStack(fragment.tag).add(R.id.fragment, fragment).commit()
    }

    private fun init() {
        askForPermissions()
        initBottomMenu()
    }

    private fun askForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
        }
    }

    private fun initBottomMenu() {
        if (Features.isEnabled(FeatureId.FILE_STORAGE)) {
            Features.setFeatureFragmentHost(FeatureId.FILE_STORAGE, this, R.id.fragment)
            val title = Features.getLabel(FeatureId.FILE_STORAGE)
            val menuItem = bottom_navigation.menu.add(0, 0, 0, title).setIcon(R.drawable.ic_cloud_circle).setOnMenuItemClickListener {
                if (currentActiveFeature != FeatureId.FILE_STORAGE) {
                    currentActiveFeature = FeatureId.FILE_STORAGE
                    binding.loadingContainer.visibility = View.VISIBLE
                    Features.launchFeatureRootFragment(FeatureId.FILE_STORAGE)
                }
                true
            }
            val badge = bottom_navigation.getOrCreateBadge(menuItem.itemId)
            badge.isVisible = true
            badge.clearNumber()
        }

        bottom_navigation.menu.add(0, 1, 1, "Settings").setIcon(R.drawable.ic_more).setOnMenuItemClickListener {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            true
        }
    }

    private fun showDefaultFeature() {
        Features.launchFeatureRootFragment(FeatureId.FILE_STORAGE)
        currentActiveFeature = FeatureId.FILE_STORAGE
    }

//    @UiThread
//    suspend fun getPin(context: Context) = suspendCoroutine<String?> { cont ->
//        val inflater = this.layoutInflater
//        val dialogView: View = inflater.inflate(R.layout.dialog_pin, null)
//        val dialog = MaterialAlertDialogBuilder(context)
//            .setTitle("title")
//            .setView(dialogView)
//            .setPositiveButton(android.R.string.ok) { _, _ ->
//                cont.resume(dialogView.dialog_pin_edittext.text.toString())
//            }
//            .setNeutralButton(android.R.string.cancel) { dialog, _ ->
//                dialog.cancel()
//            }
//            .setOnCancelListener {
//                cont.resume(null)
//            }
//            .create()
//        dialog.show()
//    }
}