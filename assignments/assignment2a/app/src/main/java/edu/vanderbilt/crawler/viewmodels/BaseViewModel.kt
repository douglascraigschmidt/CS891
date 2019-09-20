package edu.vanderbilt.crawler.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class BaseViewModel(app: Application) : AndroidViewModel(app), CoroutineScope {
    private val compositeJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + compositeJob

    protected val context: Context
        get() = getApplication()

    override fun onCleared() {
        compositeJob.cancel()
        super.onCleared()
    }
}
