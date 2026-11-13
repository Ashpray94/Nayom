package app.nayom.data

import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NlpTest {
    private val now = ZonedDateTime.of(2026, 11, 13, 10, 0, 0, 0, ZoneId.of("Asia/Kolkata"))
    @Test fun tomorrowWithTimeAndPerson() {
        val p = Nlp.parse("I will send the report to Rahul tomorrow at 5 pm", now)
        assertEquals("Rahul", p.who); assertEquals("Send the report", p.what)
        assertEquals(now.plusDays(1).withHour(17).withMinute(0), p.due)
    }
    @Test fun relative() {
        assertEquals(now.plusHours(2), Nlp.parse("call mom in 2 hours", now).due)
        assertEquals(now.plusHours(1), Nlp.parse("give me an hour", now).due)
    }
    @Test fun tonightAndByTime() {
        assertEquals(now.withHour(21), Nlp.parse("update the doc tonight", now).due)
        assertEquals(now.withHour(18), Nlp.parse("finish it by 6pm", now).due)
    }
    @Test fun noTimeNoDue() { assertNull(Nlp.parse("buy milk", now).due) }
    @Test fun numberIsNotClock() { assertNull(Nlp.parse("review 2 reports", now).due) }
}
