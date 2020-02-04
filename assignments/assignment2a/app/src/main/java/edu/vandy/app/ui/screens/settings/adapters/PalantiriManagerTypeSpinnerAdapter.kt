package edu.vandy.app.ui.screens.settings.adapters

import android.content.Context
import edu.vanderbilt.crawler.ui.screens.settings.adapters.ArraySpinnerAdapter
import edu.vandy.simulator.managers.palantiri.PalantiriManager

class PalantiriManagerTypeSpinnerAdapter @JvmOverloads constructor(
        context: Context,
        enumType: Class<PalantiriManager.Factory.Type>,
        showNull: Boolean = false)
    : ArraySpinnerAdapter<PalantiriManager.Factory.Type>(
        context,
        enumType.enumConstants?.filter { it.isSupported }?.toTypedArray() ?: emptyArray(),
        showNull)
