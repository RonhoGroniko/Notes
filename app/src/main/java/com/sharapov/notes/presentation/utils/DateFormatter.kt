package com.sharapov.notes.presentation.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object DateFormatter {

    private val millisInHour = TimeUnit.HOURS.toMillis(1)
    private val millisInDay = TimeUnit.DAYS.toMillis(1)
    private val formatter = SimpleDateFormat.getDateInstance(DateFormat.SHORT)

    fun formatCurrentDate(): String {
        return formatter.format(System.currentTimeMillis())
    }

    fun formatDateToString(timeStamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeStamp
        return when {
            diff < millisInHour -> "Just now"
            diff < millisInDay -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours h ago"
            }
            else -> {
                formatter.format(timeStamp)
            }
        }
    }
}