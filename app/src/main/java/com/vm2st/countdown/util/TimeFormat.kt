package com.vm2st.countdown.util

import kotlin.math.max

data class Remaining(val days: Long, val hours: Long, val minutes: Long, val seconds: Long, val expired: Boolean)
fun remaining(target: Long, now: Long = System.currentTimeMillis()): Remaining {
    val expired = target <= now; var value = max(0L, target-now)/1000
    val d=value/86400; value%=86400; val h=value/3600; value%=3600; val m=value/60; val s=value%60
    return Remaining(d,h,m,s,expired)
}
fun compactRemaining(target: Long, now: Long = System.currentTimeMillis()): String {
    val r=remaining(target,now); if(r.expired) return "Событие наступило"
    return when { r.days>0 -> "${r.days} д  ${r.hours} ч  ${r.minutes} мин"; r.hours>0 -> "${r.hours} ч  ${r.minutes} мин"; else -> "${r.minutes} мин  ${r.seconds} сек" }
}
