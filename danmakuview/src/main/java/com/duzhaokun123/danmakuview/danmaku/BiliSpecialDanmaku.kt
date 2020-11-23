package com.duzhaokun123.danmakuview.danmaku

import android.graphics.*
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.getDistance
import com.duzhaokun123.danmakuview.model.DanmakuConfig

class BiliSpecialDanmaku : Danmaku() {
    companion object {
        const val BILI_PLAYER_WIDTH = 682.0F
        const val BILI_PLAYER_HEIGHT = 438.0F
    }

    var lines: Array<String>? = null

    var rotationZ = 0F
    var rotationY = 0F

    var beginX = 0F
    var beginY = 0F

    var endX = 0F
    var endY = 0F

    val deltaX
        get() = endX - beginX
    val deltaY
        get() = endY - beginY

    var translationDuration = 0L
    var translationStartDelay = 0L

    var beginAlpha = 0
    var endAlpha = 0
    val deltaAlpha
        get() = endAlpha - beginAlpha

    var linePaths: Array<LinePath?>? = null

    var isQuadraticEaseOut = false

    override val cacheable = true

    override fun onBuildCache(danmakuConfig: DanmakuConfig) {
        val text = (if (lines != null) lines else arrayOf(text))!!
        var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@BiliSpecialDanmaku.textColor
            alpha = Value.ALPHA_MAX
            textSize = this@BiliSpecialDanmaku.textSize
            typeface = danmakuConfig.typeface
            isUnderlineText = underline
            setShadowLayer(
                danmakuConfig.shadowRadius,
                danmakuConfig.shadowDx,
                danmakuConfig.shadowDy,
                danmakuConfig.shadowColor
            )
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
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        var nextY = 0F
        text.forEachIndexed { i, s ->
            nextY += bounderses[i].height().toFloat()
            canvas.drawText(s, 0F, nextY, paint)
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

    val bitmapPaint by lazy { Paint() }

    override fun onDraw(
        canvas: Canvas,
        drawWidth: Int, drawHeight: Int, progress: Float,
        danmakuConfig: DanmakuConfig, line: Int
    ): RectF? {
        // TODO: 20-11-21 Z 旋转, 路径, 延迟变换
        if (cache == null) onBuildCache(danmakuConfig)
        val bitmap = cache ?: return null
        bitmapPaint.alpha = beginAlpha + (deltaAlpha * progress).toInt()
        val x = beginX + deltaX * progress
        val y = beginY + deltaY * progress
        val (drawX, drawY) = getDrawXY(x, y, drawWidth, drawHeight)
        return if (rotationY == 0F) {
            canvas.drawBitmap(bitmap, drawX, drawY, bitmapPaint)
            RectF(drawX, drawY, drawX + bitmap.width, drawY + bitmap.height)
        } else {
            val matrix = Matrix()
            matrix.postRotate(rotationY)
            matrix.postTranslate(drawX, drawY)
            canvas.drawBitmap(bitmap, matrix, bitmapPaint)
            val rect = RectF()
            matrix.mapRect(rect)
            rect
        }
    }

    fun fillText() {
        if (text.isNotEmpty() && text.contains("/n")) {
            lines = text.split("/n").toTypedArray()
        }
    }

    fun setLinePathData(points: Array<FloatArray>) {
        val length = points.size
        beginX = points[0][0]
        beginY = points[0][1]
        endX = points[length - 1][0]
        endY = points[length - 1][1]
        if (points.size > 1) {
            this.linePaths = arrayOfNulls(points.size - 1)
            for (i in linePaths!!.indices) {
                this.linePaths!![i] = LinePath().apply {
                    setPoints(
                        PointF(points[i][0], points[i][1]),
                        PointF(points[i + 1][0], points[i + 1][1])
                    )
                }
            }
            var totalDistance = 0F
            val var4: Array<LinePath?> = this.linePaths!!
            val var5 = var4.size
            var var6 = 0
            while (var6 < var5) {
                val line = var4[var6]!!
                totalDistance += line.distance
                ++var6
            }
            var lastLine: LinePath? = null
            val var11: Array<LinePath?> = this.linePaths!!
            var6 = var11.size
            for (var12 in 0 until var6) {
                val line = var11[var12]!!
                line.duration =
                    (line.distance / totalDistance * translationDuration.toFloat()).toLong()
                line.beginTime = lastLine?.endTime ?: 0L
                line.endTime = line.beginTime + line.duration
                lastLine = line
            }
        }
    }

    private fun getDrawXY(x: Float, y: Float, drawWidth: Int, drawHeight: Int): Pair<Float, Float> {
        val drawX = x / BILI_PLAYER_WIDTH * drawWidth
        val drawY = y / BILI_PLAYER_HEIGHT * drawHeight
        return drawX to drawY
    }

    class LinePath {
        var pBegin: PointF? = null
        var pEnd: PointF? = null
        var duration = 0L
        var beginTime = 0L
        var endTime = 0L
        var deltaX = 0F
        var deltaY = 0F
        fun setPoints(
            pBegin: PointF,
            pEnd: PointF
        ) {
            this.pBegin = pBegin
            this.pEnd = pEnd
            deltaX = (pEnd.x - pBegin.x)
            deltaY = pEnd.y - pBegin.y
        }

        val distance: Float
            get() = pEnd!!.getDistance(pBegin!!)

        val beginPoint: FloatArray
            get() = floatArrayOf(pBegin!!.x, pBegin!!.y)

        val endPoint: FloatArray
            get() = floatArrayOf(pEnd!!.x, pEnd!!.y)

    }
}