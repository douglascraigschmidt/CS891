package edu.vanderbilt.crawler.adapters

import android.animation.AnimatorInflater
import android.animation.StateListAnimator
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
            //val changed = value != field
            field = value

            // TODO: select all won't refresh
            // properly with this gaurd
            //if (changed) {
            refresh()
            //}
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

    fun setDefaultModeStateListAnimator(resId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defaultModeStateListAnimator =
                    AnimatorInflater.loadStateListAnimator(itemView.context, resId)
        }
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return AnimatorInflater.loadStateListAnimator(view.ctx, R.animator.raise)
        } else {
            return null
        }
    }

    /* TODO(monte) use this? */
//    private fun getRaiseStateListAnimator(view: View): StateListAnimator? {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            with(StateListAnimator()) {
//                val animTime = view.context.resources.getInteger(
//                        android.R.integer.config_longAnimTime).toLong()
//                with(ObjectAnimator.ofFloat(view, "translationZ", 12.px.toFloat())) {
//                    duration = animTime
//                    interpolator = AccelerateDecelerateInterpolator()
//                    addState(intArrayOf(android.R.attr.state_activated), this)
//                    addUpdateListener {
//                        println("*************** RAISING Z: FRACTION = $animatedFraction Z = ${view.translationZ}")
//                    }
//                }
//
//                with(ObjectAnimator.ofFloat(view, "translationZ", 0F)) {
//                    duration = animTime
//                    interpolator = AccelerateDecelerateInterpolator()
//                    addUpdateListener {
//                        println("*************** LOWERING Z: FRACTION = $animatedFraction VALUE = ${animatedValue} Z = ${view.translationZ}")
//                    }
//                    addState(StateSet.WILD_CARD, this)
//                }
//
//                this
//            }
//        } else {
//            null
//        }
//    }

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
