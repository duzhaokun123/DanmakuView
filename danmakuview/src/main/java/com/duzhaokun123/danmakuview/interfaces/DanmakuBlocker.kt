package com.duzhaokun123.danmakuview.interfaces

import com.duzhaokun123.danmakuview.danmaku.Danmaku

fun interface DanmakuBlocker {
    /**
     * @return true: 屏蔽
     */
    fun shouldBlock(danmaku: Danmaku, pool: Int): Boolean
}