package edu.vanderbilt.crawler.preferences

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Observable
import android.content.Intent
import android.net.ConnectivityManager
import android.content.IntentFilter
import io.reactivex.disposables.Disposables
import android.content.BroadcastReceiver
import edu.vanderbilt.crawler.app.App
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import io.reactivex.ObservableOnSubscribe
import java.lang.ref.WeakReference

/*
class RxSharedPref<T: Any> constructor(context: Context, key: String) : ObservableOnSubscribe<T> {
    private var weakContext: WeakReference<Context> = WeakReference(context.applicationContext)
    private lateinit var onSharedPreferenceChangeListener:
            SharedPreferences.OnSharedPreferenceChangeListener

    companion object {
        inline fun <reified T: Any> create(context: Context, key: String): Observable<T> {
            val observer = Observable.create(RxSharedPref<T>(context, key))
            return Observable.defer {
                observer.subscribeOn(Schedulers.io())
            }
        }
    }

    override fun subscribe(emitter: ObservableEmitter<T>) {
        onSharedPreferenceChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, prefKey ->
                PreferenceProvider.prefs[]
                emitter.onNext()
            }
        }

        emitter.setDisposable(Disposables.fromRunnable {
            val context = weakContext.get()
            if (context != null) {
                context!!.unregisterReceiver(broadcastReceiver)
            }
        })

        val context = weakContext.get()
        if (context != null) {
            context!!.registerReceiver(broadcastReceiver, intentFilter)
        }
    }
}
    */

/*
class PrefObservable {
    companion object {
        fun RxPrefObservable(context: Context, key: String): Observable<Boolean> {
            val applicationContext = context.applicationContext

            return RxUtils.RxBroadcastReceiver
                    .create(context, intentFilter)
                    // Share a single broadcast receiver among all subscribers.
                    .share()
                    // Start with an empty intent to emit a result at subscription time.
                    .startWith(Intent())
                    .map({ intent -> isConnectedToNetwork(applicationContext) })
                    .distinctUntilChanged()
        }

        fun <T : Any> observe(context: Context, key: String, default: T): Observable<T> {
            return Observable.create { emitter ->
                {
                    val onSharedPreferenceChangeListener =
                            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                                sharedPrefs.

                            }
                    onSharedPreferenceChangeListener
                }
            }
        }
    }
}
    */

/*
// https://github.com/takuji31/Koreference/blob/master/koreference-observable/src/main/java/jp/takuji31/koreference/observable/KoreferenceModelObservableExtensions.kt

inline fun <reified T : KoreferenceModel, reified R> T.getValueAsSingle(property: KProperty1<T, R>): Single<R> {
    getKoreferencePropertyKey(this, property)
    return Single.fromCallable {
        property.get(this)
    }
}

inline fun <reified T : KoreferenceModel, reified R> T.observe(property: KProperty1<T, R>): Observable<R> {
    val key = getKoreferencePropertyKey(this, property)
    return Observable.create { emitter ->
        val initialiValue = property.get(this)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                emitter.onNext(property.get(this))
            }
        }
        emitter.onNext(initialiValue)
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        emitter.setCancellable {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}

fun <T : KoreferenceModel, R> getKoreferencePropertyKey(receiver: T, property: KProperty1<T, R>): String {
    return receiver.getSharedPreferencesKey(property.name) ?:
    throw IllegalArgumentException("${receiver::class.qualifiedName}.${property.name} is not Koreference delegate property")
}
*/