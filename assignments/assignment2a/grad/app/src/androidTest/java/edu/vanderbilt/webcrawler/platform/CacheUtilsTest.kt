package edu.vanderbilt.webcrawler.platform

/**
 * Created by monte on 2017-09-13.
 */
import edu.vanderbilt.platform.Device
import edu.vanderbilt.utils.CacheUtils
import edu.vanderbilt.utils.Options
import edu.vanderbilt.utils.WebPageCrawler
import org.junit.Test

/**
 * Created by monte on 2017-09-09.
 */
class CacheUtilsTest {
    @Test
    fun getDefaultResourceUrlList() {
        // Device protects itself from multiple builds and this
        // method gets around that restriction.
        Device.setPrivateInstanceFieldToNullForUnitTestingOnly()

        // Create a new device with a local page crawler.
        Device.newBuilder()
                .platform(AndroidPlatform())
                .options(Options.newBuilder()
                        .local(false)
                        .diagnosticsEnabled(true)
                        .build())
                .crawler(WebPageCrawler())
                .build()

        CacheUtils.clearCache()
    }
}