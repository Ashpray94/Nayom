package app.nayom.data

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import java.time.ZonedDateTime
import java.util.TimeZone

/** Puts every promise on your personal calendar and reads it back so Nayom knows how packed you are. */
class CalendarBridge(private val ctx: Context) {
    private fun can(p: String) = ctx.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED
    val allowed get() = can(Manifest.permission.READ_CALENDAR) && can(Manifest.permission.WRITE_CALENDAR)

    private fun calendarId(): Long? {
        if (!allowed) return null
        ctx.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY),
            "${CalendarContract.Calendars.VISIBLE}=1 AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL}>=700", null, null)?.use { c ->
            var first: Long? = null
            while (c.moveToNext()) { val id = c.getLong(0); if (first == null) first = id; if (c.getInt(1) == 1) return id }
            return first
        }
        return null
    }

    fun add(title: String, start: ZonedDateTime, minutes: Int = 30): Long? {
        val id = calendarId() ?: return null
        val v = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, id); put(CalendarContract.Events.TITLE, "Nayom: $title")
            put(CalendarContract.Events.DTSTART, start.toInstant().toEpochMilli())
            put(CalendarContract.Events.DTEND, start.plusMinutes(minutes.toLong()).toInstant().toEpochMilli())
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 0)
        }
        return ctx.contentResolver.insert(CalendarContract.Events.CONTENT_URI, v)?.lastPathSegment?.toLongOrNull()
    }

    data class Busy(val title: String, val start: ZonedDateTime, val end: ZonedDateTime)
    fun between(from: ZonedDateTime, to: ZonedDateTime): List<Busy> {
        if (!allowed) return emptyList()
        val b = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(b, from.toInstant().toEpochMilli()); android.content.ContentUris.appendId(b, to.toInstant().toEpochMilli())
        val out = mutableListOf<Busy>()
        ctx.contentResolver.query(b.build(), arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY), null, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { c ->
            while (c.moveToNext()) if (c.getInt(3) == 0) out += Busy(c.getString(0) ?: "Busy",
                java.time.Instant.ofEpochMilli(c.getLong(1)).atZone(from.zone), java.time.Instant.ofEpochMilli(c.getLong(2)).atZone(from.zone))
        }
        return out
    }

    /** Hours already booked in the next [h] hours. */
    fun busyHours(now: ZonedDateTime, h: Long = 12): Double =
        between(now, now.plusHours(h)).sumOf { java.time.Duration.between(maxOf(it.start, now), minOf(it.end, now.plusHours(h))).toMinutes().coerceAtLeast(0) } / 60.0

    fun clash(at: ZonedDateTime): Busy? = between(at.minusMinutes(30), at.plusMinutes(30)).firstOrNull()
}
