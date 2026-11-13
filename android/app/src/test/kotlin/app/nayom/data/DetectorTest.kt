package app.nayom.data

import org.junit.Assert.*
import org.junit.Test

class DetectorTest {
    @Test fun catchesPromises() {
        assertEquals("promise", Detector.promise("I'll send it by 5pm")?.kind)
        assertEquals("promise", Detector.promise("haan kal tak kar dunga")?.kind)
        assertEquals("reflex", Detector.promise("ok")?.kind)
        assertNull(Detector.promise("how are you"))
    }
}
