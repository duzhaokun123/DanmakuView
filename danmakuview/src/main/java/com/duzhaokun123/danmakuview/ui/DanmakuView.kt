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
import com.duzhaokun123.danmakuview.danmakuCount
import com.duzhaokun123.danmakuview.forEachDanmaku
import com.duzhaokun123.danmakuview.getOrNew
import com.duzhaokun123.danmakuview.interfaces.DanmakuParser
import com.duzhaokun123.danmakuview.model.DanmakuConfig
import com.duzhaokun123.danmakuview.model.Danmakus
import com.duzhaokun123.danmakuview.model.ShowingDanmakuInfo
import kotlinx.coroutines.*
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.math.min

class DanmakuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    companion object {
        const val TAG = "DanmakuView"

        val defaultDanmakuConfig = DanmakuConfig()

        const val POOL_UNDEFINED = -1
    }

    init {
        if (isInEditMode) {
            setBackgroundColor(Color.TRANSPARENT)
        } else {
            holder.addCallback(this)
            holder.setFormat(PixelFormat.TRANSLUCENT)
            launchDrawThread()
            launcherTimerTask()
        }
    }

    private var drawWidth = 0
    private var drawHeight = 0
    private var drawThread: Thread? = null
    private var drawRunning = false
    private var drawPaused = true
    private var parseJob: Job? = null
    private var timerTask: TimerTask? = null

    var danmakus = mutableMapOf(POOL_UNDEFINED to Danmakus())

    /**
     * 进行时间毫秒
     */
    val conductedTimeMs
        get() = conductedTimeUs / 1_000L

    /**
     * 进行时间微秒
     */
    var conductedTimeUs = 0L
        private set
    var showingDanmakus = listOf<ShowingDanmakuInfo>()
        private set
    var isDestroyed = false
        private set
    val isPaused
        get() = drawPaused
    val isShowing
        get() = visibility == VISIBLE
    var drawDebugInfo = false
    var danmakuConfig = defaultDanmakuConfig
    var onDanmakuClickListener: ((danmaku: ShowingDanmakuInfo) -> Unit)? = null

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

    /**
     * 弹幕绘制周期 1000ms / fps 得到
     */
    var period: Long = 16
        set(value) {
            field = value
            launcherTimerTask(value)
        }

    var debugPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40F
        typeface = Typeface.MONOSPACE
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawRunning = true
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        drawWidth = width
        drawHeight = height
        drawOneTime()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawRunning = false
    }

    fun pause() {
        drawPaused = true
    }

    fun resume() {
        drawPaused = false
    }

    fun seekTo(timeMs: Long) {
        conductedTimeUs = timeMs * 1_000
        drawOneTime()
    }

    @JvmOverloads
    fun start(offsetMs: Long = 0) {
        conductedTimeUs = offsetMs * 1_000
        resume()
    }

    fun destroy() {
        isDestroyed = true
        holder.removeCallback(this)
        parseJob?.cancel()
        timerTask?.cancel()
        drawOneTime()
    }

    @JvmOverloads
    fun parse(parser: DanmakuParser, onEnd: ((danmakus: Map<Int, Danmakus>) -> Unit)? = null) {
        parse({ parser.parse() }, onEnd)
    }

    fun parse(parser: (suspend CoroutineScope.() -> MutableMap<Int, Danmakus>)) {
        parse(parser, null)
    }

    fun parse(
        parser: (suspend CoroutineScope.() -> MutableMap<Int, Danmakus>),
        onEnd: ((danmakus: Map<Int, Danmakus>) -> Unit)?
    ) {
        parseJob?.cancel()
        var thisJob: Job? = null
        thisJob = GlobalScope.launch(Dispatchers.Default) {
            val a = parser()
            if (isActive) {
                danmakus = a
                drawPaused = true
                conductedTimeUs = 0
                onEnd?.invoke(danmakus)
                drawOneTime()
            }
            if (parseJob === thisJob)
                parseJob = null
        }
        parseJob = thisJob
    }

    fun drawOneTime() {
        drawThread ?: return
        synchronized(drawThread!!) {
            (drawThread as Object).notify()
        }
    }

    fun cleanCache() {
        GlobalScope.launch(Dispatchers.Default) {
            danmakus.forEachDanmaku { danmaku ->
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
            danmakus.forEachDanmaku { danmaku ->
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


    @JvmOverloads
    fun addDanmaku(danmaku: Danmaku, pool: Int = POOL_UNDEFINED) = danmakus.getOrNew(pool).add(danmaku)
    @JvmOverloads
    fun addDanmakus(danmakus: Collection<Danmaku>, pool: Int = POOL_UNDEFINED) = this.danmakus.getOrNew(pool).addAll(danmakus)
    @JvmOverloads
    fun removeDanmaku(danmaku: Danmaku, pool: Int = POOL_UNDEFINED) = danmakus.getOrNew(pool).remove(danmaku)
    @JvmOverloads
    fun removeDanmakus(pool: Int = POOL_UNDEFINED) = danmakus.getOrNew(pool).removeAll()
    fun addDanmakus(danmakus: Map<Int, Danmakus>) {
        danmakus.forEach { (i, danmakus) ->
            this.danmakus.getOrNew(i).addAll(danmakus)
        }
    }

    private var clickedDanmaku: ShowingDanmakuInfo? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        onDanmakuClickListener ?: return super.onTouchEvent(event)
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                showingDanmakus.forEach { sd ->
                    if (sd.rect.contains(event.x, event.y)) {
                        clickedDanmaku = sd
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

    private fun launchDrawThread() {
        drawThread = Thread {
            while (true) {
                synchronized(drawThread!!) {
                    (drawThread as Object).wait()
                }
                if (isDestroyed) break
                if (drawRunning) {
                    drawDanamkus()
                }
            }
            drawThread = null
        }
        drawThread!!.start()
    }

    private fun launcherTimerTask(period: Long = 16) {
        timerTask?.cancel()
        timerTask = Timer().schedule(0, period) {
            if (drawPaused) return@schedule
            conductedTimeUs += (period * speed * 1_000).toLong()
            drawOneTime()
        }
    }

    private fun drawDanamkus() {
        val start = System.nanoTime()
        var maxLine =
            (((drawHeight - danmakuConfig.marginTop - danmakuConfig.marginBottom) * danmakuConfig.maxRelativeHeight) / danmakuConfig.lineHeight).toInt()
        if (maxLine < 1) return
        maxLine = min(maxLine, danmakuConfig.maxLine)

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
        val willDrawDanmakus = mutableListOf<Triple<Danmaku, Float, Int>>()
        val conductedTime = conductedTimeMs
        danmakus.forEach { (pool, danmakus) ->
            runCatching {
                danmakus.forEach danmakuAction@{ danmaku ->
                    if (danmaku.visible.not()) return@danmakuAction

                    val duration = (danmaku.duration * danmakuConfig.durationScale)
                    val start = danmaku.offset
                    val end = (danmaku.offset + duration).toLong()
                    if (conductedTime in start..end) {
                        for (blocker in danmakuConfig.blockers) {
                            if (blocker.shouldBlock(danmaku, pool)) return@danmakuAction
                        }
                        val progress = (conductedTime - start) / duration
                        willDrawDanmakus.add(Triple(danmaku, progress, pool))
                    }
                }
            }
        }
        willDrawDanmakus.removeAll { (danmaku, progress, pool) ->
            val re: Boolean
            val oldShowingDanmaku = findOldShowingDanmaku(oldShowingDanmakus, danmaku, pool)
            re = if (oldShowingDanmaku != null) {
                drawDanmaku(
                    canvas, maxLine,
                    danmaku, progress, oldShowingDanmaku.line, pool,
                    willShowingDanmakus
                )
                true
            } else false
            return@removeAll re
        }
        willDrawDanmakus.forEach { (danmaku, progress, pool) ->
            if (danmaku is LineDanmaku) {
                var line = 1
                var moved: Boolean
                do {
                    moved = false
                    for (willShowingDanmaku in willShowingDanmakus) {
                        if (line == willShowingDanmaku.line && danmaku.javaClass == willShowingDanmaku.danmaku.javaClass && willShowingDanmaku.pool == pool) {
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
                drawDanmaku(canvas, maxLine, danmaku, progress, line, pool, willShowingDanmakus)
            } else {
                drawDanmaku(canvas, 0, danmaku, progress, 0, pool, willShowingDanmakus)
            }
        }
        showingDanmakus = willShowingDanmakus

        if (drawDebugInfo) {
            drawDebug(canvas, System.nanoTime() - start)
        }

        holder.unlockCanvasAndPost(canvas)
    }

    private fun findOldShowingDanmaku(
        oldShowingDanmakus: List<ShowingDanmakuInfo>, danmaku: Danmaku, pool: Int
    ): ShowingDanmakuInfo? {
        oldShowingDanmakus.forEach { info ->
            if (danmaku === info.danmaku && pool == info.pool) {
                return info
            }
        }
        return null
    }

    private fun drawDanmaku(
        canvas: Canvas, maxLine: Int,
        danmaku: Danmaku, progress: Float, line: Int, pool: Int,
        willShowingDanmakus: MutableList<ShowingDanmakuInfo>
    ) {
        if (line == 0) {
            danmaku.onDraw(canvas, drawWidth, drawHeight, progress, danmakuConfig, 0)?.let {
                willShowingDanmakus.add(ShowingDanmakuInfo(danmaku, it, 0, progress, pool))
            }
        } else if (line <= maxLine || danmakuConfig.allowOverlap) {
            var drawLine = line % maxLine
            if (drawLine == 0) drawLine = maxLine
            danmaku.onDraw(canvas, drawWidth, drawHeight, progress, danmakuConfig, drawLine)?.let {
                willShowingDanmakus.add(ShowingDanmakuInfo(danmaku, it, line, progress, pool))
            }
        }
    }

    private fun drawDebug(canvas: Canvas, frameTimeNs: Long) {
        canvas.drawText(
            "frameTime = ${frameTimeNs / 1_000_000F} ms",
            20F, drawHeight - 150F, debugPaint
        )
        canvas.drawText(
            "conductedTimeUs = $conductedTimeUs, speed = $speed, size = ${drawWidth}x$drawHeight",
            20F, drawHeight - 100F, debugPaint
        )
        canvas.drawText(
            "showingCount = ${showingDanmakus.size}, pools = ${danmakus.size}, count = ${danmakus.danmakuCount}",
            20F, drawHeight - 50F, debugPaint
        )
    }
}
