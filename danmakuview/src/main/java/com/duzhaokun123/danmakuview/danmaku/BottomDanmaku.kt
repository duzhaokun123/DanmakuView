package com.duzhaokun123.danmakuview.danmaku

import android.graphics.Canvas
import android.graphics.RectF
import com.duzhaokun123.danmakuview.checkStaticLineDanmakuHit
import com.duzhaokun123.danmakuview.model.DanmakuConfig

class BottomDanmaku : LineDanmaku() {
    override fun onDraw(
        canvas: Canvas, drawWidth: Int, drawHeight: Int,
        progress: Float, danmakuConfig: DanmakuConfig, line: Int
    ): RectF? {
        if (cache == null) onBuildCache(danmakuConfig)
        val bitmap = cache ?: return null
        val x = (drawWidth - bitmap.width) / 2F
        val y =
            (drawHeight - danmakuConfig.lineHeight * line - danmakuConfig.marginBottom).toFloat()
        val width = bitmap.width
        val height = bitmap.height
        canvas.drawBitmap(bitmap, x, y, null)
        return RectF(x, y, x + width, y + height)
    }

    override fun willHit(
        other: Danmaku,
        drawWidth: Int, drawHeight: Int,
        danmakuConfig: DanmakuConfig
    ) = checkStaticLineDanmakuHit(other, danmakuConfig)
}