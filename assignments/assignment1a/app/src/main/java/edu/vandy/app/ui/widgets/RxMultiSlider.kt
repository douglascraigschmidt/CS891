package edu.vandy.app.ui.widgets

import android.os.Looper
import androidx.annotation.CheckResult
import edu.vandy.app.utils.Range

import com.jakewharton.rxbinding2.InitialValueObservable
import io.apptik.widget.MultiSlider
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class RxMultiSlider {
    companion object {
        @CheckResult
        fun changes(view: MultiSlider): InitialValueObservable<Range<Int>> {
            return MultiSliderChangeObservable(view)
        }
    }

    private class MultiSliderChangeObservable(private val view: MultiSlider)
        : InitialValueObservable<Range<Int>>() {

        override fun subscribeListener(observer: Observer<in Range<Int>>) {
            if (!checkMainThread(observer)) {
                return
            }
            val listener = Listener(view, observer)
            view.setOnThumbValueChangeListener(listener)
            observer.onSubscribe(listener)
        }

        override fun getInitialValue(): Range<Int> {
            return Range(view.getThumb(0).value, view.getThumb(1).value)
        }

        internal class Listener(private val view: MultiSlider,
                                private val observer: Observer<in Range<Int>>)
            : MainThreadDisposable(), MultiSlider.OnThumbValueChangeListener {
            override fun onValueChanged(multiSlider: MultiSlider,
                                        thumb: MultiSlider.Thumb,
                                        thumbIndex: Int,
                                        value: Int) {
                if (!isDisposed) {
                    observer.onNext(Range(view.getThumb(0).value, view.getThumb(1).value))
                }
            }

            override fun onDispose() {
                view.setOnThumbValueChangeListener(null)
            }
        }

        private fun checkMainThread(observer: Observer<*>): Boolean {
            return if (Looper.myLooper() != Looper.getMainLooper()) {
                observer.onError(IllegalStateException(
                        "Expected to be called on the main thread but was "
                        + Thread.currentThread().name))
                false
            } else {
                true
            }
        }
    }
}
