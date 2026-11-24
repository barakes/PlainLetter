package io.github.barakes.plainletter.explain

import android.content.Context
import java.util.UUID

// A random id the app creates once and keeps. The server uses it only to count requests per day.
// It isn't the phone's real identifier and says nothing about the person.
// Uninstalling the app (or clearing its data) creates a new one.
object DeviceId {

    private const val PREFS = "device"
    private const val KEY = "id"

    @Synchronized
    fun get(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY, null)?.let { return it }
        val created = UUID.randomUUID().toString()
        prefs.edit().putString(KEY, created).apply()
        return created
    }
}
