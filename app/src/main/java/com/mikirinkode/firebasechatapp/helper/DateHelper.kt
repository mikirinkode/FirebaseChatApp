package com.mikirinkode.firebasechatapp.helper

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    const val DATE_ALARM_FORMAT = "yyyy-MM-dd" // 2022-08-12
    const val DATE_PICKER_FORMAT = "MMM dd, yyyy" // Aug 12, 2022
    const val DATE_DISPLAY_FORMAT = "EEEE, dd MMM yyyy" // Friday, 12 Aug 2022
    const val DATE_REGULAR_FORMAT = "dd MMMM yyyy" // 30 August 203
    const val TIME_MESSAGE_FORMAT = "hh:mm a" // 08:20 PM

    fun formatTimestampToDate(timestamp: Long): Date {
        val timestampObj = Timestamp(timestamp)
        return Date(timestampObj.time)
    }

    fun regularFormat(date: Date): String {
        val dateFormat = SimpleDateFormat(DATE_REGULAR_FORMAT, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun regularFormat(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val dateFormat = SimpleDateFormat(DATE_REGULAR_FORMAT, Locale.getDefault())
        val date = Date(timestampObj.time)
        return dateFormat.format(date)
    }

    fun messageTimeFormat(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val dateFormat = SimpleDateFormat(TIME_MESSAGE_FORMAT, Locale.getDefault())
        val date = Date(timestampObj.time)
        return dateFormat.format(date)
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    fun getCurrentHour(): String {
        val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    fun getCurrentMinute(): String {
        val dateFormat = SimpleDateFormat("mm", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }
}