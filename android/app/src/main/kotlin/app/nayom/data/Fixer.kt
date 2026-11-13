package app.nayom.data

import android.content.Context
import android.view.textservice.*
import java.util.Locale

/** Finds typos and sloppy writing in what you typed. Each mistake gets a number. */
class Fixer(private val ctx: Context, private val onDone: (List<String>) -> Unit) : SpellCheckerSession.SpellCheckerSessionListener {
    private var session: SpellCheckerSession? = null
    private var text = ""
    private var rules = listOf<String>()

    fun check(t: String) {
        text = t; rules = ruleChecks(t)
        val tsm = ctx.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE) as? TextServicesManager
        if (session == null) session = runCatching { tsm?.newSpellCheckerSession(null, Locale.getDefault(), this, true) }.getOrNull()
        val s = session
        if (s == null) onDone(rules) else runCatching { s.getSentenceSuggestions(arrayOf(TextInfo(t)), 1) }.onFailure { onDone(rules) }
    }

    private fun ruleChecks(t: String): List<String> {
        val out = mutableListOf<String>()
        fun rule(re: String, fix: (MatchResult) -> String?) { Regex(re, RegexOption.IGNORE_CASE).findAll(t).forEach { m -> fix(m)?.let { out += "\"${m.value.trim()}\" → \"$it\"" } } }
        // --- grammar
        val silentH = setOf("hour", "hours", "honest", "honor", "honour", "heir"); val youWords = setOf("university", "user", "users", "uniform", "unit", "european", "usual", "one", "once", "useful", "union", "unique")
        rule("\\ba\\s+([aeiou]\\w*)") { m -> val w = m.groupValues[1].lowercase(); if (w in youWords || w.startsWith("uni") || w.startsWith("use") || w.startsWith("eu")) null else "an $w" }
        rule("\\ban\\s+([b-df-hj-np-tv-z]\\w*)") { m -> val w = m.groupValues[1].lowercase(); if (w in silentH) null else "a $w" }
        rule("\\b(he|she|it)\\s+don'?t\\b") { m -> "${m.groupValues[1]} doesn't" }
        rule("\\b(he|she|it)\\s+have\\b") { m -> "${m.groupValues[1]} has" }
        rule("\\b(he|she|it)\\s+are\\b") { m -> "${m.groupValues[1]} is" }
        rule("\\b(they|we|you)\\s+was\\b") { m -> "${m.groupValues[1]} were" }
        rule("\\b(they|we|you)\\s+is\\b") { m -> "${m.groupValues[1]} are" }
        rule("\\bi\\s+is\\b") { _ -> "I am" }
        rule("\\bi\\s+are\\b") { _ -> "I am" }
        rule("\\b(could|should|would|must)\\s+of\\b") { m -> "${m.groupValues[1]} have" }
        rule("\\bmore\\s+(better|easier|faster|bigger|smaller|cheaper)\\b") { m -> m.groupValues[1] }
        rule("\\b(he|she|it|that|this)\\s+(go|do|make|take|want|need)\\b(?!\\s*(to|not))") { m -> "${m.groupValues[1]} ${m.groupValues[2]}es/s" }
        rule("\\balot\\b") { _ -> "a lot" }
        rule("\\b(dont|cant|wont|didnt|doesnt|isnt|arent|wasnt|couldnt|shouldnt|wouldnt|havent|hasnt)\\b") { m -> m.value.dropLast(2) + "n't" }
        rule("\\b(im|ive|ill|id)\\b(?=\\s)") { m -> when (m.value.lowercase()) { "im" -> "I'm"; "ive" -> "I've"; "ill" -> "I'll"; else -> null } }
        // --- sloppiness
        Regex("(?i)\\b(\\w+)\\s+\\1\\b").findAll(t).forEach { out += "repeated word: \"${it.groupValues[1]}\"" }
        Regex("(^|[.!?]\\s+)([a-z])").findAll(t).forEach { m -> out += "start with a capital: \"${m.groupValues[2]}…\" → \"${m.groupValues[2].uppercase()}…\"" }
        if (Regex("\\bi\\b").containsMatchIn(t)) out += "\"i\" → \"I\""
        if (Regex("[!?]{3,}").containsMatchIn(t)) out += "too many !!! or ???"
        if (Regex(" {2,}").containsMatchIn(t)) out += "double space"
        if (Regex("[a-z],[A-Za-z]").containsMatchIn(t)) out += "no space after comma"
        return out.distinct()
    }

    override fun onGetSuggestions(r: Array<out SuggestionsInfo>?) {}
    override fun onGetSentenceSuggestions(results: Array<out SentenceSuggestionsInfo>?) {
        val out = rules.toMutableList()
        results?.firstOrNull()?.let { s ->
            for (i in 0 until s.suggestionsCount) {
                val info = s.getSuggestionsInfoAt(i)
                if (info.suggestionsAttributes and SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO == 0) continue
                val o = s.getOffsetAt(i); val l = s.getLengthAt(i)
                val w = text.substring(o.coerceAtLeast(0), (o + l).coerceAtMost(text.length))
                if (w.length < 3 || w.any { it.isDigit() } || w.startsWith("@") || w.startsWith("#")) continue
                val fix = if (info.suggestionsCount > 0) info.getSuggestionAt(0) else null
                out += if (fix != null) "$w → $fix" else "$w ?"
            }
        }
        onDone(out.distinct().take(8))
    }
}
