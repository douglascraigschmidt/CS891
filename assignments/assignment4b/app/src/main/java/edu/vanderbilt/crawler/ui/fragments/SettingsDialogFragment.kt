package edu.vanderbilt.crawler.ui.fragments

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import edu.vanderbilt.crawler.R

/**
 * Application developer options fragment that shows a list of tunable
 * options in a modal bottom sheet.
 *
 * To show this bottom sheet:
 * <pre>
 * SettingsDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * You activity (or fragment) needs to implement [SettingsDialogFragment.Listener].
 */
class SettingsDialogFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(itemCount: Int): SettingsDialogFragment {
            return SettingsDialogFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_settings_dialog, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val recyclerView = view as RecyclerView?
        recyclerView!!.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = ItemAdapter(0)
    }

    private inner class ViewHolder internal constructor(inflater: LayoutInflater, parent: ViewGroup)
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_settings_item, parent, false)) {

        val text: TextView = itemView.findViewById(R.id.text)
    }

    private inner class ItemAdapter internal constructor(private val mItemCount: Int)
        : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context), parent)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = position.toString()
        }

        override fun getItemCount(): Int = mItemCount
    }
}
