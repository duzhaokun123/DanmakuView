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
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_OPEN_XML_DANMAKU = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_send.setOnClickListener {
            dv.addDanmaku(R2LDanmaku().apply {
                offset = dv.conductedTime
                text = "danmaku"
                borderColor = Color.GREEN

            })
        }
        btn_pause.setOnClickListener { dv.pause() }
        btn_resume.setOnClickListener { dv.resume() }
        btn_start.setOnClickListener { dv.start() }
        btn_buildCache.setOnClickListener { dv.buildCache() }
        btn_cleanCache.setOnClickListener { dv.cleanCache() }
        btn_drawOnce.setOnClickListener { dv.drawOnce() }
        sb_speed.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            dv.speed = (value - 200) / 100F
        })
        sb_durationCoeff.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            if (value != 0) {
                val durationCoeff = value / 100F
                dv.danmakuConfig.durationCoeff = durationCoeff
                tv_durationCoeff.text = durationCoeff.toString()
            }
        })
        sb_textSizeCoeff.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            if (value != 0) {
                val textSizeCoeff = value / 100F
                dv.danmakuConfig.textSizeCoeff = textSizeCoeff
                tv_textSizeCoeff.text = textSizeCoeff.toString()
            }
        })
        sb_lineHeight.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            val lineHeight = value + 20
            dv.danmakuConfig.lineHeight = lineHeight
            tv_lineHeight.text = lineHeight.toString()
        })
        sb_marginTop.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            dv.danmakuConfig.marginTop = value
            tv_marginTop.text = value.toString()
        })
        sb_marginBottom.setOnSeekBarChangeListener(SimpleValueOnSeekBarChangeListener { value ->
            dv.danmakuConfig.marginBottom = value
            tv_marginBottom.text = value.toString()
        })
        sc_allowCovering.setOnCheckedChangeListener { _, isChecked ->
            dv.danmakuConfig.allowCovering = isChecked
        }
        btn_typeface.setOnClickListener {
            showPopupMenu(R.menu.typeface, it) { item ->
                when (item.itemId) {
                    R.id.a -> dv.danmakuConfig.typeface = Typeface.DEFAULT
                    R.id.b -> dv.danmakuConfig.typeface = Typeface.DEFAULT_BOLD
                    R.id.c -> dv.danmakuConfig.typeface = Typeface.SANS_SERIF
                    R.id.d -> dv.danmakuConfig.typeface = Typeface.SERIF
                    R.id.e -> dv.danmakuConfig.typeface = Typeface.MONOSPACE
                }
                true
            }
        }
        btn_parser.setOnClickListener {
            showPopupMenu(R.menu.parser, it) { item ->
                when (item.itemId) {
                    R.id.inb -> parserXMLDanmaku(resources.openRawResource(R.raw.danmaku))
                    R.id.file ->
                        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/xml"
                        }, REQUEST_OPEN_XML_DANMAKU)
                    R.id.empty -> dv.parse(DanmakuParser.EMPTY)
                }
                true
            }
        }

        supportActionBar?.hide()

        dv.drawDebugInfo = true
        parserXMLDanmaku(resources.openRawResource(R.raw.danmaku))
    }

    override fun onStart() {
        super.onStart()
        fullScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        dv.destroy()
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
        dv.parse(XMLDanmakuParser(inputStream)) { inputStream.close() }
    }
}