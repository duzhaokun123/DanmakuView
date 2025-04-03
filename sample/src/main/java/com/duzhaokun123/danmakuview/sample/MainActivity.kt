package com.duzhaokun123.danmakuview.sample

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.duzhaokun123.danmakuview.danmaku.R2LDanmaku
import com.duzhaokun123.danmakuview.danmaku.TopDanmaku
import com.duzhaokun123.danmakuview.getOrNew
import com.duzhaokun123.danmakuview.interfaces.DanmakuParser
import com.duzhaokun123.danmakuview.model.DanmakuConfig
import com.duzhaokun123.danmakuview.model.Danmakus
import com.duzhaokun123.danmakuview.sample.databinding.ActivityMainBinding
import com.duzhaokun123.danmakuview.ui.DanmakuView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var baseBinding: ActivityMainBinding
    private val openXmlFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it ?: return@registerForActivityResult
        parserXMLDanmaku(contentResolver.openInputStream(it)!!)
    }
    private val openBackgroundImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it ?: return@registerForActivityResult
        val img = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
        baseBinding.root.background = img.toDrawable(resources)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        baseBinding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        baseBinding.btnHide.setOnClickListener { baseBinding.dv.visibility = View.INVISIBLE }
        baseBinding.btnShow.setOnClickListener { baseBinding.dv.visibility = View.VISIBLE }
        baseBinding.btnSend.setOnClickListener {
            baseBinding.dv.addDanmaku(R2LDanmaku().apply {
                offset = baseBinding.dv.conductedTimeMs
                text = "danmaku"
                borderColor = Color.GREEN
            })
        }
        baseBinding.btnPause.setOnClickListener { baseBinding.dv.pause() }
        baseBinding.btnResume.setOnClickListener { baseBinding.dv.resume() }
        baseBinding.btnStart.setOnClickListener { baseBinding.dv.start() }
        baseBinding.btnBuildCache.setOnClickListener { baseBinding.dv.buildCache() }
        baseBinding.btnCleanCache.setOnClickListener { baseBinding.dv.cleanCache() }
        baseBinding.btnDrawOneTime.setOnClickListener { baseBinding.dv.drawOneTime() }
        baseBinding.sbSpeed.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            val speed = (value - 200) / 100F
            baseBinding.dv.speed = speed
            baseBinding.tvSpeed.text = speed.toString()
        })
        baseBinding.sbDurationScale.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            if (value != 0) {
                val durationScale = value / 100F
                baseBinding.dv.danmakuConfig.durationScale = durationScale
                baseBinding.tvDurationScale.text = durationScale.toString()
            }
        })
        baseBinding.sbTextSizeScale.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            if (value != 0) {
                val textSizeScale = value / 100F
                baseBinding.dv.danmakuConfig.textSizeScale = textSizeScale
                baseBinding.tvTextSizeScale.text = textSizeScale.toString()
            }
        })
        baseBinding.sbLineHeight.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            val lineHeight = value + 20
            baseBinding.dv.danmakuConfig.lineHeight = lineHeight
            baseBinding.tvLineHeight.text = lineHeight.toString()
        })
        baseBinding.sbMarginTop.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            baseBinding.dv.danmakuConfig.marginTop = value
            baseBinding.tvMarginTop.text = value.toString()
        })
        baseBinding.sbMarginBottom.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            baseBinding.dv.danmakuConfig.marginBottom = value
            baseBinding.tvMarginBottom.text = value.toString()
        })
        baseBinding.scAllowCovering.setOnCheckedChangeListener { _, isChecked ->
            baseBinding.dv.danmakuConfig.allowCovering = isChecked
        }
        baseBinding.btnTypeface.setOnClickListener {
            showPopupMenu(R.menu.typeface, it) { item ->
                when(item.itemId) {
                    R.id.a -> baseBinding.dv.danmakuConfig.typeface = Typeface.DEFAULT
                    R.id.b -> baseBinding.dv.danmakuConfig.typeface = Typeface.DEFAULT_BOLD
                    R.id.c -> baseBinding.dv.danmakuConfig.typeface = Typeface.SANS_SERIF
                    R.id.d -> baseBinding.dv.danmakuConfig.typeface = Typeface.SERIF
                    R.id.e -> baseBinding.dv.danmakuConfig.typeface = Typeface.MONOSPACE
                }
                true
            }
        }
        baseBinding.btnStyle.setOnClickListener {
            showPopupMenu(R.menu.style, it) { item ->
                when(item.itemId) {
                    R.id.defaule -> baseBinding.dv.danmakuConfig.drawMode = DanmakuConfig.DrawMode.DEFAULT
                    R.id.shadow -> baseBinding.dv.danmakuConfig.drawMode = DanmakuConfig.DrawMode.SHADOW
                    R.id.stroke -> baseBinding.dv.danmakuConfig.drawMode = DanmakuConfig.DrawMode.STROKE
                    R.id.stroke_shadow -> baseBinding.dv.danmakuConfig.drawMode = DanmakuConfig.DrawMode.SHADOW_STROKE
                }
                true
            }
        }
        baseBinding.btnParser.setOnClickListener {
            showPopupMenu(R.menu.parser, it) { item ->
                when (item.itemId) {
                    R.id.inb -> parserXMLDanmaku(resources.openRawResource(R.raw.danmaku))
                    R.id.file -> openXmlFile.launch(arrayOf("text/xml"))
                    R.id.special -> baseBinding.dv.parse(SpecialDanmakuTestParser)
                    R.id.empty -> baseBinding.dv.parse(DanmakuParser.EMPTY)
                    R.id.later_10s -> baseBinding.dv.parse {
                        (1..10).forEach {
                            delay(1000)
                            Log.d("later10sp", "$it")
                        }
                        mutableMapOf(DanmakuView.POOL_UNDEFINED to Danmakus().apply {
                            add(TopDanmaku().apply {
                                offset = 0
                                duration = 2000
                                text = "Danmaku"
                            })
                        })
                    }
                    R.id._200k_danmaku -> {
                        baseBinding.dv.debugPaint.color = Color.RED
                        baseBinding.dv.parse {
                            val danmakus = mutableMapOf<Int, Danmakus>()
                            (1..200_000).forEach {
                                danmakus.getOrNew(1).add(R2LDanmaku().apply {
                                    offset = 1L + it
                                    duration = 2000
                                    text = "Danmaku"
                                })
                            }
                            return@parse danmakus
                        }
                    }
                }
                true
            }
        }
        baseBinding.btnBackground.setOnClickListener {
            showPopupMenu(R.menu.background, it) { item ->
                when(item.itemId) {
                    R.id.color -> {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("设置背景颜色")
                            .setView(EditText(this).apply {
                                addTextChangedListener(object : TextWatcher {
                                    override fun afterTextChanged(s: Editable?) {
                                        runCatching {
                                            baseBinding.root.setBackgroundColor(s.toString().toColorInt())
                                        }
                                    }

                                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                    }

                                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    }
                                })
                            })
                            .setPositiveButton("确定") { _, _ -> }
                            .show()
                    }
                    R.id.file -> {
                        openBackgroundImage.launch(arrayOf("image/*"))
                    }
                    R.id.transparent -> baseBinding.root.setBackgroundColor(Color.TRANSPARENT)
                }
                true
            }
        }

        supportActionBar?.hide()

        baseBinding.dv.drawDebugInfo = true
        baseBinding.dv.zOnTop = true
        baseBinding.dv.onDanmakuClickListener = { danmaku ->
            Log.d("MainActivity", "$danmaku")
        }
        parserXMLDanmaku(resources.openRawResource(R.raw.danmaku))

        // 1000ms / 120fps = 8.33ms
        baseBinding.dv.period = 8
    }

    override fun onStart() {
        super.onStart()
        fullScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        baseBinding.dv.destroy()
    }

    private fun fullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun showPopupMenu(
        @MenuRes menu: Int, view: View,
        onItemClickListener: (item: MenuItem) -> Boolean
    ) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(onItemClickListener)
        popupMenu.setOnDismissListener { fullScreen() }
        popupMenu.show()
    }

    private fun parserXMLDanmaku(inputStream: InputStream) {
        baseBinding.dv.parse(XMLDanmakuParser(inputStream)) { inputStream.close() }
    }
}