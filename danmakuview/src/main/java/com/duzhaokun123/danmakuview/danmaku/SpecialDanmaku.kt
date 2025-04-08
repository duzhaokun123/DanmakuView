package com.duzhaokun123.danmakuview.danmaku

import android.graphics.*
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.component1
import com.duzhaokun123.danmakuview.component2
import com.duzhaokun123.danmakuview.isDark
import com.duzhaokun123.danmakuview.model.DanmakuConfig
import kotlin.math.PI
import kotlin.math.cos

/**
 * 这只是一个简单的高级弹幕(关键帧弹幕)
 * 没有透视效果
 * 如果你需要一些非线性插值变换颜色缩放什么的 自己写
 */
class SpecialDanmaku : Danmaku() {
    /**
     * [point] 左上角坐标 相对位置 [0.0, 1.0]
     * [alpha] 透明度 [0, 255]
     * [rotationX], [rotationY], [rotationZ] XYZ 旋转角度
     *  其中 rotationX, rotationY 是伪 3D 旋转 毕竟这不是 3D 引擎
     * TODO: 解释旋转方向
     * 屏幕坐标系
     *  从左向右 +x
     *  从上向下 +y
     *  从内向外 +z
     */
    data class Frame(
        val point: PointF,
        val alpha: Int,
        val rotationX: Float,
        val rotationY: Float,
        val rotationZ: Float,
    )

    companion object {
        val defaultStartFrame = Frame(PointF(0F, 0F), Value.ALPHA_MAX, 0F, 0F, 0F)
        val defaultEndFrame = Frame(PointF(1F, 1F), Value.ALPHA_MAX, 0F, 0F, 0F)
    }

    var lines: Array<String>? = null

    var typeface: Typeface = Typeface.DEFAULT

    var drawMode = DanmakuConfig.DrawMode.DEFAULT

    /**
     * K: 进度 [0, 1]
     * V: 帧 见 [Frame]
     */
    var keyframes = mutableMapOf<Float, Frame>()

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
        val stokePaint = when(drawMode) {
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

    private val bitmapPaint = Paint()

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

        val (lastPoint, lastAlpha, lastRX, lastRY, lastRZ) = lastFrame
        val (lastX, lastY) = lastPoint
        val (nextPoint, nextAlpha, nextRX, nextRY, nextRZ) = nextFrame
        val (nextX, nextY) = nextPoint
        var mProgress = (progress - lastKeyframeP) / (nextKeyframeP - lastKeyframeP)
        if (mProgress.isNaN()) mProgress = 1F

        val x = lastX + (nextX - lastX) * mProgress
        val y = lastY + (nextY - lastY) * mProgress
        val alpha = (lastAlpha + (nextAlpha - lastAlpha) * mProgress).toInt()
        val rx = lastRX + (nextRX - lastRX) * mProgress
        val ry = lastRY + (nextRY - lastRY) * mProgress
        val rz = lastRZ + (nextRZ - lastRZ) * mProgress

        val drawX = x * drawWidth
        val drawY = y * drawHeight

        bitmapPaint.alpha = alpha
        val matrix = Matrix()
        matrix.postScale(cos((rx * PI / 180)).toFloat(), cos(ry * PI / 180).toFloat())
        matrix.postRotate(rz)
        matrix.postTranslate(drawX, drawY)
        canvas.drawBitmap(bitmap, matrix, bitmapPaint)
        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        matrix.mapRect(rect)
        return rect
    }

    fun fillText() {
        if (text.isNotEmpty() && text.contains("\n")) {
            lines = text.split("\n").toTypedArray()
        }
    }
}