package eth.sebastiankanz.decentralizedthings.filestorage.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eth.sebastiankanz.decentralizedthings.filestorage.R

class FileStorageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_storage)
        supportFragmentManager.beginTransaction().replace(R.id.fragment, FileStorageFragment()).commit()
    }
}