package com.vm2st.countdown.data

data class Countdown(
    val id: String,
    val title: String,
    val targetMillis: Long,
    val style: Int,
    val color: Long,
    val createdMillis: Long
)
enum class AppTheme { LIGHT, DARK, WINE }
