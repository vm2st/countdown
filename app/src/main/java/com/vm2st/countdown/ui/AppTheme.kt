package com.vm2st.countdown.ui

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.vm2st.countdown.data.AppTheme

private val Burgundy = Color(0xFF892929)
private val Wine = darkColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF641C1C),
    onPrimaryContainer = Color(0xFFFFDAD7),
    secondary = Color(0xFFE7BDB9),
    background = Color(0xFF160E0E),
    surface = Color(0xFF211716),
    surfaceVariant = Color(0xFF534341)
)
private val Dark = darkColorScheme(primary=Color(0xFFD0BCFF),background=Color(0xFF111318),surface=Color(0xFF191C20))
private val Light = lightColorScheme(primary=Burgundy,onPrimary=Color.White,secondary=Color(0xFF775654),background=Color(0xFFFFF8F7),surface=Color(0xFFFFF8F7))

@Composable fun CountdownTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val scheme = when(theme) { AppTheme.LIGHT -> Light; AppTheme.DARK -> Dark; AppTheme.WINE -> Wine }
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = scheme.background.toArgb()
        window.navigationBarColor = scheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = theme == AppTheme.LIGHT
            isAppearanceLightNavigationBars = theme == AppTheme.LIGHT
        }
    }
    MaterialTheme(colorScheme=scheme, typography=Typography(), content=content)
}
