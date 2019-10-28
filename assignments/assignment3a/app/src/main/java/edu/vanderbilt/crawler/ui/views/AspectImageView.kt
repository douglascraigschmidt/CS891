package edu.vanderbilt.crawler.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.*
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.debug
import edu.vanderbilt.crawler.utils.warn

/**
 * Custom ImageView that overrides [ImageView.onMeasure] to
 * set the view width and height to reflect and aspect ratio.
 * Both portrait and landscape are handled.
 */
class AspectImageView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0)
    : AppCompatImageView(context, attrs, defStyleAttr), KtLogger {
    companion object {
        val VERBOSE_LOGGING = false
        // Current ration for this application is 1:1
        // because image dimensions vary widly.
        val ASPECT_RATIO = 1f
    }


    /**
     * Calculates view size based on one dimension (vertical or horizontal)
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isViewRealized()) {
            // This class only handles wrap content in one dimension.
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val lp = layoutParams
            val widthSpecMode = getMode(widthMeasureSpec)
            val widthSpecSize = getSize(widthMeasureSpec)
            val heightSpecMode = getMode(heightMeasureSpec)
            val heightSpecSize = getSize(heightMeasureSpec)

            if (VERBOSE_LOGGING) {
                when (widthSpecMode) {
                    UNSPECIFIED -> log("Width MeasureSpec mode = UNSPECIFIED")
                    AT_MOST -> log("Width MeasureSpec mode = AT_MOST $widthSpecSize")
                    EXACTLY -> log("Width MeasureSpec mode = EXACTLY $widthSpecSize")
                    else -> log("Width MeasureSpec mode = NO SPEC MODE ($widthMeasureSpec)")
                }

                when (heightSpecMode) {
                    UNSPECIFIED -> log("Height MeasureSpec mode = UNSPECIFIED")
                    AT_MOST -> log("Height MeasureSpec mode = AT_MOST $heightSpecSize")
                    EXACTLY -> log("Height MeasureSpec mode = EXACTLY $heightSpecSize")
                    else -> log("Height MeasureSpec mode = NO SPEC MODE ($heightMeasureSpec)")
                }
            }

            if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT &&
                    lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                when (widthSpecMode) {
                    UNSPECIFIED -> {
                        warn("Height MeasureSpec mode = UNSPECIFIED UNEXPECTED!!!!")
                    }
                    AT_MOST -> {
                        val w = widthSpecSize - (paddingLeft + paddingRight)

                        //val aspect = height.toFloat() / width
                        val aspect = ASPECT_RATIO
                        if (w != 0) {
                            val h = w * aspect + paddingTop + paddingBottom
                            setMeasuredDimension(width, h.toInt())
                            log("Width MeasureSpec mode = AT_MOST $widthSpecSize -> height set to $h")
                            // Do not call base class handler.
                            return
                        }
                    }
                    EXACTLY -> {
                        val w = widthSpecSize - (paddingLeft + paddingRight)

                        //val aspect = height.toFloat() / width
                        val aspect = ASPECT_RATIO
                        if (w != 0) {
                            val h = (w * aspect) + paddingTop + paddingBottom
                            setMeasuredDimension(width, h.toInt())
                            log("Width MeasureSpec mode = EXACTLY $widthSpecSize -> height set to $h")
                            // Do not call base class handler.
                            return
                        }
                    }
                    else -> warn("Width MeasureSpec mode = NO SPEC MODE ($widthMeasureSpec)")
                }
            } else if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT &&
                    lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                when (widthSpecMode) {
                    UNSPECIFIED -> {
                        warn("Height MeasureSpec mode = UNSPECIFIED UNEXPECTED!!!!")
                    }
                    AT_MOST -> {
                        val h = heightSpecSize - (paddingLeft + paddingRight)

                        //val aspect = height.toFloat() / width
                        val aspect = ASPECT_RATIO
                        if (h != 0) {
                            val w = (h * aspect) + paddingTop + paddingBottom
                            setMeasuredDimension(w.toInt(), height)
                            log("Height MeasureSpec mode = AT_MOST $heightSpecSize -> width set to $w")
                            // Do not call base class handler.
                            return
                        }
                    }
                    EXACTLY -> {
                        val w = widthSpecSize - (paddingLeft + paddingRight)

                        //val aspect = height.toFloat() / width
                        val aspect = ASPECT_RATIO
                        if (w != 0) {
                            val h = (w * aspect) + paddingTop + paddingBottom
                            setMeasuredDimension(width, h.toInt())
                            log("Width MeasureSpec mode = EXACTLY $heightSpecSize -> width set to $w")
                            // Do not call base class handler.
                            return
                        }
                    }
                    else -> warn("Height MeasureSpec mode = NO SPEC MODE ($heightMeasureSpec)")
                }
            } else {
                error("AspectImage view must use MATCH_PARENT/WRAP_CONTENT or vice-versa!")
            }

            //wtf("This line should never be reached.")
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun isViewRealized(): Boolean {
        return width != 0
    }

    private fun log(msg: String) {
        if (VERBOSE_LOGGING) {
            debug(msg)
        }
    }
}