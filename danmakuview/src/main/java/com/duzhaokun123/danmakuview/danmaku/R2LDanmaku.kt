package com.duzhaokun123.danmakuview.danmaku

import android.graphics.*
import com.duzhaokun123.danmakuview.checkScrollLineDanmakuHit
import com.duzhaokun123.danmakuview.model.DanmakuConfig

class R2LDanmaku : LineDanmaku() {
    override fun onDraw(
        canvas: Canvas, drawWidth: Int, drawHeight: Int,
        progress: Float, danmakuConfig: DanmakuConfig, line: Int
    ): RectF? {
        if (cache == null) onBuildCache(danmakuConfig)
        val bitmap = cache ?: return null
        val x = (drawWidth + bitmap.width) * (1 - progress) - bitmap.width
        val y = (danmakuConfig.lineHeight * (line - 1)).toFloat() + danmakuConfig.marginTop
        val width = bitmap.width
        val height = bitmap.height
        canvas.drawBitmap(bitmap, x, y, null)
        return RectF(x, y, x + width, y + height)
    }

    override fun willHit(
        other: Danmaku,
        drawWidth: Int, drawHeight: Int,
        danmakuConfig: DanmakuConfig
    ) = checkScrollLineDanmakuHit(other, drawWidth, danmakuConfig)
}