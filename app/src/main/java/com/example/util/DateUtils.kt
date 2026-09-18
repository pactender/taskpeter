package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun formatShortDate(timestamp: Long): String = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))

    fun formatIsoDate(timestamp: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))

    fun formatFullDate(timestamp: Long): String = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

    fun isToday(timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun calculateConsecutiveStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        val dayStarts = timestamps.map { getStartOfDay(it) }.distinct().sortedDescending()
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val yesterdayStart = todayStart - (24 * 60 * 60 * 1000L)

        // Check if the most recent activity is today or yesterday
        val mostRecent = dayStarts.firstOrNull() ?: return 0
        if (mostRecent < yesterdayStart) return 0

        var streak = 0
        var expectedDay = if (mostRecent == todayStart) todayStart else yesterdayStart
        for (day in dayStarts) {
            if (day == expectedDay) {
                streak++
                expectedDay -= (24 * 60 * 60 * 1000L)
            } else if (day < expectedDay) {
                break
            }
        }
        return streak
    }

    fun calculatePast7DaysFocus(sessionTimestampsAndMinutes: List<Pair<Long, Int>>): List<Pair<String, Int>> {
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val result = mutableListOf<Pair<String, Int>>()
        for (i in 6 downTo 0) {
            val targetCal = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val start = targetCal.timeInMillis
            val end = start + (24 * 60 * 60 * 1000L) - 1
            val dayName = dayOfWeekFormat.format(targetCal.time)
            val minutes = sessionTimestampsAndMinutes
                .filter { it.first in start..end }
                .sumOf { it.second }
            result.add(dayName to minutes)
        }
        return result
    }

    data class DayContribution(
        val dateIso: String,
        val shortLabel: String,
        val timestamp: Long,
        val count: Int,
        val level: Int // 0 to 4
    )

    fun calculatePast28DaysMatrix(
        completedTimestamps: List<Long>
    ): List<DayContribution> {
        val days = mutableListOf<DayContribution>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Generate 28 days ending today
        for (i in 27 downTo 0) {
            val targetCal = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val start = targetCal.timeInMillis
            val end = start + (24 * 60 * 60 * 1000L) - 1

            val count = completedTimestamps.count { it in start..end }
            val level = when {
                count == 0 -> 0
                count <= 1 -> 1
                count <= 2 -> 2
                count <= 4 -> 3
                else -> 4
            }

            days.add(
                DayContribution(
                    dateIso = formatIsoDate(targetCal.timeInMillis),
                    shortLabel = formatShortDate(targetCal.timeInMillis),
                    timestamp = start,
                    count = count,
                    level = level
                )
            )
        }
        return days
    }

    data class DailyProductivityPoint(
        val dayName: String,
        val dateLabel: String,
        val timestamp: Long,
        val completedTasks: Int,
        val focusMinutes: Int,
        val commitsCount: Int = 0,
        val isToday: Boolean = false
    )

    fun calculateWeeklyProductivity(
        completedTaskTimestamps: List<Long>,
        sessionTimestampsAndMinutes: List<Pair<Long, Int>>,
        commitTimestamps: List<Long> = emptyList()
    ): List<DailyProductivityPoint> {
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val todayStart = cal.timeInMillis
        val points = mutableListOf<DailyProductivityPoint>()

        for (i in 6 downTo 0) {
            val targetCal = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val start = targetCal.timeInMillis
            val end = start + (24 * 60 * 60 * 1000L) - 1

            val tasks = completedTaskTimestamps.count { it in start..end }
            val focus = sessionTimestampsAndMinutes
                .filter { it.first in start..end }
                .sumOf { it.second }
            val commits = commitTimestamps.count { it in start..end }

            points.add(
                DailyProductivityPoint(
                    dayName = dayOfWeekFormat.format(targetCal.time),
                    dateLabel = formatShortDate(targetCal.timeInMillis),
                    timestamp = start,
                    completedTasks = tasks,
                    focusMinutes = focus,
                    commitsCount = commits,
                    isToday = start == todayStart
                )
            )
        }
        return points
    }
}
