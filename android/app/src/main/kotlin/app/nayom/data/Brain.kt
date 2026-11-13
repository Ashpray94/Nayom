package app.nayom.data

import android.content.Context
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The central "neural network": a plain Obsidian vault. Everything Nayom sees becomes a linked markdown note
 * (Daily/, Apps/, Chess/, Index.md) so Obsidian's graph shows how your days connect, and Nayom re-reads it to judge you.
 */
class Brain(private val ctx: Context) {
    private val root: File get() = Vault.open(ctx).root
    private val hm = DateTimeFormatter.ofPattern("HH:mm")
    private fun dir(n: String) = File(root, n).also { it.mkdirs() }
    private fun safe(n: String) = n.replace(Regex("[\\\\/:*?\"<>|#^\\[\\]]"), " ").trim().ifEmpty { "Unknown" }

    private fun daily(): File {
        val d = LocalDate.now(); val f = File(dir("Daily"), "$d.md")
        if (!f.exists()) f.writeText("---\nnayom: day\ndate: $d\ntags: [daily]\n---\n# $d\n\n## Timeline\n")
        return f
    }

    /** One line on today's note, linked to the app note. */
    fun event(kind: String, app: String?, text: String) {
        runCatching {
            val link = app?.let { ensureApp(it); " [[Apps/${safe(it)}|${safe(it)}]]" } ?: ""
            daily().appendText("- ${LocalTime.now().format(hm)}$link **$kind** ${text.trim().replace("\n", " ").take(240)}\n")
        }
    }

    private fun ensureApp(app: String) {
        val f = File(dir("Apps"), "${safe(app)}.md")
        if (!f.exists()) f.writeText("---\nnayom: app\nname: ${safe(app)}\ntags: [app]\n---\n# ${safe(app)}\n\nSeen by Nayom. Stats below are rewritten after every session.\n")
    }

    /** Called when a stay in an app ends: rewrites the app's note with what Nayom has learnt about it. */
    fun session(pkg: String, app: String, ms: Long, h: Habits) {
        runCatching {
            event("used", app, "${ms / 60000}m ${ms / 1000 % 60}s")
            val hours = (0..23).map { it to h.hourCount(pkg, it) }.filter { it.second > 0 }.sortedByDescending { it.second }.take(5)
            val f = File(dir("Apps"), "${safe(app)}.md"); ensureApp(app)
            val head = f.readText().substringBefore("\n## Pattern")
            f.writeText(head.trimEnd() + "\n\n## Pattern\n- opens: ${h.opens(pkg)}\n- usual stay: ${h.usualMin(pkg)} min\n- today: ${h.todayMin(pkg)} min\n" +
                "- usual hours: ${hours.joinToString(", ") { "${it.first}:00 (${it.second}x)" }.ifEmpty { "not enough data yet" }}\n- time-waster: ${Habits.waster(pkg)}\n- package: `$pkg`\n\nDays: [[Daily/${LocalDate.now()}|today]]\n")
            index()
        }
    }

    fun person(name: String, line: String) {
        runCatching {
            val f = File(dir("People"), "${safe(name)}.md")
            if (!f.exists()) f.writeText("---\nnayom: person\nname: ${safe(name)}\ntags: [person]\n---\n# ${safe(name)}\n\n## Promises and asks\n")
            f.appendText("- [[Daily/${LocalDate.now()}|${LocalDate.now()}]] $line\n"); event("promise", null, "$line → [[People/${safe(name)}|${safe(name)}]]")
        }
    }

    fun chessGame(moves: List<String>, result: String, blunders: Int, mistakes: Int, inaccuracies: Int): File {
        val now = java.time.LocalDateTime.now(); val f = File(dir("Chess"), "${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm"))}.md")
        val pgn = moves.chunked(2).mapIndexed { i, p -> "${i + 1}. ${p.joinToString(" ")}" }.joinToString(" ")
        f.writeText("---\nnayom: chess\ndate: ${now.toLocalDate()}\nmoves: ${moves.size}\nresult: \"$result\"\nblunders: $blunders\nmistakes: $mistakes\ninaccuracies: $inaccuracies\ntags: [chess]\n---\n# Chess ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}\n\n$pgn\n\nResult: $result · blunders $blunders · mistakes $mistakes · inaccuracies $inaccuracies\n\nDay: [[Daily/${now.toLocalDate()}|today]]\n")
        event("chess", "Chess", "${moves.size} moves, $result, $blunders blunders → [[Chess/${f.nameWithoutExtension}]]")
        index(); return f
    }
    fun lastChess(): Triple<Int, Int, Int>? = dir("Chess").listFiles { x -> x.extension == "md" }.orEmpty().sortedBy { it.name }.let { l ->
        if (l.size < 2) null else l[l.size - 2].readText().let { t -> fun n(k: String) = Regex("$k: (\\d+)").find(t)?.groupValues?.get(1)?.toInt() ?: 0; Triple(n("blunders"), n("mistakes"), n("inaccuracies")) } }

    fun index() {
        val apps = dir("Apps").listFiles { f -> f.extension == "md" }.orEmpty().map { it.nameWithoutExtension }.sorted()
        val days = dir("Daily").listFiles { f -> f.extension == "md" }.orEmpty().map { it.nameWithoutExtension }.sortedDescending().take(14)
        val chess = dir("Chess").listFiles { f -> f.extension == "md" }.orEmpty().map { it.nameWithoutExtension }.sortedDescending().take(10)
        File(root, "Index.md").writeText("---\nnayom: index\ntags: [moc]\n---\n# Nayom brain\n\nThe map of everything Nayom has seen. Open the graph view.\n\n## Days\n" + days.joinToString("\n") { "- [[Daily/$it|$it]]" } +
            "\n\n## Apps\n" + apps.joinToString("\n") { "- [[Apps/$it|$it]]" } + "\n\n## Chess\n" + chess.joinToString("\n") { "- [[Chess/$it|$it]]" } + "\n")
    }
}
