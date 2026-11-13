package app.nayom.data

/** Spots a commitment in something you are typing (English + Hinglish). Pure, on-device, no network. */
object Detector {
    data class Hit(val kind: String, val phrase: String, val time: String?)

    private val commit = Regex("""(?i)\b(i['’]?ll|i will|i can|will do|will send|will call|i['’]?m on it|sure|okay|ok|done deal|yes|yeah|haan|ha|theek hai|thik hai|kar dunga|kar dungi|kar deta|bhej dunga|bhej dungi|bhej dungi|aa jaunga|aa jaungi|aata hu|aati hu|pakka|promise|dekh lunga|dekh lungi|kal tak|ho jayega|ho jaega|let me|will (?:check|update|share|get back|look|fix|do|send|call|finish|review|try|ping|message|reply|revert)|give me|gimme|get back to you|i can do|i['’]?ll try|count on me)\b""")
    private val time = Regex("""(?i)\b(tomorrow|tmrw|tonight|today|kal|aaj|raat tak|shaam tak|subah|by \d{1,2}(:\d{2})?\s?(am|pm)?|at \d{1,2}(:\d{2})?\s?(am|pm)?|before \d{1,2}|in \d+ ?(min|mins|minutes|hour|hours|hr|hrs)|next week|an? (?:hour|hr)(?: or so)?|(?:one|1|2|two|a couple of|few) ?(?:hours?|hrs?)|few minutes|eod|end of day|this evening|this afternoon|later today|in a bit|shortly|asap|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b""")
    private val strong = Regex("""(?i)\b(i['’]?ll|i will|will do|will send|will call|kar dunga|kar dungi|bhej dunga|bhej dungi|aa jaunga|aa jaungi|pakka|promise|ho jayega|let me|will (?:check|update|share|get back|look|fix|send|call|finish|review|ping|reply|revert)|give me|get back to you)\b""")
    private val reflex = Regex("""(?i)^\s*(ok|okay|sure|yes|yeah|haan|ha|theek hai|thik hai)[.! ]*$""")

    /** Looks at each sentence on its own, so a long message with one promise in it is still caught. The last promise wins. */
    fun promise(text: String): Hit? {
        val whole = text.trim()
        if (whole.length < 2) return null
        if (reflex.matches(whole)) return Hit("reflex", whole, null)
        var found: Hit? = null
        for (sentence in whole.split(Regex("(?<=[.!?\\n])\\s+"))) {
            val t = sentence.trim()
            if (t.length < 2 || t.length > 280) continue
            val c = commit.find(t) ?: continue
            val tm = time.find(t)?.value
            if (strong.containsMatchIn(t) || tm != null) found = Hit("promise", c.value, tm)
        }
        return found
    }
}
