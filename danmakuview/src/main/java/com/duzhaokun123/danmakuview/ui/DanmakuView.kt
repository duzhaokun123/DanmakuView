package com.duzhaokun123.danmakuview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.duzhaokun123.danmakuview.clean
import com.duzhaokun123.danmakuview.danmaku.*
import com.duzhaokun123.danmakuview.interfaces.DanmakuParser
import com.duzhaokun123.danmakuview.model.DanmakuConfig
import com.duzhaokun123.danmakuview.model.Danmakus
import com.duzhaokun123.danmakuview.model.ShowingDanmakuInfo
import kotlinx.coroutines.*

class DanmakuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    companion object {
        const val TAG = "DanmakuView"

        val defaultDanmakuConfig = DanmakuConfig()
    }

    init {
        if (isInEditMode) {
            setBackgroundColor(Color.TRANSPARENT)
        } else {
            holder.addCallback(this)
            holder.setFormat(PixelFormat.TRANSLUCENT)
        }
    }

    private var drawWidth = 0
    private var drawHeight = 0
    private var drawJob: Job? = null
    private var drawRunning = false
    private var drawPaused = true
    private var drawOnceResume = false
    private var parseJob: Job? = null

    var danmakus = Danmakus()

    /**
     * 进行时间毫秒
     */
    val conductedTimeMs
        get() = conductedTimeNs / 1_000_000

    /**
     * 进行时间纳秒 (不准确)
     */
    var conductedTimeNs = 0L
        private set
    var showingDanmakus = listOf<ShowingDanmakuInfo>()
        private set
    var isDestroied = false
        private set
    val isPaused
        get() = drawPaused
    val isShowing
        get() = visibility == VISIBLE
    var drawDebugInfo = false
    var danmakuConfig = defaultDanmakuConfig
    var onDanmakuClickListener: ((danmaku: Danmaku) -> Unit)? = null

    /**
     * -1: 可以倒放
     */
    var speed = 1F

    /**
     * 提升 Z轴
     * false: 会完全挡住下面的普通 View, 但不会挡住其他 Surface
     * true: 不透明部分会挡住在上和下的所有 View
     */
    var zOnTop = false
        set(value) {
            field = value
            setZOrderOnTop(value)
        }

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawRunning = true
        launchDrawJob()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        drawWidth = width
        drawHeight = height
        drawOnceResume = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawRunning = false
    }

    fun pause() {
        drawPaused = true
    }

    fun resume() {
        if (drawPaused) {
            drawPaused = false
            //计时不是很准
            Thread {
                while (true) {
                    if (isDestroied || drawPaused) break
                    val startTime = System.nanoTime()
                    Thread.sleep(16)
                    conductedTimeNs += ((System.nanoTime() - startTime) * speed).toLong()
                }
            }.apply {
                priority = Thread.MAX_PRIORITY
            }.start()
        }
    }

    fun seekTo(timeMs: Long) {
        conductedTimeNs = timeMs * 1_000_000
        drawOnceResume = true
    }

    @JvmOverloads
    fun start(offsetMs: Long = 0) {
        conductedTimeNs = offsetMs * 1_000_000
        resume()
    }

    fun destroy() {
        isDestroied = true
        holder.removeCallback(this)
        parseJob?.cancel()
    }

    @JvmOverloads
    fun parse(parser: DanmakuParser, onEnd: ((danmakus: Danmakus) -> Unit)? = null) {
        parse({ parser.parse() }, onEnd)
    }

    fun parse(parser: (suspend CoroutineScope.() -> Danmakus)) {
        parse(parser, null)
    }

    fun parse(
        parser: (suspend CoroutineScope.() -> Danmakus),
        onEnd: ((danmakus: Danmakus) -> Unit)?
    ) {
        parseJob?.cancel()
        var thisJob: Job? = null
        thisJob = GlobalScope.launch(Dispatchers.Default) {
            val a = parser()
            if (isActive) {
                danmakus = a
                drawPaused = true
                conductedTimeNs = 0
                drawOnceResume = true
                onEnd?.invoke(danmakus)
            }
            if (parseJob === thisJob)
                parseJob = null
        }
        parseJob = thisJob
    }

    fun drawOnce() {
        drawOnceResume = true
    }

    fun cleanCache() {
        GlobalScope.launch(Dispatchers.Default) {
            danmakus.forEach { danmaku ->
                danmaku.cache = null
            }
        }
    }

    /**
     * @param recache true: 重构已有缓存
     */
    @JvmOverloads
    fun buildCache(recache: Boolean = false) {
        GlobalScope.launch(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()
            var count = 0
            danmakus.forEach { danmaku ->
                if (danmaku.cacheable && (danmaku.cache == null || recache)) {
                    danmaku.onBuildCache(danmakuConfig)
                    if (danmaku.cache != null) count++
                }
            }
            val endTime = System.currentTimeMillis()
            Log.d(
                TAG,
                "buildCache: $count of ${danmakus.size} danmakus in ${endTime - startTime} ms"
            )
        }
    }


    fun addDanmaku(danmaku: Danmaku) = danmakus.add(danmaku)
    fun addDanmakus(danmakus: Collection<Danmaku>) = this.danmakus.addAll(danmakus)
    fun removeDanmaku(danmaku: Danmaku) = danmakus.remove(danmaku)
    fun removeDanmakus() = danmakus.removeAll()

    private var clickedDanmaku: Danmaku? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        onDanmakuClickListener ?: return super.onTouchEvent(event)
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                showingDanmakus.forEach { sd ->
                    if (sd.rect.contains(event.x, event.y)) {
                        clickedDanmaku = sd.danmaku
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                clickedDanmaku?.let { onDanmakuClickListener?.invoke(it) }
                clickedDanmaku = null
            }
        }
        return super.onTouchEvent(event)
    }

    private fun launchDrawJob() {
        if (drawJob != null) return
        drawJob = GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                if (drawRunning.not()) break

                val startTime = System.currentTimeMillis()

                drawDanamkus()

                val endTime = System.currentTimeMillis()
                val deltaTime = endTime - startTime

                if (deltaTime < 16) {
                    delay(deltaTime)
                }

                while (drawPaused && drawRunning && drawOnceResume.not()) {
                    delay(1)
                }
                if (drawOnceResume) drawOnceResume = false
            }
            drawJob = null
        }
    }

    private fun drawDanamkus() {
        val maxLine =
            (drawHeight - danmakuConfig.marginTop - danmakuConfig.marginBottom) / danmakuConfig.lineHeight
        if (maxLine < 1) return

        val canvas = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                holder.lockHardwareCanvas()
            else
                holder.lockCanvas()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            holder.lockCanvas()
        } ?: return

        canvas.clean()
        val oldShowingDanmakus = showingDanmakus
        val willShowingDanmakus = mutableListOf<ShowingDanmakuInfo>()
        val willDrawDanmakus = mutableListOf<Pair<Danmaku, Float>>()
        val conductedTime = conductedTimeMs
        danmakus.forEach { danmaku ->
            if (danmaku.visibility.not()) return@forEach

            val duration = (danmaku.duration * danmakuConfig.durationCoeff)
            val start = danmaku.offset
            val end = (danmaku.offset + duration).toLong()
            if (conductedTime in start..end) {
                for (blocker in danmakuConfig.blockers) {
                    if (blocker.shouldBlock(danmaku)) return@forEach
                }
                val progress = (conductedTime - start) / duration
                willDrawDanmakus.add(danmaku to progress)
            }
        }
        willDrawDanmakus.removeAll { (danmaku, progress) ->
            val re: Boolean
            val olsShowingDanmaku = findOldShowingDanmaku(oldShowingDanmakus, danmaku)
            re = if (olsShowingDanmaku != null) {
                drawDanmaku(
                    canvas, maxLine,
                    danmaku, progress, olsShowingDanmaku.line,
                    willShowingDanmakus
                )
                true
            } else false
            re
        }
        willDrawDanmakus.forEach { (danmaku, progress) ->
            if (danmaku is LineDanmaku) {
                var line = 1
                var moved: Boolean
                do {
                    moved = false
                    for (willShowingDanmaku in willShowingDanmakus) {
                        if (line == willShowingDanmaku.line && danmaku.javaClass == willShowingDanmaku.danmaku.javaClass) {
                            if (danmaku.willHit(
                                    willShowingDanmaku.danmaku,
                                    drawWidth,
                                    drawHeight,
                                    danmakuConfig
                                )
                            ) {
                                line++
                                moved = true
                                break
                            }
                        }
                    }
                } while (moved)
                drawDanmaku(canvas, maxLine, danmaku, progress, line, willShowingDanmakus)
            } else {
                drawDanmaku(canvas, 0, danmaku, progress, 0, willShowingDanmakus)
            }
        }
        showingDanmakus = willShowingDanmakus

        if (drawDebugInfo) {
            drawDebug(canvas)
        }

        holder.unlockCanvasAndPost(canvas)
    }

    private fun findOldShowingDanmaku(
        oldShowingDanmakus: List<ShowingDanmakuInfo>, danmaku: Danmaku
    ): ShowingDanmakuInfo? {
        var re: ShowingDanmakuInfo? = null
        oldShowingDanmakus.forEach { info ->
            if (danmaku === info.danmaku)
                re = info
        }
        return re
    }

    private fun drawDanmaku(
        canvas: Canvas, maxLine: Int,
        danmaku: Danmaku, progress: Float, line: Int,
        willShowingDanmakus: MutableList<ShowingDanmakuInfo>
    ) {
        if (line == 0) {
            danmaku.onDraw(canvas, drawWidth, drawHeight, progress, danmakuConfig, 0)?.let {
                willShowingDanmakus.add(ShowingDanmakuInfo(danmaku, it, 0, progress))
            }
        } else if (line < maxLine || danmakuConfig.allowCovering) {
            var drawLine = line % maxLine
            if (drawLine == 0) drawLine = maxLine
            danmaku.onDraw(canvas, drawWidth, drawHeight, progress, danmakuConfig, drawLine)?.let {
                willShowingDanmakus.add(ShowingDanmakuInfo(danmaku, it, line, progress))
            }
        }
    }

    private val debugPaint by lazy {
        Paint().apply {
            color = Color.WHITE
            textSize = 40F
        }
    }

    private fun drawDebug(canvas: Canvas) {
        canvas.drawText(
            "conductedTimeNs = $conductedTimeNs, speed = $speed, size = ${drawWidth}x$drawHeight",
            20F, drawHeight - 100F, debugPaint
        )
        canvas.drawText(
            "showingCount = ${showingDanmakus.size}, count = ${danmakus.size}",
            20F, drawHeight - 50F, debugPaint
        )
    }
}
