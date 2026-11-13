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
        Regex("(?i)\\b(\\w+)\\s+\\1\\b").findAll(t).forEach { out += "repeated word: \"${it.groupValues[1]}\"" }
        if (Regex("\\bi\\b").containsMatchIn(t)) out += "\"i\" should be \"I\""
        if (Regex("[!?]{3,}").containsMatchIn(t)) out += "too many !!! or ???"
        if (Regex(" {2,}").containsMatchIn(t)) out += "double space"
        if (Regex("[a-z],[A-Za-z]").containsMatchIn(t)) out += "no space after comma"
        return out
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
