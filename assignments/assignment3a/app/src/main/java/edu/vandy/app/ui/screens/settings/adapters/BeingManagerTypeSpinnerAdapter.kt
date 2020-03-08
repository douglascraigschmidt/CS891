package edu.vandy.app.ui.screens.settings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import edu.vanderbilt.crawler.ui.screens.settings.adapters.ArraySpinnerAdapter
import edu.vandy.simulator.managers.beings.BeingManager

class BeingManagerTypeSpinnerAdapter @JvmOverloads constructor(
        context: Context,
        enumType: Class<BeingManager.Factory.Type>,
        showNull: Boolean = false)
    : ArraySpinnerAdapter<BeingManager.Factory.Type>(
        context,
        enumType.enumConstants?.filter { it.isSupported }?.toTypedArray() ?: emptyArray(),
        showNull)

