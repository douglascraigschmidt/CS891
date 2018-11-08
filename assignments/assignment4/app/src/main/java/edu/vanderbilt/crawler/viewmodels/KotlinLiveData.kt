//package edu.vanderbilt.crawler.viewmodels
//
//
//import android.arch.lifecycle.LifecycleOwner
//import android.arch.lifecycle.MutableLiveData
//import android.arch.lifecycle.Observer
//import android.os.Looper
//import kotlin.reflect.KMutableProperty0
//import kotlin.reflect.KProperty
//import kotlin.reflect.jvm.isAccessible
//
///**
// * From: https://stackoverflow.com/questions/44844933/more-fun-with-kotlin-delegates
// */
//class KotlinLiveData<T>(val default: T, val liveData: MutableLiveData<T>? = null) {
//    val data = liveData ?: MutableLiveData()
//
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        return data.value ?: default
//    }
//
//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            data.value = value
//        } else {
//            data.postValue(value)
//        }
//    }
//}
//
//inline fun <reified R> KMutableProperty0<*>.getLiveData(): MutableLiveData<R> {
//    isAccessible = true
//    return (getDelegate() as KotlinLiveData<R>).data
//}
//
//inline fun <reified R> KMutableProperty0<*>.observe(owner: LifecycleOwner, obs: Observer<R>) {
//    isAccessible = true
//    (getDelegate() as KotlinLiveData<R>).data.observe(owner, obs)
//}
