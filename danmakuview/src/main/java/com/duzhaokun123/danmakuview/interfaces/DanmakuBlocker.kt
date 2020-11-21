package com.duzhaokun123.danmakuview.interfaces

import com.duzhaokun123.danmakuview.danmaku.Danmaku

interface DanmakuBlocker {
    /**
     * @return true: 屏蔽
     */
    fun shouldBlock(danmaku: Danmaku): Boolean
}