package edu.vanderbilt.crawler.ui.screens.settings.adapters

import android.content.Context

open class EnumSpinnerAdapter<T : Enum<T>> @JvmOverloads constructor(
        context: Context,
        enumType: Class<T>,
        showNull: Boolean = false)
    : ArraySpinnerAdapter<T>(context, enumType.enumConstants!!, showNull)
