package com.duzhaokun123.danmakuview.interfaces

import com.duzhaokun123.danmakuview.model.Danmakus

fun interface DanmakuParser {
    object EMPTY: DanmakuParser {
        override fun parse() = mutableMapOf<Int, Danmakus>()
    }

    /**
     * 工作协程调用
     * 对于 Kotlin 要用协程用`DanmakuView.parse(parser: (CoroutineScope.() -> MutableMap<Int, Danmakus>) , onEnd: ((danmakus: Map<Int, Danmakus>) -> Unit)? = null)`
     */
    fun parse(): MutableMap<Int, Danmakus>
}