package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType

class CrawlerTypeSpinnerAdapter @JvmOverloads constructor(
        context: Context,
        enumType: Class<CrawlerType>,
        showNull: Boolean = false)
    : ArraySpinnerAdapter<CrawlerType>(
        context,
        enumType.enumConstants?.filter { it.isSupported }?.toTypedArray() ?: emptyArray(),
        showNull)
