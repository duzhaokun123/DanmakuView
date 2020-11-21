package com.duzhaokun123.danmakuview.interfaces

import com.duzhaokun123.danmakuview.model.Danmakus

interface DanmakuParser {
    object EMPTY: DanmakuParser {
        override fun parse() = Danmakus()
    }

    /**
     * 工作协程调用
     */
    fun parse(): Danmakus
}