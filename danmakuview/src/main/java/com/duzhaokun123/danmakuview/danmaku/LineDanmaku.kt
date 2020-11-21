package com.duzhaokun123.danmakuview.danmaku

import android.graphics.*
import com.duzhaokun123.danmakuview.model.DanmakuConfig

abstract class LineDanmaku : Danmaku() {
    override val cacheable = true

    override fun onBuildCache(danmakuConfig: DanmakuConfig) {
        var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@LineDanmaku.textColor
            alpha = this@LineDanmaku.alpha
            textSize = this@LineDanmaku.textSize * danmakuConfig.textSizeCoeff
            typeface = danmakuConfig.typeface
            isUnderlineText = underline
            when (danmakuConfig.drawMode) {
                DanmakuConfig.DrawMode.DEFAULT -> Unit
                DanmakuConfig.DrawMode.SHADOW -> {
                    setShadowLayer(
                        danmakuConfig.shadowRadius,
                        danmakuConfig.shadowDx,
                        danmakuConfig.shadowDy,
                        danmakuConfig.shadowColor
                    )
                }
            }
        }
        val bounders = Rect()
        paint.getTextBounds(text, 0, text.length, bounders)
        val width = (bounders.width() + textSize / 3).toInt()
        val height = (bounders.height() + textSize / 3).toInt()
        if (width == 0 || height == 0) return
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, 0F, bounders.height().toFloat(), paint)
        if (borderColor != 0) {
            paint = Paint()
            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5F
            canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        }
        cache = bitmap
    }

    /**
     * 仅判断同行同类弹幕是否碰撞
     */
    abstract fun willHit(
        other: Danmaku,
        drawWidth: Int, drawHeight: Int, danmakuConfig: DanmakuConfig
    ): Boolean
}