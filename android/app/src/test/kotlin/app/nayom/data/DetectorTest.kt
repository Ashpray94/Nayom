package app.nayom.data

import org.junit.Assert.*
import org.junit.Test

class DetectorTest {
    @Test fun catchesPromises() {
        assertEquals("promise", Detector.promise("I'll send it by 5pm")?.kind)
        assertEquals("promise", Detector.promise("haan kal tak kar dunga")?.kind)
        assertEquals("reflex", Detector.promise("ok")?.kind)
        assertNull(Detector.promise("how are you"))
        val slack = "Only a couple of them were open akka. But those were for profile page and not this feature. So if you see any copy comment open let me update tonight. A bit stuck rn. Give me an hour or so. Will check and update."
        assertEquals("promise", Detector.promise(slack)?.kind)
        assertEquals("promise", Detector.promise("let me update tonight")?.kind)
        assertEquals("promise", Detector.promise("Give me an hour")?.kind)
    }
}
