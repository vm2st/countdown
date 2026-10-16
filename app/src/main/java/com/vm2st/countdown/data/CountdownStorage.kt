package com.vm2st.countdown.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CountdownStorage(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _items = MutableStateFlow(readAll(prefs.getString(KEY_ITEMS, "[]") ?: "[]"))
    val items: StateFlow<List<Countdown>> = _items
    private val _theme = MutableStateFlow(runCatching { AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.WINE.name)!!) }.getOrDefault(AppTheme.WINE))
    val theme: StateFlow<AppTheme> = _theme

    fun add(item: Countdown) { _items.value = _items.value + item; persist() }
    fun delete(id: String) { _items.value = _items.value.filterNot { it.id == id }; persist() }
    fun setTheme(value: AppTheme) { _theme.value = value; prefs.edit().putString(KEY_THEME, value.name).apply() }
    fun find(id: String): Countdown? = _items.value.firstOrNull { it.id == id }
    private fun persist() = prefs.edit().putString(KEY_ITEMS, encode(_items.value).toString()).apply()

    companion object {
        private const val PREFS = "countdown_store"; private const val KEY_ITEMS = "items"; private const val KEY_THEME = "theme"
        fun load(context: Context): List<Countdown> { val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE); return readAll(p.getString(KEY_ITEMS,"[]") ?: "[]") }
        private fun readAll(raw: String): List<Countdown> = runCatching {
            val a=JSONArray(raw); buildList { for(i in 0 until a.length()) { val o=a.getJSONObject(i); val storedColor=o.getLong("color"); val migratedColor=if(storedColor==0xFF7A1428L) 0xFF892929L else storedColor
                add(Countdown(o.getString("id"),o.getString("title"),o.getLong("target"),o.getInt("style").coerceIn(0,9),migratedColor,o.getLong("created"))) } }
        }.getOrDefault(emptyList())
        private fun encode(items: List<Countdown>) = JSONArray().apply { items.forEach { put(JSONObject().put("id",it.id).put("title",it.title).put("target",it.targetMillis).put("style",it.style).put("color",it.color).put("created",it.createdMillis)) } }
    }
}
