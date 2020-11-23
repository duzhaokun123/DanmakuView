package com.duzhaokun123.danmakuview

import android.graphics.*
import com.duzhaokun123.danmakuview.danmaku.Danmaku
import com.duzhaokun123.danmakuview.model.DanmakuConfig
import com.duzhaokun123.danmakuview.danmaku.LineDanmaku
import kotlin.math.abs
import kotlin.math.sqrt

private val cleanPaint by lazy {
    Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
}

fun Canvas.clean() {
    this.drawPaint(cleanPaint)
}

fun LineDanmaku.checkStaticLineDanmakuHit(
    other: Danmaku, danmakuConfig: DanmakuConfig
): Boolean {
    val thisDanmakuStart = offset
    val thisDanmakuEnd = offset + (duration * danmakuConfig.durationCoeff).toLong()
    val otherDanmakuStart = other.offset
    val otherDanmakuEnd = other.offset + (other.duration * danmakuConfig.durationCoeff).toLong()
    return thisDanmakuStart in otherDanmakuStart..otherDanmakuEnd || otherDanmakuStart in thisDanmakuStart..thisDanmakuEnd
}

fun LineDanmaku.checkScrollLineDanmakuHit(
    other: Danmaku,
    drawWidth: Int, danmakuConfig: DanmakuConfig
): Boolean {
    if (this.offset == other.offset) return true

    if (this.cache == null) this.onBuildCache(danmakuConfig)
    val otherCache = other.cache ?: return false
    val otherSpeed =
        (drawWidth + otherCache.width).toDouble() / (other.duration * danmakuConfig.durationCoeff)
    val otherFullShowTime = other.offset + (otherCache.width / otherSpeed).toLong()
    if (this.offset in other.offset..otherFullShowTime) return true

    if (other.cache == null) other.onBuildCache(danmakuConfig)
    val thisCache = cache ?: return false
    val thisSpeed =
        (drawWidth + thisCache.width).toDouble() / (this.duration * danmakuConfig.durationCoeff)
    val thisFullShowTime = this.offset + (thisCache.width / thisSpeed).toLong()
    if (other.offset in this.offset..thisFullShowTime) return true

    if (thisSpeed == otherSpeed) return false

    val x1 = otherSpeed * (this.offset - other.offset) - otherCache.width
    if (x1 > 0) {
        val t1 = x1 / (thisSpeed - otherSpeed)
        if (t1 in 0.0..(drawWidth / thisSpeed)) return true
    }

    val x2 = thisSpeed * (other.offset - this.offset) - thisCache.width
    if (x2 > 0) {
        val t2 = x2 / (otherSpeed - thisSpeed)
        if (t2 in 0.0..(drawWidth / otherSpeed)) return true
    }

    return false //不会碰撞
}

fun PointF.getDistance(p: PointF): Float {
    val x = abs(this.x - p.x)
    val y = abs(this.y - p.y)
    return sqrt(x * x + y * y)
}