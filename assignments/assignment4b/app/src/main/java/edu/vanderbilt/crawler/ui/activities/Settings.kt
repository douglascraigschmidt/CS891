package edu.vanderbilt.crawler.ui.activities

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.CrawlResult
import edu.vanderbilt.imagecrawler.transforms.Transform

/**
 * Data class used to centralize all crawler settings.
 */
data class Settings(val strategy: ImageCrawler.Type,
                     val filters: List<Transform.Type> = Transform.Type.values().toList(),
                     val rootUrl: String,
                     val crawlDepth: Int = 3,
                     val diagnostics: Boolean = true,
                     val consumer: (result: CrawlResult) -> Unit)
