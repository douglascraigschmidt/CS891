package edu.vanderbilt.crawler.app

import android.app.Application
import android.util.Log
import com.squareup.leakcanary.LeakCanary
import edu.vanderbilt.crawler.BuildConfig
import edu.vanderbilt.crawler.extensions.DelegatesExt.notNullSingleValue
import edu.vanderbilt.crawler.utils.globalLogLevel

/**
 * The application made to be easily accessible as a singleton.
 */
class App : Application() {
    companion object {
        @JvmStatic
        var instance: App by notNullSingleValue()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (BuildConfig.DEBUG) {
            globalLogLevel = Log.VERBOSE
        }
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }
}
