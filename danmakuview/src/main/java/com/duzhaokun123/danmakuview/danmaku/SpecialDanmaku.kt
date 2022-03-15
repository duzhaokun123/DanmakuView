package com.duzhaokun123.danmakuview.danmaku

import android.graphics.*
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.component1
import com.duzhaokun123.danmakuview.component2
import com.duzhaokun123.danmakuview.isDark
import com.duzhaokun123.danmakuview.model.DanmakuConfig

class SpecialDanmaku : Danmaku() {
    companion object {
        val defaultStartFrame = Triple(PointF(0F, 0F), 0F, Value.ALPHA_MAX)
        val defaultEndFrame = Triple(PointF(1F, 1F), 0F, Value.ALPHA_MAX)
    }

    var lines: Array<String>? = null

    var typeface: Typeface = Typeface.DEFAULT

    var drawMode = DanmakuConfig.DrawMode.DEFAULT

    /**
     * K: 进度
     * V: (相对位置, Z 轴旋转(度), Alpha)
     */
    var keyframes = mutableMapOf<Float, Triple<PointF, Float, Int>>()

    override val cacheable = true

    override fun onBuildCache(danmakuConfig: DanmakuConfig) {
        val text = (if (lines != null) lines else arrayOf(text))!!
        var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@SpecialDanmaku.textColor
            alpha = Value.ALPHA_MAX
            textSize = this@SpecialDanmaku.textSize
            typeface = this@SpecialDanmaku.typeface
            isUnderlineText = underline
            when (drawMode) {
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
        val stokePaint = when(drawMode){
            DanmakuConfig.DrawMode.STROKE,
                DanmakuConfig.DrawMode.SHADOW_STROKE ->
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textStrokeColor ?: if (textColor.isDark) Color.WHITE else Color.BLACK
                alpha = Value.ALPHA_MAX
                textSize = this@SpecialDanmaku.textSize
                typeface = this@SpecialDanmaku.typeface
                isUnderlineText = underline
                style = Paint.Style.STROKE
                strokeWidth = danmakuConfig.stokeWidth
            }
            else -> null
        }
        val bounderses = mutableListOf<Rect>()
        var width = 0
        var height = 0
        text.forEach { s ->
            val bounders = Rect()
            paint.getTextBounds(s, 0, s.length, bounders)
            bounderses.add(bounders)
            if (width < bounders.width()) width = bounders.width()
            height += bounders.height()
        }
        width += (textSize / 3).toInt()
        height += (textSize / 3).toInt()
        if (width == 0 || height == 0) return
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        var nextY = 0F
        text.forEachIndexed { i, s ->
            nextY += bounderses[i].height().toFloat()
            canvas.drawText(s, 0F, nextY, paint)
            if (stokePaint != null) {
                canvas.drawText(s, 0F, nextY, stokePaint)
            }
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

    private val bitmapPaint by lazy { Paint() }

    override fun onDraw(
        canvas: Canvas,
        drawWidth: Int, drawHeight: Int, progress: Float,
        danmakuConfig: DanmakuConfig, line: Int
    ): RectF? {
        if (cache == null) onBuildCache(danmakuConfig)
        val bitmap = cache ?: return null

        var lastKeyframeP = 0F
        var nextKeyframeP = 1F
        var lastFrame = defaultStartFrame
        var nextFrame = defaultEndFrame
        keyframes.forEach { (p, frame) ->
            if (p in lastKeyframeP..progress) {
                lastKeyframeP = p
                lastFrame = frame
            }
            if (p in progress..nextKeyframeP) {
                nextKeyframeP = p
                nextFrame = frame
            }
        }

        val (lastPoint, lastZR, lastAlpha) = lastFrame
        val (lastX, lastY) = lastPoint
        val (nextPoint, nextZR, nextAlpha) = nextFrame
        val (nextX, nextY) = nextPoint
        val mProgress = (progress - lastKeyframeP) / (nextKeyframeP - lastKeyframeP)

        val x = lastX + (nextX - lastX) * mProgress
        val y = lastY + (nextY - lastY) * mProgress
        val zr = lastZR + (nextZR - lastZR) * mProgress
        val alpha = (lastAlpha + (nextAlpha - lastAlpha) * mProgress).toInt()

        val drawX = x * drawWidth
        val drawY = y * drawHeight

        bitmapPaint.alpha = alpha
        return if (zr == 0F) {
            canvas.drawBitmap(bitmap, drawX, drawY, bitmapPaint)
            RectF(drawX, drawY, drawX + bitmap.width, drawY + bitmap.height)
        } else {
            val matrix = Matrix()
            matrix.postRotate(zr)
            matrix.postTranslate(drawX, drawY)
            canvas.drawBitmap(bitmap, matrix, bitmapPaint)
            val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            matrix.mapRect(rect)
            rect
        }
    }

    fun fillText() {
        if (text.isNotEmpty() && text.contains("\n")) {
            lines = text.split("\n").toTypedArray()
        }
    }
}