package edu.vanderbilt.crawler.adapters

import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.StateSet
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.ctx

/**
 * Created by monte on 2017-09-07.
 */

@SuppressLint("ObsoleteSdkInt") // Used by projects with lower min SDKs
open class SelectableViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    /** Sets item view's activated stated */
    var activated: Boolean
        get() = itemView.isActivated
        set(value) {
            itemView.isActivated = value
            selectable = value
        }

    var selectable: Boolean = true
        set(value) {
            field = value
            refresh()
        }

    private var defaultModeBackgroundDrawable: Drawable? = itemView.background
        set(value) {
            field = value
            if (!selectable) {
                itemView.background = value
            }
        }

    private var selectionModeBackgroundDrawable: Drawable? =
            getAccentStateDrawable(itemView.context)
        set(value) {
            field = value
            if (selectable) {
                itemView.background = value
            }
        }

    private var selectionModeStateListAnimator: StateListAnimator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getRaiseStateListAnimator(itemView)
            } else {
                null
            }

    private var defaultModeStateListAnimator: StateListAnimator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.stateListAnimator
            } else {
                null
            }

    @Suppress("unused")
    fun setDefaultModeStateListAnimator(resId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defaultModeStateListAnimator =
                    AnimatorInflater.loadStateListAnimator(itemView.context, resId)
        }
    }

    @Suppress("unused")
    fun setSelectionModeStateListAnimator(resId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            selectionModeStateListAnimator =
                    AnimatorInflater.loadStateListAnimator(itemView.context, resId)
        }
    }

    private fun getAccentStateDrawable(context: Context): Drawable {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true)

        val colorDrawable = ColorDrawable(typedValue.data)

        return with(StateListDrawable()) {
            addState(intArrayOf(android.R.attr.state_activated), colorDrawable)
            addState(StateSet.WILD_CARD, null)
            this
        }
    }

    private fun getRaiseStateListAnimator(view: View): StateListAnimator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatorInflater.loadStateListAnimator(view.ctx, R.animator.raise)
        } else {
            null
        }
    }

    private fun refresh() {
        val backgroundDrawable = if (activated) {
            selectionModeBackgroundDrawable
        } else {
            defaultModeBackgroundDrawable
        }

        itemView.background = backgroundDrawable

        backgroundDrawable?.jumpToCurrentState()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val animator = if (activated) {
                selectionModeStateListAnimator
            } else {
                defaultModeStateListAnimator
            }

            itemView.stateListAnimator = animator
            animator?.jumpToCurrentState()
        }
    }
}
