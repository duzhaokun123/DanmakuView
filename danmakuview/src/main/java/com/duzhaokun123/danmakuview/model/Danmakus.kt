package com.duzhaokun123.danmakuview.model

import com.duzhaokun123.danmakuview.danmaku.Danmaku

class Danmakus : Collection<Danmaku> {
    private var danmakuSet = mutableSetOf<Danmaku>()

    override val size
        get() = danmakuSet.size

    override fun contains(element: Danmaku) = danmakuSet.contains(element)

    override fun containsAll(elements: Collection<Danmaku>) = danmakuSet.containsAll(elements)

    override fun isEmpty() = danmakuSet.isEmpty()

    override fun iterator() = danmakuSet.iterator()

    val duration
        get() = last()?.offset ?: 0

    fun sub(start: Long, end: Long): Danmakus {
        val range = start..end
        val subDanamkus = Danmakus()
        forEach { danmaku ->
            if (danmaku.offset in range) {
                subDanamkus.add(danmaku)
            }
        }
        return subDanamkus
    }

    fun add(danmaku: Danmaku) = danmakuSet.add(danmaku)

    fun addAll(danamkus: Collection<Danmaku>) = danmakuSet.addAll(danamkus)

    fun remove(danmaku: Danmaku) = danmakuSet.remove(danmaku)

    fun removeAll() = danmakuSet.removeAll { true }

    fun last(): Danmaku? {
        var last: Danmaku? = null
        forEach { danmaku ->
            if (last == null) {
                last = danmaku
            }
            if (last!!.offset < danmaku.offset)  {
                last = danmaku
            }
        }
        return last
    }
}