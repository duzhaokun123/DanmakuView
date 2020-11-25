package com.duzhaokun123.danmakuview.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import com.duzhaokun123.danmakuview.danmaku.R2LDanmaku
import com.duzhaokun123.danmakuview.interfaces.DanmakuParser
import com.duzhaokun123.danmakuview.sample.databinding.ActivityMainBinding
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_OPEN_XML_DANMAKU = 1
    }

    private lateinit var baseBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
        baseBinding.btnDrawOnce.setOnClickListener { baseBinding.dv.drawOnce() }
        baseBinding.sbSpeed.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            baseBinding.dv.speed = (value - 200) / 100F
        })
        baseBinding.sbDurationCoeff.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            if (value != 0) {
                val durationCoeff = value / 100F
                baseBinding.dv.danmakuConfig.durationCoeff = durationCoeff
                baseBinding.tvDurationCoeff.text = durationCoeff.toString()
            }
        })
        baseBinding.sbTextSizeCoeff.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            if (value != 0) {
                val textSizeCoeff = value / 100F
                baseBinding.dv.danmakuConfig.textSizeCoeff = textSizeCoeff
                baseBinding.tvTextSizeCoeff.text = textSizeCoeff.toString()
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
                when (item.itemId) {
                    R.id.a -> baseBinding.dv.danmakuConfig.typeface = Typeface.DEFAULT
                    R.id.b -> baseBinding.dv.danmakuConfig.typeface = Typeface.DEFAULT_BOLD
                    R.id.c -> baseBinding.dv.danmakuConfig.typeface = Typeface.SANS_SERIF
                    R.id.d -> baseBinding.dv.danmakuConfig.typeface = Typeface.SERIF
                    R.id.e -> baseBinding.dv.danmakuConfig.typeface = Typeface.MONOSPACE
                }
                true
            }
        }
        baseBinding.btnParser.setOnClickListener {
            showPopupMenu(R.menu.parser, it) { item ->
                when (item.itemId) {
                    R.id.inb -> parserXMLDanmaku(resources.openRawResource(R.raw.danmaku))
                    R.id.file ->
                        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/xml"
                        }, REQUEST_OPEN_XML_DANMAKU)
                    R.id.empty -> baseBinding.dv.parse(DanmakuParser.EMPTY)
                }
                true
            }
        }

        supportActionBar?.hide()

        baseBinding.dv.drawDebugInfo = true
        baseBinding.dv.zOnTop = true
        parserXMLDanmaku(resources.openRawResource(R.raw.danmaku))
    }

    override fun onStart() {
        super.onStart()
        fullScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        baseBinding.dv.destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK)
            parserXMLDanmaku(contentResolver.openInputStream(data!!.data!!)!!)

        super.onActivityResult(requestCode, resultCode, data)
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