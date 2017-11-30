package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.classNameToTitle
import edu.vanderbilt.crawler.ui.screens.settings.Settings
import edu.vanderbilt.imagecrawler.transforms.Transform

open class TransformsAdapter : EnumCheckedListAdapter<Transform.Type> {

    constructor(context: Context, checkedList: List<Boolean>)
            : super(context, Transform.Type::class.java, checkedList)

    override fun getName(item: Transform.Type?): String = super.getName(item).classNameToTitle()

    override fun bindView(item: Transform.Type?, position: Int, view: View) {
        super.bindView(item, position, view)

        // Use Rx to track check box changes.
        RxCompoundButton.checkedChanges(view.findViewById(R.id.checkBox))
                .filter { it != Settings.transformTypes.contains(item) }
                .subscribe {
                    Settings.transformTypes = if (it) {
                        Settings.transformTypes + Transform.Type.values()[position]
                    } else {
                        Settings.transformTypes - Transform.Type.values()[position]
                    }
                }
    }

    companion object {
        fun buildAdapter(viewGroup: ViewGroup): TransformsAdapter {
            // Use generics to determine what transforms are supported
            // by the current crawler.
            val supportedTransforms = try {
                val crawler = Settings.crawlStrategy.clazz.newInstance()
                val method = crawler.javaClass.getMethod("getSupportedTransforms")
                @Suppress("UNCHECKED_CAST")
                method.invoke(crawler) as List<Transform.Type>
            } catch (e: Exception) {
                mutableListOf<Transform.Type>()
            }

            // Currently selected transforms.
            val currentTransforms: List<Transform.Type> = Settings.transformTypes

            // Build Boolean list of selected transforms.
            val selectedTransforms = supportedTransforms.map { currentTransforms.contains(it) }

            val adapter = TransformsAdapter(viewGroup.context, selectedTransforms)

            (0..(supportedTransforms.size - 1)).forEach {
                val view = adapter.newView(LayoutInflater.from(viewGroup.context), it, viewGroup)
                adapter.bindView(adapter.getItem(it), it, view)
                viewGroup.addView(view)
            }

            return adapter
        }
    }
}