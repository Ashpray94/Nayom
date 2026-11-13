package app.nayom.data

/** Spots a commitment in something you are typing (English + Hinglish). Pure, on-device, no network. */
object Detector {
    data class Hit(val kind: String, val phrase: String, val time: String?)

    private val commit = Regex("""(?i)\b(i['’]?ll|i will|i can|will do|will send|will call|i['’]?m on it|sure|okay|ok|done deal|yes|yeah|haan|ha|theek hai|thik hai|kar dunga|kar dungi|kar deta|bhej dunga|bhej dungi|bhej dungi|aa jaunga|aa jaungi|aata hu|aati hu|pakka|promise|dekh lunga|dekh lungi|kal tak|ho jayega|ho jaega)\b""")
    private val time = Regex("""(?i)\b(tomorrow|tmrw|tonight|today|kal|aaj|raat tak|shaam tak|subah|by \d{1,2}(:\d{2})?\s?(am|pm)?|at \d{1,2}(:\d{2})?\s?(am|pm)?|before \d{1,2}|in \d+ ?(min|mins|minutes|hour|hours|hr|hrs)|next week|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b""")
    private val strong = Regex("""(?i)\b(i['’]?ll|i will|will do|will send|will call|kar dunga|kar dungi|bhej dunga|bhej dungi|aa jaunga|aa jaungi|pakka|promise|ho jayega)\b""")
    private val reflex = Regex("""(?i)^\s*(ok|okay|sure|yes|yeah|haan|ha|theek hai|thik hai)[.! ]*$""")

    fun promise(text: String): Hit? {
        val t = text.trim()
        if (t.length < 2 || t.length > 280) return null
        val c = commit.find(t) ?: return null
        val tm = time.find(t)?.value
        return when {
            reflex.matches(t) -> Hit("reflex", t, null)                 // a bare yes: the people-pleasing reflex
            strong.containsMatchIn(t) || tm != null -> Hit("promise", c.value, tm)
            else -> null
        }
    }
}
