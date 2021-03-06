package com.brentpanther.bitcoinwidget

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.google.gson.Gson
import com.google.gson.JsonObject


internal class Prefs(val widgetId: Int) {
    private val context: Context = WidgetApplication.instance

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(context.getString(R.string.key_prefs), Context.MODE_PRIVATE)

    val coin: Coin
        get() = getValue(COIN)?.let { Coin.valueOf(it)} ?: Coin.BTC

    val exchangeCoinName: String
        get() = getValue(COIN_CUSTOM) ?: this.coin.name

    val currency: String?
        get() = getValue(CURRENCY)

    val exchangeCurrencyName: String?
        get() = getValue(CURRENCY_CUSTOM) ?: getValue(CURRENCY)

    val interval: Int
        get() = getValue(REFRESH)?.toInt() ?: 30

    val exchange: Exchange
        get() {
            return try {
                Exchange.valueOf(getValue(EXCHANGE) ?: return Exchange.values()[0])
            } catch (e: IllegalArgumentException) {
                Exchange.values()[0]
            }
        }

    val exchangeName: String?
        get() = getValue(EXCHANGE)

    val themeLayout: Int
        get() {
            val value = getValue(THEME) ?: return R.layout.widget_layout
            return when (value) {
                "Dark" -> R.layout.widget_layout_dark
                "Transparent Dark" -> R.layout.widget_layout_transparent_dark
                "DayNight" -> R.layout.widget_layout_auto
                "Transparent DayNight" -> R.layout.widget_layout_transparent_auto
                "Transparent" -> R.layout.widget_layout_transparent
                else -> R.layout.widget_layout
            }
        }

    val isTransparent: Boolean
        get() {
            val value = getValue(THEME)
            return "Transparent" == value || "Transparent Dark" == value || "Transparent DayNight" == value
        }

    val unit: String?
        get() = getValue(UNITS)

    val lastUpdate: Long
        get() = getValue(LAST_UPDATE)?.toLong() ?: 0

    var lastValue: String?
        get() = getValue(LAST_VALUE)
        set(value) = setValue(LAST_VALUE, value!!)

    val label: Boolean
        get() = getValue(SHOW_LABEL)?.toBoolean() ?: false

    val showDecimals: Boolean
        get() = getValue(SHOW_DECIMALS)?.toBoolean() ?: true

    fun isLightTheme(context: Context): Boolean {
        val themeLayout = themeLayout
        if (themeLayout == R.layout.widget_layout_auto || themeLayout == R.layout.widget_layout_transparent_auto) {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_NO
        }
        return themeLayout == R.layout.widget_layout || themeLayout == R.layout.widget_layout_transparent
    }

    fun setLastUpdate() {
        setValue(LAST_UPDATE, "" + System.currentTimeMillis())
    }

    fun showIcon(): Boolean {
        return getValue(HIDE_ICON)?.toBoolean()?.not() ?: true
    }

    fun setValue(key: String, value: String?) {
        val string = prefs.getString("" + widgetId, null)
        val obj = string?.let {
            Gson().fromJson(it, JsonObject::class.java)
        } ?: JsonObject()
        obj.addProperty(key, value)
        prefs.edit().putString("" + widgetId, obj.toString()).apply()
    }

    fun setValues(coin: String, currency: String, refreshValue: Int, exchange: String, checked: Boolean,
                  theme: String, iconChecked: Boolean, showDecimals: Boolean, unit: String?) {
        val obj = JsonObject()
        with(obj) {
            addProperty(COIN, coin)
            addProperty(CURRENCY, currency)
            addProperty(REFRESH, "" + refreshValue)
            addProperty(EXCHANGE, exchange)
            addProperty(SHOW_LABEL, "" + checked)
            addProperty(THEME, theme)
            addProperty(HIDE_ICON, "" + !iconChecked)
            addProperty(SHOW_DECIMALS, "" + showDecimals)
            addProperty(UNITS, unit)
        }
        prefs.edit().putString("" + widgetId, obj.toString()).apply()
    }

    fun delete() {
        prefs.edit().remove("" + widgetId).apply()
    }

    fun getValue(key: String): String? {
        val string = prefs.getString("" + widgetId, null) ?: return null
        val obj = Gson().fromJson(string, JsonObject::class.java)
        if (!obj.has(key)) return null
        val el = obj.get(key)
        return if (el.isJsonNull) null else el.asString
    }

    fun setTextSize(size: Float, portrait: Boolean) {
        setValue(if (portrait) PORTRAIT_TEXT_SIZE else LANDSCAPE_TEXT_SIZE, size.toString())
    }

    fun getTextSize(portrait: Boolean): Float {
        val size = getValue(if (portrait) PORTRAIT_TEXT_SIZE else LANDSCAPE_TEXT_SIZE)?.toFloat() ?: Float.MAX_VALUE
        return if (size > 0) size else Float.MAX_VALUE
    }

    fun clearTextSize() {
        setTextSize(0f, true)
        setTextSize(0f, false)
    }

    fun setExchangeValues(exchangeCoinName: String?, exchangeCurrencyName: String?) {
        if (exchangeCoinName != null) setValue(COIN_CUSTOM, exchangeCoinName)
        if (exchangeCurrencyName != null) setValue(CURRENCY_CUSTOM, exchangeCurrencyName)
    }

    fun setTemporary(temporary: Boolean) {
        setValue(TEMPORARY, if (temporary) "true" else null)
    }

    fun deleteIfTemporary() {
        if (getValue(TEMPORARY) != null) {
            delete()
        }
    }

    companion object {

        private const val LAST_UPDATE = "last_update"
        private const val CURRENCY = "currency"
        private const val CURRENCY_CUSTOM = "currency_custom"
        private const val REFRESH = "refresh"
        private const val EXCHANGE = "exchange"
        private const val SHOW_LABEL = "show_label"
        private const val THEME = "theme"
        private const val HIDE_ICON = "icon"
        private const val SHOW_DECIMALS = "show_decimals"
        private const val LAST_VALUE = "last_value"
        private const val UNITS = "units"
        private const val COIN = "coin"
        private const val COIN_CUSTOM = "coin_custom"
        private const val PORTRAIT_TEXT_SIZE = "portrait_text_size"
        private const val LANDSCAPE_TEXT_SIZE = "landscape_text_size"
        private const val TEMPORARY = "temp"
    }
}
