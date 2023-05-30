package com.mikirinkode.firebasechatapp.helper

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    const val DATE_ALARM_FORMAT = "yyyy-MM-dd" // 2022-08-12
    const val DATE_PICKER_FORMAT = "MMM dd, yyyy" // Aug 12, 2022
    const val DATE_DISPLAY_FORMAT = "EEEE, dd MMM yyyy" // Friday, 12 Aug 2022
    const val DATE_REGULAR_FORMAT = "dd MMMM yyyy" // 30 August 2023
    const val DATE_CHAT_HISTORY_FORMAT = "dd/MM/yyyy" // 30/August/2023
    const val TIME_MESSAGE_FORMAT = "hh:mm a" // 08:20 PM
    const val DATE_TIME_LAST_ONLINE_FORMAT = "dd MMMM yyyy hh:mm a"

    fun formatTimestampToDate(timestamp: Long): Date {
        val timestampObj = Timestamp(timestamp)
        return Date(timestampObj.time)
    }

    /**
     * @param timestamp
     * @return string date: 20 August 2015
     */
    fun regularFormat(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val dateFormat = SimpleDateFormat(DATE_REGULAR_FORMAT, Locale.getDefault())
        val date = Date(timestampObj.time)
        return dateFormat.format(date)
    }

    fun getDateFromTimestamp(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val date = Date(timestampObj.time)
        return dateFormat.format(date)
    }

    /**
     * @param Timestamp
     * @return time, example: 08:00 PM
     */
    fun getTimeFromTimestamp(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val dateFormat = SimpleDateFormat(TIME_MESSAGE_FORMAT, Locale.getDefault())
        val date = Date(timestampObj.time)
        return dateFormat.format(date)
    }

    /**
     * @param Timestamp
     * @return Day name: "Sunday" || "Monday" etc
     */
    fun getDayNameFromTimestamp(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val date = Date(timestampObj.time)
        return dateFormat.format(date)

    }

    /**
     * @param timestamp
     * @return date time string: 30 May 2014, 18:30
     */
    fun getRegularFormattedDateTimeFromTimestamp(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val date = Date(timestampObj.time)
        return dateFormat.format(date)
    }

    /**
     * @param Timestamp
     * @return Date: 30/August/2023
     */
    fun getFormattedDateFromTimestamp(timestamp: Long): String {
            val timestampObj = Timestamp(timestamp)
            val dateFormat = SimpleDateFormat(DATE_CHAT_HISTORY_FORMAT, Locale.getDefault())
            val date = Date(timestampObj.time)
            return dateFormat.format(date)

    }

    fun getFormattedLastOnline(timestamp: Long): String {
        val timestampObj = Timestamp(timestamp)
        val date = Date(timestampObj.time)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * @return Date Time: 30/01/2022 20:42:23
     */
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    /**
     * @return Date: 30
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }


    /**
     * @return Date: 30
     */
    fun getThisWeekStartDate(): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Set the calendar to the current date
        calendar.time = Calendar.getInstance().time

        // Get the start date of the week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDate = calendar.time

        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        return dateFormat.format(startDate)
    }


    /**
     * @return Date: 30
     */
    fun getThisWeekEndDate(): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Set the calendar to the current date
        calendar.time = Calendar.getInstance().time

        // Get the end date of the week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val endDate = calendar.time

        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val date = dateFormat.format(endDate)

        val thisMonthEndDate = getThisMonthEndDate()

        return if (date in "1"..thisMonthEndDate) {
            date
        } else {
            thisMonthEndDate
        }
    }

    /**
     * @return Date in String: 31 atau 30 atau 29 atau 38
     */
    fun getThisMonthEndDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        val monthEndDate = calendar.get(Calendar.DATE)

        return monthEndDate.toString()
    }

    fun isYesterdayDate(timestamp: Long): Boolean {
        val todayDate: Int = getCurrentDate().toInt()
        val timestampDate: Int = getDateFromTimestamp(timestamp).toInt()
        val thisMonthEndDate: Int = getThisMonthEndDate().toInt()

        return if (todayDate == 1 && timestampDate == thisMonthEndDate){
            true
        } else todayDate - timestampDate == 1
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