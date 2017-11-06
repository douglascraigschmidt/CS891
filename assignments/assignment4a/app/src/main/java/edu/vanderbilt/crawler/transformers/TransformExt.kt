package edu.vanderbilt.crawler.transformers

import edu.vanderbilt.crawler.platform.AndroidPlatform
import edu.vanderbilt.crawler.ui.activities.Settings
import edu.vanderbilt.imagecrawler.platform.Controller

fun Settings.toController(): Controller {
    return Controller
            .newBuilder()
            .platform(AndroidPlatform)
            .transforms(filters)
            .consumer(consumer)
            .rootUrl(rootUrl)
            .maxDepth(crawlDepth)
            .diagnosticsEnabled(diagnostics)
            .build()
}
