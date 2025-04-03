package com.duzhaokun123.danmakuview.model

import android.graphics.Color
import android.graphics.Typeface
import com.duzhaokun123.danmakuview.interfaces.DanmakuBlocker

class DanmakuConfig {
    enum class DrawMode {
        DEFAULT, SHADOW, STROKE, SHADOW_STROKE
    }

    /**
     * 字体
     */
    var typeface: Typeface = Typeface.DEFAULT

    /**
     * 持续时间系数
     */
    var durationScale = 1F

    /**
     * 字体大小系数
     */
    var textSizeScale = 1F

    /**
     * 行高 (像素)
     */
    var lineHeight = 40

    /**
     * 上边距
     */
    var marginTop = 0

    /**
     * 下边距
     */
    var marginBottom = 0

    /**
     * 绘制模式
     */
    var drawMode = DrawMode.DEFAULT

    /**
     * 阴影半径
     */
    var shadowRadius = 5F

    /**
     * 阴影 X 偏移
     */
    var shadowDx = 0F

    /**
     * 阴影 Y 偏移
     */
    var shadowDy = 0F

    /**
     * 后备阴影颜色
     */
    var shadowColor = Color.DKGRAY

    /**
     * 描边宽度
     */
    var stokeWidth = 1F

    /**
     * 允许覆盖
     */
    var allowOverlap = false

    /**
     * 屏蔽器
     */
    var blockers = mutableSetOf<DanmakuBlocker>()

    /**
     * 最大行数
     *
     * 与 [maxRelativeHeight] 一同设置时取最小值
     */
    var maxLine = Int.MAX_VALUE

    /**
     * 最大相对高度 (0, 1]
     *
     * 与 [maxLine] 一同设置时取最小值
     */
    var maxRelativeHeight = 1F

    /**
     * 也许你需要一些自己但弹幕类的特别设置
     */
    val custom = mutableMapOf<String, String>()
}