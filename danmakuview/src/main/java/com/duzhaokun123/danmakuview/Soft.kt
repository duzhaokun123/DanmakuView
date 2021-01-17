package com.duzhaokun123.danmakuview

import java.lang.ref.SoftReference
import kotlin.reflect.KProperty

class Soft<T : Any?>(initializer: () -> T) {
    private var s = SoftReference(initializer())

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return s.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        s = SoftReference(value)
    }
}

fun <T : Any?> soft(initializer: () -> T) = Soft(initializer)
