package eth.sebastiankanz.decentralizedthings.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eth.sebastiankanz.decentralizedthings.R
import eth.sebastiankanz.decentralizedthings.databinding.ActivityMainBinding
import eth.sebastiankanz.decentralizedthings.features.FeatureId
import eth.sebastiankanz.decentralizedthings.features.Features
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_pin.view.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
// AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    // Respond to navigation item 1 click

                    Features.setFeatureFragmentHost(FeatureId.FILE_STORAGE, this, R.id.fragment)
                    Features.launchFeatureRootFragment(FeatureId.FILE_STORAGE)
                    true
                }
                R.id.page_2 -> {
                    // Respond to navigation item 2 click
                    true
                }
                else -> false
            }
        }

        val badge = bottom_navigation.getOrCreateBadge(R.id.page_1)
        badge.isVisible = true
        badge.clearNumber()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123)
        }
    }

    @UiThread
    suspend fun getPin(context: Context) = suspendCoroutine<String?> { cont ->
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_pin, null)
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("title")
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                cont.resume(dialogView.dialog_pin_edittext.text.toString())
            }
            .setNeutralButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setOnCancelListener {
                cont.resume(null)
            }
            .create()
        dialog.show()
    }
}