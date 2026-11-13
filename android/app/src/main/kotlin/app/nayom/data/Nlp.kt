package app.nayom.data

import java.time.DayOfWeek
import java.time.ZonedDateTime

/** Reads a promise written in plain words and finds the task, the person and the time. On-device, no network. */
object Nlp {
    data class Parsed(val what: String, val who: String?, val due: ZonedDateTime?, val timeText: String?)

    private val words = mapOf("a" to 1, "an" to 1, "one" to 1, "two" to 2, "couple" to 2, "three" to 3, "few" to 3, "four" to 4, "five" to 5, "ten" to 10)
    private val days = mapOf("monday" to DayOfWeek.MONDAY, "tuesday" to DayOfWeek.TUESDAY, "wednesday" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY, "friday" to DayOfWeek.FRIDAY, "saturday" to DayOfWeek.SATURDAY, "sunday" to DayOfWeek.SUNDAY)
    private val stop = setOf("the", "a", "an", "my", "me", "you", "your", "him", "her", "them", "us", "it", "this", "that", "do", "be", "get", "see", "check",
        "send", "finish", "today", "tomorrow", "tonight", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "morning", "evening",
        "afternoon", "night", "now", "then", "later", "soon", "work", "home", "office", "someone", "everyone", "time", "free", "sure", "okay", "ok", "kal", "aaj", "next", "end")

    private val rel = Regex("""(?i)\b(?:in|within|after|give me)\s+(a couple of|an?|one|two|three|few|four|five|ten|\d{1,3})\s*(minutes?|mins?|hours?|hrs?|days?|weeks?)\b(?:\s+or so)?""")
    private val dayWord = Regex("""(?i)\b(day after tomorrow|parso|tomorrow|tmrw|tommorow|kal|today|aaj|tonight|next week|this weekend|monday|tuesday|wednesday|thursday|friday|saturday|sunday|eod|end of day)\b""")
    private val clock = Regex("""(?i)(?:\b(?:at|by|before|around|till|until)\s+|@\s*)?\b(\d{1,2})(?::(\d{2}))?\s*(am|pm|a\.m\.|p\.m\.)?(?![\d:])""")
    private val period = Regex("""(?i)\b(this morning|morning|subah|noon|afternoon|this afternoon|evening|this evening|shaam|night|raat|tonight)\b""")
    private val lead = Regex("""(?i)^\s*(?:please\s+)?(?:remind me to|promise(?: to)?|i promise(?: to)?|i['’]?ll|i will|i can|will|let me|i['’]?m going to|i have to|i need to|i must|i should|kar dunga|kar dungi)\s+""")

