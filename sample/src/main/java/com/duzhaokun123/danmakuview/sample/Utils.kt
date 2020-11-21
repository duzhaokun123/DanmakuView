package com.duzhaokun123.danmakuview.sample

import android.widget.SeekBar
import java.lang.Exception

fun String.toFloatOrDefault(default: Float = 0F): Float {
    return try {
        this.toFloat()
    } catch (e: Exception) {
        default
    }
}

fun String.toLongOrDefault(default: Long = 0L): Long {
    return try {
        this.toLong()
    } catch (e: Exception) {
        default
    }
}

class SimpleValueOnSeekBarChangeListener(
    private val onValueChange: (value: Int) -> Unit
) : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) onValueChange(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}