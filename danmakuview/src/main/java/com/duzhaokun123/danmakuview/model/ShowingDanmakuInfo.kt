package com.duzhaokun123.danmakuview.model

import android.graphics.RectF
import com.duzhaokun123.danmakuview.danmaku.Danmaku

data class ShowingDanmakuInfo(
    val danmaku: Danmaku,
    val rect: RectF,
    val line: Int,
    val progress: Float,
    val pool: Int
)