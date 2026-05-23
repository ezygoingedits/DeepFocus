package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private fun getDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getCurrentDateString(): String {
        return getDateFormat().format(Date())
    }

    fun addDaysToDateString(dateString: String, days: Int): String {
        return try {
            val date = getDateFormat().parse(dateString) ?: return dateString
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            getDateFormat().format(cal.time)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return getDateFormat().format(cal.time)
    }

    fun getFormattedDate(dateString: String): String {
        return try {
            val date = getDateFormat().parse(dateString) ?: return dateString
            val targetFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)
            targetFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getShortMonthAndDay(dateString: String): String {
        return try {
            val date = getDateFormat().parse(dateString) ?: return dateString
            val targetFormat = SimpleDateFormat("MMM d", Locale.US)
            targetFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getDayOfWeekLetter(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offsetDays)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "S"
            Calendar.MONDAY -> "M"
            Calendar.TUESDAY -> "T"
            Calendar.WEDNESDAY -> "W"
            Calendar.THURSDAY -> "T"
            Calendar.FRIDAY -> "F"
            Calendar.SATURDAY -> "S"
            else -> ""
        }
    }

    fun getDateStringWithOffset(offsetDays: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offsetDays)
        return getDateFormat().format(cal.time)
    }

    fun getCurrentWeekDates(): List<String> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val dates = mutableListOf<String>()
        for (i in 0 until 7) {
            dates.add(getDateFormat().format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dates
    }

    fun getDayOfWeekAbbreviation(dateString: String): String {
        return try {
            val date = getDateFormat().parse(dateString) ?: return ""
            val targetFormat = SimpleDateFormat("EEE", Locale.US)
            targetFormat.format(date).uppercase()
        } catch (e: Exception) {
            ""
        }
    }
}
