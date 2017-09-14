package edu.vanderbilt.webcrawler.platform

import edu.vanderbilt.webcrawler.App
import org.junit.Test

import org.junit.Assert.*
import java.io.File

/**
 * Created by monte on 2017-09-09.
 */
class AndroidPlatformTest {
    @Test
    fun getDefaultResourceUrlList() {
        listAssetFiles("")
    }

    private fun listAssetFiles(path: String) {
        val list: Array<String>
        list = App.instance.assets.list(path)
        if (list.isEmpty()) {
            try {
                val stream = App.instance.assets.open(path)
                stream.close()
                System.out.println("[FILE]: $path")
            } catch (e: Exception) {
                System.out.println("$path does not exist")
            }
        } else {
            if (!path.isBlank()) {
                System.out.println("[FOLDER]: $path")
            }
            list.forEach {
                if (path.isBlank()) {
                    listAssetFiles(it)
                } else {
                    listAssetFiles(path + File.separator + it)
                }
            }
        }
    }
}