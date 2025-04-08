package com.duzhaokun123.danmakuview.sample

import android.graphics.PointF
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.danmaku.SpecialDanmaku
import com.duzhaokun123.danmakuview.interfaces.DanmakuParser
import com.duzhaokun123.danmakuview.model.Danmakus
import com.duzhaokun123.danmakuview.ui.DanmakuView
import kotlin.random.Random

object SpecialDanmakuTestParser : DanmakuParser {
    override fun parse(): MutableMap<Int, Danmakus> {
        val danmakus = Danmakus()
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.5F, 0.5F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.5F, 0.5F), Value.ALPHA_MAX, 0F, 0F, 0F)
            text = "高级弹幕"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.2F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.2F), Value.ALPHA_MAX, 360F, 0F, 0F)
            text = "X 旋转 ------"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.2F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.2F), Value.ALPHA_MAX, 0F, 360F, 0F)
            text = "Y 旋转 ||||||"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.2F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.2F), Value.ALPHA_MAX, 0F, 0F, 360F)
            text = "Z 旋转 ++++++"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.3F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.3F), Value.ALPHA_MAX, 360F, 360F, 0F)
            text = "XY 旋转 -|-|-|"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.3F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.3F), Value.ALPHA_MAX, 360F, 0F, 360F)
            text = "XZ 旋转 -+-+-+"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.3F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.3F), Value.ALPHA_MAX, 0F, 360F, 360F)
            text = "YZ 旋转 |+|+|+"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.4F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.4F), Value.ALPHA_MAX, 360F, 360F, 360F)
            text = "XYZ 旋转 @@@@@@"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.8F, 0.8F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[0.5F] = SpecialDanmaku.Frame(PointF(0.8F, 0.8F), Value.ALPHA_TRANSPARENT, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.8F, 0.8F), Value.ALPHA_MAX, 0F, 0F, 0F)
            text = "变 Alpha ******"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.0F, 0.0F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[0.2F] = SpecialDanmaku.Frame(PointF(0.3F, 0.5F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[0.4F] = SpecialDanmaku.Frame(PointF(0.9F, 0.1F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[0.6F] = SpecialDanmaku.Frame(PointF(0.9F, 0.6F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[0.8F] = SpecialDanmaku.Frame(PointF(0.44F, 0.7F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(1.8F, 1F), Value.ALPHA_MAX, 0F, 0F, 0F)
            text = "===移动==="
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            text = "多行\n行1\n行2\n行3"
            fillText()
            keyframes[0F] = SpecialDanmaku.Frame(PointF(0.2F, 0.8F), Value.ALPHA_MAX, 0F, 0F, 0F)
            keyframes[1F] = SpecialDanmaku.Frame(PointF(0.2F, 0.8F), Value.ALPHA_MAX, 0F, 0F, 0F)
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            text = "随机生成"
            textSize = 50F
            offset = 0
            val createRandomFrame = {
                SpecialDanmaku.Frame(
                    PointF(
                        Random.nextDouble(0.0, 1.0).toFloat(),
                        Random.nextDouble(0.0, 1.0).toFloat()
                    ),
                    Random.nextInt(0, 256),
                    Random.nextDouble(0.0, 720.0).toFloat(),
                    Random.nextDouble(0.0, 720.0).toFloat(),
                    Random.nextDouble(0.0, 720.0).toFloat(),
                )
            }
            (0..8).forEach {
                keyframes[Random.nextDouble(0.0, 1.0).toFloat()] = createRandomFrame()
            }
            keyframes[0F] = createRandomFrame()
            keyframes[1F] = createRandomFrame()
        })
        return mutableMapOf(DanmakuView.POOL_UNDEFINED to danmakus)
    }
}