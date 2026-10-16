package com.vm2st.countdown.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import com.vm2st.countdown.MainActivity
import com.vm2st.countdown.R
import com.vm2st.countdown.data.Countdown
import com.vm2st.countdown.data.CountdownStorage
import com.vm2st.countdown.util.compactRemaining
import java.text.DateFormat
import java.util.Date

private const val WIDGET_PREFS="widget_bindings"
fun bindWidget(context: Context, widgetId: Int, countdownId: String) = context.getSharedPreferences(WIDGET_PREFS,Context.MODE_PRIVATE).edit().putString(widgetId.toString(),countdownId).apply()
private fun boundId(context: Context, widgetId: Int)=context.getSharedPreferences(WIDGET_PREFS,Context.MODE_PRIVATE).getString(widgetId.toString(),null)
private fun unbindWidget(context: Context, widgetId: Int)=context.getSharedPreferences(WIDGET_PREFS,Context.MODE_PRIVATE).edit().remove(widgetId.toString()).apply()

object WidgetRenderer {
    fun views(context: Context, widgetId: Int, item: Countdown?): RemoteViews {
        val rv=RemoteViews(context.packageName,R.layout.widget_countdown)
        val open=PendingIntent.getActivity(context,widgetId,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        rv.setOnClickPendingIntent(R.id.widget_root,open)
        if(item==null){ rv.setTextViewText(R.id.widget_title,context.getString(R.string.app_name)); rv.setTextViewText(R.id.widget_time,context.getString(R.string.widget_tap)); rv.setTextViewText(R.id.widget_subtitle,context.getString(R.string.developer)); return rv }
        val style=item.style.coerceIn(0,9)
        val backgrounds=intArrayOf(R.drawable.widget_bg_1,R.drawable.widget_bg_2,R.drawable.widget_bg_3,R.drawable.widget_bg_4,R.drawable.widget_bg_5,R.drawable.widget_bg_6,R.drawable.widget_bg_7,R.drawable.widget_bg_8)
        val palette=longArrayOf(0xFF892929,0xFF9C2746,0xFF3B2D51,0xFF163B52,0xFF145A4A,0xFF5A3A18,0xFF25262B,0xFF6A356A)
        rv.setInt(R.id.widget_root,"setBackgroundResource",backgrounds[palette.indexOf(item.color).coerceAtLeast(0)])
        rv.setTextViewText(R.id.widget_title,item.title)
        rv.setTextViewText(R.id.widget_time,compactRemaining(item.targetMillis))
        rv.setTextViewText(R.id.widget_subtitle,DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(item.targetMillis)))
        rv.setTextViewTextSize(R.id.widget_time,TypedValue.COMPLEX_UNIT_SP,floatArrayOf(20f,24f,26f,22f,28f,30f,27f,32f,34f,38f)[style])
        return rv
    }
    fun update(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val item=boundId(context,widgetId)?.let { id->CountdownStorage.load(context).firstOrNull{it.id==id} }
        manager.updateAppWidget(widgetId,views(context,widgetId,item))
    }
}

abstract class BaseCountdownProvider: AppWidgetProvider(){
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray)=ids.forEach{WidgetRenderer.update(context,manager,it)}
    override fun onDeleted(context: Context, ids: IntArray)=ids.forEach{unbindWidget(context,it)}
    override fun onReceive(context: Context,intent: Intent){ super.onReceive(context,intent); if(intent.action in setOf(Intent.ACTION_TIME_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_DATE_CHANGED,Intent.ACTION_BOOT_COMPLETED)){ val m=AppWidgetManager.getInstance(context); onUpdate(context,m,m.getAppWidgetIds(ComponentName(context,javaClass))) } }
}
class Style01Provider:BaseCountdownProvider(); class Style02Provider:BaseCountdownProvider(); class Style03Provider:BaseCountdownProvider(); class Style04Provider:BaseCountdownProvider(); class Style05Provider:BaseCountdownProvider(); class Style06Provider:BaseCountdownProvider(); class Style07Provider:BaseCountdownProvider(); class Style08Provider:BaseCountdownProvider(); class Style09Provider:BaseCountdownProvider(); class Style10Provider:BaseCountdownProvider()

class PinWidgetReceiver:BroadcastReceiver(){ override fun onReceive(context: Context,intent: Intent){ val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID); val countdown=intent.getStringExtra("countdown_id") ?: return; if(id!=AppWidgetManager.INVALID_APPWIDGET_ID){ bindWidget(context,id,countdown); WidgetRenderer.update(context,AppWidgetManager.getInstance(context),id) } } }

object WidgetPinning {
    const val OPTION_COUNTDOWN_ID = "com.vm2st.countdown.COUNTDOWN_ID"
    private val providers=arrayOf(Style01Provider::class.java,Style02Provider::class.java,Style03Provider::class.java,Style04Provider::class.java,Style05Provider::class.java,Style06Provider::class.java,Style07Provider::class.java,Style08Provider::class.java,Style09Provider::class.java,Style10Provider::class.java)

    fun request(context: Context,item: Countdown): Boolean {
        if(Build.VERSION.SDK_INT<26) return false
        val manager=AppWidgetManager.getInstance(context); val provider=ComponentName(context,providers[item.style.coerceIn(0,9)])
        if(!manager.isRequestPinAppWidgetSupported) return false
        val callbackIntent=Intent(context,PinWidgetReceiver::class.java).putExtra("countdown_id",item.id)
        val flags=PendingIntent.FLAG_UPDATE_CURRENT or if(Build.VERSION.SDK_INT>=31) PendingIntent.FLAG_MUTABLE else 0
        val callback=PendingIntent.getBroadcast(context,item.id.hashCode(),callbackIntent,flags)
        val options=Bundle().apply { putString(OPTION_COUNTDOWN_ID,item.id) }
        return manager.requestPinAppWidget(provider,options,callback)
    }

    fun refreshAll(context: Context) {
        val manager=AppWidgetManager.getInstance(context)
        providers.forEach { providerClass -> manager.getAppWidgetIds(ComponentName(context,providerClass)).forEach { WidgetRenderer.update(context,manager,it) } }
    }
}
