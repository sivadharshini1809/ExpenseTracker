package com.example.expensetracker.util

import android.content.Context
import java.util.Calendar

object Prefs {
    fun getBudget(c: Context): Float =
        c.getSharedPreferences("app", Context.MODE_PRIVATE).getFloat("budget", 0f)

    fun setBudget(c: Context, v: Float) {
        c.getSharedPreferences("app", Context.MODE_PRIVATE).edit().putFloat("budget", v).apply()
    }
}

/** Start and end (millis) of the current month */
fun monthRange(): Pair<Long, Long> {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val start = c.timeInMillis
    c.add(Calendar.MONTH, 1)
    return Pair(start, c.timeInMillis - 1)
}
