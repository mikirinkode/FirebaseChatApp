package com.mikirinkode.firebasechatapp.helper

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    const val DATE_ALARM_FORMAT = "yyyy-MM-dd" // 2022-08-12
    const val DATE_PICKER_FORMAT = "MMM dd, yyyy" // Aug 12, 2022
    const val DATE_DISPLAY_FORMAT = "EEEE, dd MMM yyyy" // Friday, 12 Aug 2022

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