package edu.vanderbilt.webcrawler.unused

//import android.content.Context
//import android.net.Uri
//import android.support.v4.content.ContextCompat
//import android.support.v7.widget.RecyclerView
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//
//import edu.vanderbilt.webcrawler.R
//import edu.vanderbilt.webcrawler.adapters.MultiSelectAdapter
//
//class UriAdapter(context: Context, listener: MultiSelectAdapter.OnSelectionListener?)
//    : MultiSelectAdapter<Uri, UriAdapter.UriViewHolder>(context, listener) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UriViewHolder {
//        val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.item_list, parent, false)
//
//        return UriViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: UriViewHolder, position: Int) {
//        // Never rely on passed position; always use the real adapter position.
//        val adapterPosition = holder.adapterPosition
//        val uri = getItem(adapterPosition)
//
//        // Setup up click listen callback.
//        initializeListeners(holder.mTextView, adapterPosition)
//
//        // Display the URL text.
//        holder.mTextView!!.text = uri.toString()
//
//        // Draw the selection state.
//        drawSelectionState(holder.itemView, adapterPosition)
//    }
//
//    private fun initializeListeners(view: View?, position: Int) {
//        // Redirect all selection handling to
//        // registered click listen.
//        view?.setOnClickListener { onSelectionListener?.onItemClick(it, position) }
//
//        // Redirect all selection handling to
//        // registered click listen.
//        view?.setOnLongClickListener { onSelectionListener?.onItemLongClick(it, position) ?: false }
//    }
//
//    private fun drawSelectionState(view: View, position: Int) {
//        // Set list item background color based on selection state.
//        if (isItemSelected(position)) {
//            view.setBackgroundColor(
//                    ContextCompat.getColor(
//                            view.context,
//                            R.color.grid_item_selected_color_filter))
//        } else {
//            view.setBackgroundColor(
//                    ContextCompat.getColor(
//                            view.context, android.R.color.transparent))
//        }
//    }
//
//    class UriViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        internal val mTextView: TextView? = null
//    }
//}
