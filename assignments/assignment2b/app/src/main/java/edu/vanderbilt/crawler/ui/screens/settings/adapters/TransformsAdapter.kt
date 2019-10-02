package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
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
        val tv = view.findViewById<TextView>(R.id.textView)
        // Use tag to find the checkbox because each checkbox was
        // created with View.NO_ID to prevent CompoundButton save/restore
        // handling that interferes with restoring from shared prefs.
        val cb = view.findViewWithTag<CheckBox>("checkBox")
        tv.text = getName(item)
        cb.isChecked = isChecked(position)

        // Use Rx to track check box changes.
        RxCompoundButton.checkedChanges(cb)
                .filter { it != Settings.transformTypes.contains(item) }
                .subscribe {
                    Settings.transformTypes = if (it) {
                        Settings.transformTypes + Transform.Type.values()[position]
                    } else {
                        Settings.transformTypes - Transform.Type.values()[position]
                    }
                }
    }

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View {
        val view = super.newView(inflater, position, container)
        view.id = View.generateViewId()
        return view
    }

    companion object {
        fun buildAdapter(viewGroup: ViewGroup): TransformsAdapter {
            // Use generics to determine what transforms are supported
            // by the current crawler.
            val supportedTransforms: List<Transform.Type> = try {
                val crawler = Settings.crawlStrategy.newInstance()
                val method = crawler.javaClass.getMethod("getSupportedTransforms")
                @Suppress("UNCHECKED_CAST")
                method.invoke(crawler) as List<Transform.Type>
            } catch (e: Exception) {
                emptyList()
            }

            // Currently selected transforms with any unsupported entries removed.
            val currentTransforms: List<Transform.Type?> = (Settings.transformTypes).filterNotNull()

            // Build Boolean list of selected transforms.
            val selectedTransforms = supportedTransforms.map { currentTransforms.contains(it) }

            val adapter = TransformsAdapter(viewGroup.context, selectedTransforms)

            (supportedTransforms.indices).forEach {
                val view = adapter.newView(LayoutInflater.from(viewGroup.context), it, viewGroup)
                // Set id to NO_ID so that framework does not save/restore checkbox state. The
                // checkbox's are always restored from shared preferences. Also, set tag so that
                // bindView call above can easily find the check box.
                val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
                checkBox.id = View.NO_ID
                checkBox.tag = "checkBox"

                adapter.bindView(adapter.getItem(it), it, view)
                viewGroup.addView(view)
            }

            return adapter
        }
    }
}