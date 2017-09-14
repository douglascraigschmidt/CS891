package edu.vanderbilt.webcrawler

import android.app.Application
import com.facebook.stetho.Stetho
import edu.vanderbilt.webcrawler.extensions.DelegatesExt

/**
 * Created by monte on 2017-09-01.
 */

class App : Application() {
    companion object {
        var instance: App by DelegatesExt.notNullSingleValue()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //TODO: remove before releasing
        Stetho.initializeWithDefaults(applicationContext)
    }
}