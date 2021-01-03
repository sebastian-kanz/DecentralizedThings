package eth.sebastiankanz.decentralizedthings.helper

import android.Manifest
import androidx.annotation.CallSuper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import eth.sebastiankanz.decentralizedthings.data.IPFSDatabase
import eth.sebastiankanz.decentralizedthings.di.modules.androidModule
import eth.sebastiankanz.decentralizedthings.di.modules.applicationModule
import eth.sebastiankanz.decentralizedthings.di.modules.databaseModule
import eth.sebastiankanz.decentralizedthings.di.modules.ipfsModule
import eth.sebastiankanz.decentralizedthings.di.modules.repositoryModule
import eth.sebastiankanz.decentralizedthings.di.modules.useCaseModule
import eth.sebastiankanz.decentralizedthings.di.modules.viewModelModule
import eth.sebastiankanz.decentralizedthings.persistence.AppSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.logging.Logger

@ExperimentalCoroutinesApi
open class BaseAndroidTest : KoinTest {
    @get:Rule
    val archComponentsExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val instantDispatcherRule = InstantDispatchersRule()

    @get:Rule
    val testName = TestName()

    protected fun getScreenshotName(): String {
        return javaClass.simpleName + "_" + testName.methodName
    }

    @Suppress("USELESS_CAST")// needed for KOIN
    @Before
    @Throws(Exception::class)
    @CallSuper
    open fun setUp() {
        // load Modules between test to have new singeltons
        resetKoin()
        cleanDatabases()
        val appSettings: AppSettings = get()
        appSettings.cleanUp()
    }

    private fun resetKoin() {
        stopKoin()
        startKoin {
            androidContext(getTestContext())
            modules(
                listOf(
                    androidModule,
                    applicationModule,
                    databaseModule,
                    ipfsModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule
                )
            )
        }
    }

    private fun cleanDatabases() {
        val db: IPFSDatabase = get()
        db.clearAllTables()
    }

    @After
    @Throws(Exception::class)
    @CallSuper
    open fun tearDown() {
        stopKoin()
    }

    companion object {
        private val LOGGER = Logger.getLogger(BaseAndroidTest::class.java.name)

        /**
         * Grants the required permissions.
         */
        @JvmStatic
        @BeforeClass
        @Throws(Exception::class)
        fun globalSetUp() {

            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            val packageNames = arrayOf(getTestContext().packageName)

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            for (packageName in packageNames) {
                for (permission in permissions) {
                    // grant all required permissions
                    val command = "pm grant $packageName $permission"
                    LOGGER.info("Executing $command")
                    val result = device.executeShellCommand(command)
                    LOGGER.info(result)
                }
            }
        }
    }
}

private fun getTestContext() = InstrumentationRegistry.getInstrumentation().targetContext