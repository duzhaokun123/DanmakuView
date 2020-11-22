package com.duzhaokun123.danmakuview.sample

import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import com.duzhaokun123.danmakuview.Value
import com.duzhaokun123.danmakuview.danmaku.SimpleDanmakuFactory
import com.duzhaokun123.danmakuview.danmaku.SpecialDanmaku
import com.duzhaokun123.danmakuview.interfaces.DanmakuParser
import com.duzhaokun123.danmakuview.model.Danmakus
import org.json.JSONArray
import org.json.JSONException
import java.io.InputStream
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants

class XMLDanmakuParser(inputStream: InputStream) : DanmakuParser {
    companion object {
        const val TAG = "XMLDanmakuParser"

        const val BILI_PLAYER_WIDTH = 682.0F
        const val BILI_PLAYER_HEIGHT = 438.0F
    }

    private val simpleDanmakuFactory by lazy { SimpleDanmakuFactory() }

    private val danmakus by lazy {
        val danmakus = Danmakus()

        val xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream)
        var startD = false
        var p: String? = null
        while (xmlEventReader.hasNext()) {
            val event = xmlEventReader.nextEvent()
            when (event.eventType) {
                XMLStreamConstants.START_ELEMENT -> {
                    with(event.asStartElement()) {
                        startD = name.localPart == "d"
                        if (startD) {
                            p = getAttributeByName(QName("p")).value
                        }
                    }
                }
                XMLStreamConstants.CHARACTERS -> {
                    //如果前一个解析到的是 d 标签, 那么此处得到的一定是 d 标签的 body
                    if (startD) {
                        java.util.StringTokenizer(p, ",").let { tokens ->
                            val text = event.asCharacters().data
                            //FIXME: 见鬼了, 解析出一堆 \n
                            if (text.startsWith("\n")) return@let
                            val offset = (tokens.nextToken().toFloat() * 1000).toLong() // offset
                            val danmaku = simpleDanmakuFactory.create(
                                tokens.nextToken().toInt().toType() // type
                            )
                            danmaku.text = text
                            danmaku.offset = offset
                            danmaku.textSize = tokens.nextToken().toFloat() // textSize
                            val color =
                                (-0x1000000L or tokens.nextToken().toLong() and -0x1).toInt()
                            danmaku.textColor = color
                            danmaku.textShadowColor =
                                if (color <= android.graphics.Color.BLACK) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                            if (danmaku is SpecialDanmaku) initialSpecialDanmakuData(danmaku)
//                        initialSpecailDanmakuData(danmaku, mContext, mDispScaleX, mDispScaleY)
                            danmakus.add(danmaku)
                        }
                    }
                }
            }
        }
        danmakus
    }

    override fun parse() = danmakus

    private fun initialSpecialDanmakuData(danmaku: SpecialDanmaku) {
        val text = danmaku.text.trim { it <= ' ' }
        if (text.startsWith('[')) {
            var textArray: Array<String?>? = null
            try {
                val jsonArray = JSONArray(text)
                textArray = arrayOfNulls(jsonArray.length())
                for (i in textArray.indices) {
                    textArray[i] = jsonArray.getString(i)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (textArray != null && textArray.size >= 5 && textArray[4].isNullOrEmpty().not()) {
                danmaku.text = textArray[4]!!
                danmaku.fillText()
                var beginX = textArray[0]!!.toFloatOrDefault()
                var beginY = textArray[1]!!.toFloatOrDefault()
                var endX = beginX
                var endY = beginY
                val alphaArray = textArray[2]!!.split("-".toRegex()).toTypedArray()
                val beginAlpha = (Value.ALPHA_MAX * alphaArray[0].toFloatOrDefault()).toInt()
                var endAlpha = beginAlpha
                if (alphaArray.size > 1) {
                    endAlpha = (Value.ALPHA_MAX * alphaArray[1].toFloatOrDefault()).toInt()
                }
                val alphaDuration = (textArray[3]!!.toFloatOrDefault() * 1000).toLong()
                var translationDuration = alphaDuration
                var translationStartDelay = 0L
                var rotateY = 0F
                var rotateZ = 0F
                if (textArray.size >= 7) {
                    rotateZ = textArray[5]!!.toFloatOrDefault()
                    rotateY = textArray[6]!!.toFloatOrDefault()
                }
                if (textArray.size >= 11) {
                    endX = textArray[7]!!.toFloatOrDefault()
                    endY = textArray[8]!!.toFloatOrDefault()
                    if (textArray[9].isNullOrEmpty().not())
                        translationDuration = textArray[9]!!.toLong()
                    if (textArray[10].isNullOrEmpty().not())
                        translationStartDelay = textArray[10]!!.toFloatOrDefault().toLong()

                }
                if (textArray[0]!!.contains('.')) {
                    beginX *= BILI_PLAYER_WIDTH
                }
                if (textArray[1]!!.contains('.')) {
                    beginY *= BILI_PLAYER_HEIGHT
                }
                if (textArray.size >= 8 && textArray[7]!!.contains('.')) {
                    endX *= BILI_PLAYER_WIDTH
                }
                if (textArray.size >= 9 && textArray[8]!!.contains('.')) {
                    endY *= BILI_PLAYER_HEIGHT
                }
                danmaku.duration = alphaDuration
                danmaku.rotationZ = rotateZ
                danmaku.rotationY = rotateY
                danmaku.beginX = beginX
                danmaku.beginY = beginY
                danmaku.endX = endX
                danmaku.endY = endY
                danmaku.translationDuration = translationDuration
                danmaku.translationStartDelay = translationStartDelay
                danmaku.beginAlpha = beginAlpha
                danmaku.endAlpha = endAlpha
                if (textArray.size >= 12) {
                    if (textArray[11].isNullOrEmpty().not() && textArray[11].toBoolean()) {
                        danmaku.textShadowColor = Color.TRANSPARENT
                    }
                }
                if (textArray.size >= 13) {
                    //TODO 字体 textArray[12]
                }
                if (textArray.size >= 14) {
                    // Linear.easeIn or Quadratic.easeOut
                    danmaku.isQuadraticEaseOut = "0" == textArray[13]
                }
                if (textArray.size >= 15) {
                    // 路径数据
                    if ("" != textArray[14]) {
                        val motionPathString = textArray[14]!!.substring(1)
                        if (!TextUtils.isEmpty(motionPathString)) {
                            val pointStrArray = motionPathString.split("L".toRegex()).toTypedArray()
                            if (pointStrArray.isNotEmpty()) {
                                val points = Array(pointStrArray.size) { FloatArray(2) }
                                for (i in pointStrArray.indices) {
                                    val pointArray =
                                        pointStrArray[i].split(",".toRegex()).toTypedArray()
                                    if (pointArray.size >= 2) {
                                        points[i][0] = pointArray[0].toFloatOrDefault()
                                        points[i][1] = pointArray[1].toFloatOrDefault()
                                    }
                                }
                                danmaku.setLinePathData(points)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Int.toType(): SimpleDanmakuFactory.Type {
        return when (this) {
            1 -> SimpleDanmakuFactory.Type.R2L_DANMAKU
            4 -> SimpleDanmakuFactory.Type.BOTTOM_DANMAKU
            5 -> SimpleDanmakuFactory.Type.TOP_DANMAKU
            6 -> SimpleDanmakuFactory.Type.L2R_DANMAKU
            7 -> SimpleDanmakuFactory.Type.SPECIAL_DANMAKU
            else -> {
                Log.e(TAG, "unknowen type $this, R2L_DANMAKU as default")
                SimpleDanmakuFactory.Type.R2L_DANMAKU
            }
        }
    }
}