    fun parse(input: String, now: ZonedDateTime = ZonedDateTime.now()): Parsed {
        var text = input.trim().replace(Regex("\\s+"), " ")
        val cut = mutableListOf<IntRange>()

        // relative: "in 2 hours", "give me an hour"
        var due: ZonedDateTime? = null; var timeText: String? = null
        rel.find(text)?.let { m ->
            val n = m.groupValues[1].lowercase().let { words[it.replace("a couple of", "couple")] ?: it.toIntOrNull() ?: 1 }
            val u = m.groupValues[2].lowercase()
            due = when { u.startsWith("min") -> now.plusMinutes(n.toLong()); u.startsWith("h") -> now.plusHours(n.toLong()); u.startsWith("d") -> now.plusDays(n.toLong()); else -> now.plusWeeks(n.toLong()) }
            timeText = m.value.trim(); cut += m.range
        }

        if (due == null) {
            var base: ZonedDateTime? = null; var named = false; var eod = false; var tonight = false
            dayWord.find(text)?.let { m ->
                val w = m.value.lowercase(); named = true; cut += m.range; timeText = m.value
                when {
                    w == "tomorrow" || w == "tmrw" || w == "tommorow" || w == "kal" -> base = now.plusDays(1)
                    w == "day after tomorrow" || w == "parso" -> base = now.plusDays(2)
                    w == "today" || w == "aaj" -> base = now
                    w == "tonight" -> { base = now; tonight = true }
                    w == "next week" -> base = now.plusWeeks(1).with(DayOfWeek.MONDAY)
                    w == "this weekend" -> base = now.with(DayOfWeek.SATURDAY).let { if (it.isBefore(now)) it.plusWeeks(1) else it }
                    w == "eod" || w == "end of day" -> { base = now; eod = true }
                    else -> { val d = days[w]!!; var t = now.with(d); if (!t.toLocalDate().isAfter(now.toLocalDate())) t = t.plusWeeks(1); base = t }
                }
            }
            var hour: Int? = null; var min = 0
            // clock time: need am/pm, a colon, or a word like at/by in front, so "2 reports" is not read as 2 o'clock
            for (m in clock.findAll(text)) {
                val lead = m.value.trim().lowercase()
                val ok = m.groupValues[3].isNotEmpty() || m.groupValues[2].isNotEmpty() || Regex("^(at|by|before|around|till|until|@)").containsMatchIn(lead)
                if (!ok) continue
                var h = m.groupValues[1].toInt(); val mi = m.groupValues[2].ifEmpty { "0" }.toInt()
                if (h > 23 || mi > 59) continue
                val ap = m.groupValues[3].lowercase().firstOrNull()
                if (ap == 'p' && h < 12) h += 12; if (ap == 'a' && h == 12) h = 0
                if (ap == null && h in 1..7) h += 12   // "at 5" in daily talk means 5 pm
                hour = h; min = mi; cut += m.range; timeText = listOfNotNull(timeText, m.value.trim()).joinToString(" "); break
            }
            if (hour == null) period.find(text)?.let { m ->
                val w = m.value.lowercase(); cut += m.range; timeText = listOfNotNull(timeText, m.value).joinToString(" ")
                hour = when { "morning" in w || w == "subah" -> 9; w == "noon" -> 12; "afternoon" in w -> 15; "evening" in w || w == "shaam" -> 18; else -> 21 }
                if (w == "tonight" || "night" in w || w == "raat") { if (base == null) base = now }
            }
            if (hour == null && eod) { hour = 18 }
            if (hour == null && tonight) { hour = 21 }
            if (hour != null || named) {
                val h = hour ?: if (base != null && base!!.toLocalDate() == now.toLocalDate()) maxOf(now.hour + 3, 18).coerceAtMost(21) else 10
                var d = (base ?: now).withHour(h).withMinute(min).withSecond(0).withNano(0)
                if (base == null && d.isBefore(now)) d = d.plusDays(1)       // "at 9am" said after 9am means tomorrow
                due = d
            }
        }

        // person
        var who: String? = null
        val prep = Regex("""\b(?:to|for|with)\s+([A-Z][a-z]{1,14}(?:\s[A-Z][a-z]{1,14})?)\b""").find(text)
        val verb = Regex("""\b(?:call|email|message|text|ping|tell|ask|remind|meet|send)\s+([A-Z][a-z]{1,14})\b""").find(text)
        val at = Regex("""@([A-Za-z][A-Za-z0-9_]{1,20})""").find(text)
        val low = Regex("""(?i)\b(?:send|tell|call|ping|message|text|email|ask|remind|meet)\s+([a-z]{3,12})\b""").find(text)
        val lowTo = Regex("""(?i)\b(?:to|for|with)\s+([a-z]{3,12})(?=\s*$|\s+(?:by|at|in|before|tomorrow|today|tonight|kal|aaj|on)\b)""").find(text)
        fun ok(s: String) = s.lowercase() !in stop && s.lowercase() !in days
        when {
            prep != null && ok(prep.groupValues[1].split(" ")[0]) -> { who = prep.groupValues[1]; cut += prep.range }
            at != null -> { who = at.groupValues[1]; cut += at.range }
            verb != null && ok(verb.groupValues[1]) -> who = verb.groupValues[1]
            lowTo != null && ok(lowTo.groupValues[1]) -> { who = lowTo.groupValues[1].replaceFirstChar { it.uppercase() }; cut += lowTo.range }
            low != null && ok(low.groupValues[1]) -> who = low.groupValues[1].replaceFirstChar { it.uppercase() }
        }

        // task = text without the time and person pieces
        for (r in cut.sortedByDescending { it.first }) if (r.last < text.length) text = text.removeRange(r.first, r.last + 1)
        text = text.replace(Regex("(?i)\\b(?:by|before|at|on|around|till|until)\\s*$"), "").replace(Regex("\\s+"), " ").trim(' ', ',', '.', '-', '!')
        text = text.replace(lead, "").replace(Regex("(?i)\\b(?:by|before|at|on|around|till|until)\\s*([,.]|$)"), "$1").replace(Regex("\\s+"), " ").trim(' ', ',', '.', '-', '!')
        val what = text.ifEmpty { input.trim() }.replaceFirstChar { it.uppercase() }
        return Parsed(what, who, due, timeText?.trim())
    }
}
