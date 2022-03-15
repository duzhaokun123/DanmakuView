package com.duzhaokun123.danmakuview.danmaku

import android.graphics.*
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.isDark
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
                DanmakuConfig.DrawMode.SHADOW,
                DanmakuConfig.DrawMode.SHADOW_STROKE -> {
                    setShadowLayer(
                        danmakuConfig.shadowRadius,
                        danmakuConfig.shadowDx,
                        danmakuConfig.shadowDy,
                        textShadowColor ?: danmakuConfig.shadowColor
                    )
                }
                else -> Unit
            }
        }
        val stokePaint = when(danmakuConfig.drawMode) {
            DanmakuConfig.DrawMode.STROKE,
            DanmakuConfig.DrawMode.SHADOW_STROKE ->
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textStrokeColor ?: if (textColor.isDark) Color.WHITE else Color.BLACK
                    alpha = Value.ALPHA_MAX
                    textSize = this@LineDanmaku.textSize * danmakuConfig.textSizeCoeff
                    typeface = danmakuConfig.typeface
                    isUnderlineText = underline
                    style = Paint.Style.STROKE
                    strokeWidth = danmakuConfig.stokeWidth
                }
            else -> null
        }
        val bounders = Rect()
        paint.getTextBounds(text, 0, text.length, bounders)
        val width = (bounders.width() + textSize / 3).toInt()
        val height = (bounders.height() + textSize / 3).toInt()
        if (width == 0 || height == 0) return
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, 0F, bounders.height().toFloat(), paint)
        if (stokePaint != null) {
            canvas.drawText(text, 0F, bounders.height().toFloat(), stokePaint)
        }
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