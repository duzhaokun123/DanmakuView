package com.duzhaokun123.danmakuview.danmaku

import android.graphics.*
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.model.DanmakuConfig
import com.duzhaokun123.danmakuview.soft

abstract class Danmaku {
    /**
     * 偏移时间
     */
    var offset = 0L

    /**
     * 文本
     */
    var text = ""

    /**
     * 文字颜色
     */
    var textColor = Color.WHITE

    /**
     * 阴影/描边颜色
     */
    var textShadowColor = 0

    /**
     * 下划线
     */
    var underline = false

    /**
     * 字体大小
     */
    var textSize = 25F

    /**
     * 框的颜色,0表示无框
     */
    var borderColor = 0

//    /**
//     * 内边距(像素)
//     */
//    var padding = 0

    /**
     * 存活时间(毫秒)
     */
    var duration = 5000L

    /**
     * 绘制用缓存
     */
    var cache: Bitmap? by soft { null }

    /**
     * 透明度
     */
    var alpha = Value.ALPHA_MAX

    /**
     * 可见性
     */
    var visibility = true

    var tag: Any? = null

    /**
     * 可缓存
     */
    abstract val cacheable: Boolean

    abstract fun onBuildCache(danmakuConfig: DanmakuConfig)

    /**
     * @param drawWidth 画布宽
     * @param drawHeight 画布高
     * @param progress 进度 [0, 1]
     * @param line 行弹幕绘制在第几行, 从 1 开始计数, 对于非行弹幕常为 0
     * @return 画到哪了 null: 没画
     */
    abstract fun onDraw(
        canvas: Canvas, drawWidth: Int, drawHeight: Int,
        progress: Float, danmakuConfig: DanmakuConfig, line: Int
    ): RectF?

    override fun toString(): String {
        return text
    }
}