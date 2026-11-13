package app.nayom.data

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.time.*

class VaultStoreTest {
    private val z = ZoneId.of("Asia/Kolkata")
    private var now = ZonedDateTime.of(2026, 11, 12, 10, 0, 0, 0, z)
    private fun store() = VaultStore(Files.createTempDirectory("v").toFile(), "ph", z) { now }

    @Test fun promiseRoundTripAndStatus() {
        val s = store(); val p = s.addPromise("Meena", "Send the final file", now.plusHours(7))
        assertEquals(Status.Open, s.promises().single().status)
        now = now.plusMinutes(5); s.setStatus(p.id, Status.Kept)
        assertEquals(Status.Kept, s.promises().single().status)
        assertEquals(1, s.today().kept)
    }
    @Test fun movedUpdatesDue() {
        val s = store(); val p = s.addPromise("Ravi", "Tweak", now.plusHours(1))
        now = now.plusMinutes(1); s.setStatus(p.id, Status.Moved, now.plusDays(1))
        assertEquals(now.plusDays(1).toInstant(), s.promises().single().due.toInstant())
    }
    @Test fun sweepMarksOverdueOnce() {
        val s = store(); s.addPromise("A", "x", now.plusMinutes(10))
        now = now.plusHours(4)
        assertEquals(1, s.sweepMissed()); assertEquals(0, s.sweepMissed())
        assertEquals(Status.Missed, s.promises().single().status)
    }
    @Test fun askDecisionAndReflexYes() {
        val s = store(); val a = s.addAsk("Ravi", "Banner tweak")
        now = now.plusSeconds(2); s.decide(a.id, Decision.Yes)
        assertEquals(Decision.Yes, s.asks().single().decision); assertEquals(1, s.today().reflexYes)
    }
    @Test fun peopleCountOpen() {
        val s = store(); s.addPromise("Meena", "a", now.plusHours(2)); s.addPromise("Meena", "b", now.plusHours(3))
        assertEquals("Meena" to 2, s.people().single())
    }
    @Test fun notesIgnoreNayomFolder() {
        val s = store(); s.addNote("Plan", "hi"); s.addPromise("A", "x", now.plusHours(1))
        assertEquals(listOf("plan"), s.notes().map { it.name })
    }

    @Test fun activityLogsEverything() {
        val st = store()
        st.addNote("Plan", "buy milk")
        val p = st.addPromise("Mira", "call", java.time.ZonedDateTime.now().plusHours(2))
        st.setStatus(p.id, Status.Kept)
        st.inbox("whatsapp", "Mira", "hello")
        val kinds = st.activity().map { it.kind }
        assertTrue(kinds.containsAll(listOf("note", "promise-add", "promise-kept")))
        assertTrue(java.io.File(st.root, "Nayom/inbox/ph").listFiles()!!.isNotEmpty())
    }
}
