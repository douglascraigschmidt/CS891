package edu.vanderbilt.crawler.viewmodels

import android.annotation.SuppressLint
import android.os.Parcelable
import edu.vanderbilt.imagecrawler.platform.Cache
import kotlinx.android.parcel.Parcelize
import java.io.File

@SuppressLint("ParcelCreator")
@Parcelize
data class Resource(
        val url: String,
        val tag: String,
        val state: State,
        val type: Type,
        val timestamp: Long,
        val size: Int = 0,
        val thread: Int = 0,
        val filePath: String? = null,
        val progress: Int = -1,
        val message: String? = null,
        val exception: Exception? = null) : Parcelable {

    enum class Type {
        SOURCE,
        TRANSFORM
    }

    enum class State {
        LOAD,
        DOWNLOAD,
        CREATE,
        READ,
        WRITE,
        PROCESS,
        CLOSE,
        CANCEL
    }
    companion object {
        fun fromFileObserver(item: Cache.Item,
                             operation: Cache.Operation,
                             progress: Int = -1,
                             thread: Int = 0): Resource
            = fromFileObserver(item.key,
                               operation,
                               item.file,
                               item.size,
                               item.tag,
                               item.timestamp,
                               progress,
                               thread)

        private fun fromFileObserver(url: String,
                                     operation: Cache.Operation,
                                     file: File,
                                     size: Int,
                                     tag: String,
                                     timestamp: Long,
                                     progress: Int,
                                     thread: Int): Resource {
            return Resource(
                    url = url,
                    tag = tag,
                    state = when (operation) {
                        Cache.Operation.CREATE -> State.CREATE
                        Cache.Operation.DOWNLOAD -> State.DOWNLOAD
                        Cache.Operation.READ -> State.READ
                        Cache.Operation.WRITE -> State.WRITE
                        Cache.Operation.TRANSFORM -> State.PROCESS
                        Cache.Operation.LOAD -> State.LOAD
                        Cache.Operation.CLOSE -> State.CLOSE
                        else -> throw Exception("Illegal operation: $operation")
                    },
                    type = if (tag == Cache.NOTAG) Type.SOURCE else Type.TRANSFORM,
                    size = Math.round(size.toFloat() / 1000) * 1000,
                    thread = thread,
                    timestamp = timestamp,
                    filePath = file.absolutePath,
                    progress = progress,
                    message = null,
                    exception = null)
        }
    }
}
