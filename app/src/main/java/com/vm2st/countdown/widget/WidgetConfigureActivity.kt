package com.vm2st.countdown.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vm2st.countdown.MainActivity
import com.vm2st.countdown.data.CountdownStorage
import com.vm2st.countdown.ui.CountdownTheme
import com.vm2st.countdown.util.compactRemaining

/** Configuration screen for manual widget placement and older Android launchers. */
class WidgetConfigureActivity : ComponentActivity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val manager = AppWidgetManager.getInstance(this)
        val optionsFromIntent = intent?.getBundleExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS)
        val preselectedId = optionsFromIntent?.getString(WidgetPinning.OPTION_COUNTDOWN_ID)
            ?: manager.getAppWidgetOptions(widgetId)
                .getString(WidgetPinning.OPTION_COUNTDOWN_ID)

        if (preselectedId != null) {
            completeConfiguration(preselectedId)
            return
        }

        val storage = CountdownStorage(this)
        setContent {
            val theme by storage.theme.collectAsStateWithLifecycle()
            val counters by storage.items.collectAsStateWithLifecycle()

            CountdownTheme(theme) {
                Scaffold(
                    topBar = {
                        Surface(shadowElevation = 3.dp) {
                            Text(
                                text = "Выберите отсчёт",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.fillMaxWidth().padding(20.dp)
                            )
                        }
                    }
                ) { padding ->
                    if (counters.isEmpty()) {
                        EmptyState(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            onOpenApp = {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(counters, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { completeConfiguration(item.id) }
                                ) {
                                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            compactRemaining(item.targetMillis),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun completeConfiguration(countdownId: String) {
        val item = CountdownStorage.load(this).firstOrNull { it.id == countdownId }
        if (item == null) {
            finish()
            return
        }

        bindWidget(this, widgetId, item.id)
        WidgetRenderer.update(this, AppWidgetManager.getInstance(this), widgetId)
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        )
        finish()
    }

    @androidx.compose.runtime.Composable
    private fun EmptyState(modifier: Modifier, onOpenApp: () -> Unit) {
        Column(
            modifier = modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text("Сначала создайте отсчёт")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onOpenApp) {
                Text("Открыть приложение")
            }
        }
    }
}
