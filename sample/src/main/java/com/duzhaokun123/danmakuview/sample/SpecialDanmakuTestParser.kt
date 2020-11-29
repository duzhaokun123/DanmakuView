package com.duzhaokun123.danmakuview.sample

import android.graphics.PointF
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.danmaku.SpecialDanmaku
import com.duzhaokun123.danmakuview.interfaces.DanmakuParser
import com.duzhaokun123.danmakuview.model.Danmakus
import kotlin.random.Random

object SpecialDanmakuTestParser : DanmakuParser {
    override fun parse(): Danmakus {
        val danmakus = Danmakus()
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = Triple(PointF(0.5F, 0.5F), 0F, Value.ALPHA_MAX)
            keyframes[1F] = Triple(PointF(0.5F, 0.5F), 0F, Value.ALPHA_MAX)
            text = "高级弹幕"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = Triple(PointF(0.2F, 0.2F), 0F, Value.ALPHA_MAX)
            keyframes[1F] = Triple(PointF(0.2F, 0.2F), 360F, Value.ALPHA_MAX)
            text = "旋转------"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = Triple(PointF(0.8F, 0.8F), 0F, Value.ALPHA_MAX)
            keyframes[0.5F] = Triple(PointF(0.8F, 0.8F), 0F, Value.ALPHA_TRANSPARENT)
            keyframes[1F] = Triple(PointF(0.8F, 0.8F), 0F, Value.ALPHA_MAX)
            text = "变 Alpha ******"
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            keyframes[0F] = Triple(PointF(0.0F, 0.0F), 0F, Value.ALPHA_MAX)
            keyframes[0.2F] = Triple(PointF(0.3F, 0.5F), 0F, Value.ALPHA_MAX)
            keyframes[0.4F] = Triple(PointF(0.9F, 0.1F), 0F, Value.ALPHA_MAX)
            keyframes[0.6F] = Triple(PointF(0.9F, 0.6F), 0F, Value.ALPHA_MAX)
            keyframes[0.8F] = Triple(PointF(0.44F, 0.7F), 0F, Value.ALPHA_MAX)
            keyframes[1F] = Triple(PointF(1.8F, 1F), 0F, Value.ALPHA_MAX)
            text = "===移动==="
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            text = "多行\n行1\n行2\n行3"
            fillText()
            keyframes[0F] = Triple(PointF(0.2F, 0.8F), 0F, Value.ALPHA_MAX)
            keyframes[1F] = Triple(PointF(0.2F, 0.8F), 0F, Value.ALPHA_MAX)
            textSize = 50F
            offset = 0
        })
        danmakus.add(SpecialDanmaku().apply {
            text = "随机生成"
            textSize = 50F
            offset = 0
            val createRandomFrame = {
                Triple(
                    PointF(
                        Random.nextDouble(0.0, 1.0).toFloat(),
                        Random.nextDouble(0.0, 1.0).toFloat()
                    ),
                    Random.nextDouble(0.0, 720.0).toFloat(),
                    Random.nextInt(0, 256)
                )
            }
            for (i in 0..8) {
                keyframes[Random.nextDouble(0.0, 1.0).toFloat()] = createRandomFrame()
            }
            keyframes[0F] = createRandomFrame()
            keyframes[1F] = createRandomFrame()
        })
        return danmakus
    }
}