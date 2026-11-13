package app.nayom.data

import java.io.File
import java.time.*
import java.time.format.DateTimeFormatter

enum class Status { Open, Moved, Kept, Missed, Released;
    companion object { fun of(s: String) = entries.firstOrNull { it.name.equals(s, true) } ?: Open } }
enum class Decision { Yes, Later, No }

data class Promise(val id: String, val person: String, val what: String, val due: ZonedDateTime, val status: Status)
data class Ask(val id: String, val from: String, val what: String, val created: ZonedDateTime, val decision: Decision?)
data class Note(val name: String, val file: File)
data class Day(val kept: Int, val missed: Int, val moved: Int, val gated: Int, val reflexYes: Int)

/**
 * The vault is the only database (spec 09). One file per promise/ask written once;
 * status lives in append-only ledger lines under ledger/<dev>/. Phone device code: ph.
 * [root] is the vault folder; Nayom files live in root/Nayom.
 */
class VaultStore(val root: File, private val dev: String = "ph", private val zone: ZoneId = ZoneId.systemDefault(),
                 private val clock: () -> ZonedDateTime = { ZonedDateTime.now(zone) }) {
    private val nay get() = File(root, "Nayom")
    private fun dir(n: String) = File(nay, n).also { it.mkdirs() }
    private val iso = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    private var seq = 0
    private fun newId(kind: Char): String {
        val n = clock()
        return "%c-%s-%s-%02d".format(kind, n.format(DateTimeFormatter.ofPattern("yyyyMMdd")), n.format(DateTimeFormatter.ofPattern("HHmmss")) + "-$dev", (++seq))
    }

    fun slug(name: String) = name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifEmpty { "someone" }

    fun addPromise(person: String, what: String, due: ZonedDateTime): Promise {
        val id = newId('P')
        File(dir("promises"), "$id.md").writeText(
            "---\nnayom: promise\nid: $id\nperson: \"[[${slug(person)}]]\"\nwhat: ${q(what)}\ndue: ${due.format(iso)}\n" +
            "due_source: stated\ncreated: ${clock().format(iso)}\nsource_app: nayom\nquote_enc: null\nprivate: false\n---\n")
        ensurePerson(person)
        ledger(id, "open", null)
        log("promise-add", "$id | ${person.trim()} | ${what.trim()} | due ${due.format(iso)}")
        return Promise(id, person.trim(), what.trim(), due, Status.Open)
    }

    fun addAsk(from: String, what: String): Ask {
        val id = newId('A')
        File(dir("asks"), "$id.md").writeText(
            "---\nnayom: ask\nid: $id\nfrom: \"[[${slug(from)}]]\"\nwhat: ${q(what)}\ncreated: ${clock().format(iso)}\nprivate: false\n---\n")
        ensurePerson(from)
        log("ask-in", "$id | ${from.trim()} | ${what.trim()}")
        return Ask(id, from.trim(), what.trim(), clock(), null)
    }

    fun decide(askId: String, d: Decision) { ledger(askId, d.name.lowercase(), null); log("ask-${d.name.lowercase()}", askId) }
    fun setStatus(id: String, s: Status, newDue: ZonedDateTime? = null) {
        ledger(id, s.name.lowercase(), newDue?.format(iso)); log("promise-${s.name.lowercase()}", id + (newDue?.let { " -> ${it.format(iso)}" } ?: ""))
    }

    // ---- activity: everything you do and write, one line each, one file per day. activity/<dev>/YYYY-MM-DD.md
    fun log(kind: String, text: String = "") {
        val now = clock()
        val f = File(dir("activity/$dev"), now.toLocalDate().toString() + ".md")
        if (!f.exists()) f.writeText("# ${now.toLocalDate()}\n\n")
        f.appendText("- ${now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))} | $kind | ${text.trim().replace("\n", " ")}\n")
    }
    data class Entry(val time: String, val kind: String, val text: String)
    fun activity(date: java.time.LocalDate = clock().toLocalDate()): List<Entry> =
        File(dir("activity/$dev"), "$date.md").takeIf { it.exists() }?.readLines().orEmpty()
            .filter { it.startsWith("- ") }.map { it.removePrefix("- ").split(" | ", limit = 3) }
            .filter { it.size == 3 }.map { Entry(it[0], it[1], it[2]) }.reversed()

    // ---- inbox: messages that reached the phone (notification listener). inbox/<dev>/YYYY-MM-DD.md
    fun inbox(app: String, title: String, text: String) {
        val now = clock()
        val f = File(dir("inbox/$dev"), now.toLocalDate().toString() + ".md")
        if (!f.exists()) f.writeText("# ${now.toLocalDate()}\n\n")
        f.appendText("- ${now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))} | $app | ${title.trim().replace("\n", " ")} | ${text.trim().replace("\n", " ")}\n")
    }

    private fun ensurePerson(name: String) {
        val f = File(dir("people"), slug(name) + ".md")
        if (!f.exists()) f.writeText("---\nnayom: person\nname: ${q(name.trim())}\ntier: 1\noverride: null\nprivate: false\n---\n")
    }

    // ---- ledger: ts | id | status | evidence | by | new_due
    private fun ledger(id: String, status: String, newDue: String?) {
        val now = clock()
        val f = File(dir("ledger/$dev"), now.toLocalDate().toString() + ".md")
        val head = "```\nts | id | status | evidence | by | new_due\n"
        val body = if (f.exists()) f.readText().removeSuffix("```\n").removeSuffix("```") else head
        f.writeText(body + "${now.format(iso)} | $id | $status |  | $dev | ${newDue ?: ""}\n```\n")
    }

    private data class Line(val ts: ZonedDateTime, val id: String, val status: String, val newDue: String)
    private fun lines(): List<Line> =
        dir("ledger").listFiles { f -> f.isDirectory }.orEmpty().flatMap { d ->
            d.listFiles { f -> f.name.endsWith(".md") }.orEmpty().flatMap { f ->
                f.readLines().filter { it.contains(" | ") && !it.startsWith("ts |") }.mapNotNull { l ->
                    val p = l.split(" | ").map { it.trim() }
                    if (p.size < 6) null else runCatching { Line(ZonedDateTime.parse(p[0]), p[1], p[2], p[5]) }.getOrNull()
                }
            }
        }.sortedBy { it.ts }

    /** Current state of an item = latest line for its id across all devices. */
    private fun latest(): Map<String, Line> = lines().associateBy { it.id }

    private fun front(f: File): Map<String, String> =
        f.readLines().drop(1).takeWhile { it != "---" }.mapNotNull {
            val i = it.indexOf(':'); if (i < 0) null else it.substring(0, i) to it.substring(i + 1).trim().removeSurrounding("\"").replace("\\\"", "\"")
        }.toMap()

    private fun person(raw: String) = raw.removePrefix("[[").removeSuffix("]]").replace('-', ' ').replaceFirstChar { it.uppercase() }

    fun promises(): List<Promise> {
        val st = latest()
        return dir("promises").listFiles { f -> f.name.endsWith(".md") }.orEmpty().mapNotNull { f ->
            val m = front(f); val id = m["id"] ?: return@mapNotNull null
            val l = st[id]
            val due = l?.newDue?.takeIf { it.isNotEmpty() }?.let { ZonedDateTime.parse(it) } ?: ZonedDateTime.parse(m["due"] ?: return@mapNotNull null)
            Promise(id, person(m["person"] ?: ""), m["what"] ?: "", due, Status.of(l?.status ?: "open").let { if (it == Status.Moved) Status.Open else it })
        }.sortedBy { it.due }
    }

    fun asks(): List<Ask> {
        val st = latest()
        return dir("asks").listFiles { f -> f.name.endsWith(".md") }.orEmpty().mapNotNull { f ->
            val m = front(f); val id = m["id"] ?: return@mapNotNull null
            val d = st[id]?.status?.let { s -> Decision.entries.firstOrNull { it.name.equals(s, true) } }
            Ask(id, person(m["from"] ?: ""), m["what"] ?: "", ZonedDateTime.parse(m["created"]), d)
        }.sortedByDescending { it.created }
    }

    /** Anything open and past due is a miss. Written once as a ledger line so every device agrees. */
    fun sweepMissed(): Int {
        val now = clock(); var n = 0
        promises().filter { it.status == Status.Open && it.due.isBefore(now) }.forEach { setStatus(it.id, Status.Missed); n++ }
        return n
    }

    fun people(): List<Pair<String, Int>> {
        val open = promises().filter { it.status == Status.Open }.groupingBy { it.person }.eachCount()
        val names = dir("people").listFiles { f -> f.name.endsWith(".md") }.orEmpty().map { front(it)["name"] ?: it.nameWithoutExtension }
        return names.map { it to (open[it] ?: 0) }.sortedByDescending { it.second }
    }

    fun today(): Day {
        val now = clock().toLocalDate()
        val l = lines().filter { it.ts.toLocalDate() == now }
        val asks = asks().associateBy { it.id }
        val reflex = l.count { it.status == "yes" && asks[it.id]?.let { a -> java.time.Duration.between(a.created, it.ts).seconds < 8 } == true }
        return Day(l.count { it.status == "kept" }, l.count { it.status == "missed" }, l.count { it.status == "moved" },
            l.count { it.status in setOf("yes", "later", "no") }, reflex)
    }

    fun addDebrief(promiseId: String, reason: String) {
        val id = newId('D')
        File(dir("debriefs"), "$id.md").writeText("---\nnayom: debrief\nid: $id\npromise: \"[[$promiseId]]\"\ncreated: ${clock().format(iso)}\nreason: ${q(reason)}\n---\n")
        log("debrief", "$promiseId | ${reason.trim()}")
    }
    fun debriefed(): Set<String> = dir("debriefs").listFiles { f -> f.name.endsWith(".md") }.orEmpty()
        .mapNotNull { front(it)["promise"]?.removePrefix("[[")?.removeSuffix("]]") }.toSet()

    // ---- notes: your own markdown, outside Nayom/. Read-only unless Nayom wrote it.
    fun notes(): List<Note> = root.walkTopDown().onEnter { it.name != "Nayom" && !it.name.startsWith(".") }
        .filter { it.isFile && it.extension == "md" }.map { Note(it.nameWithoutExtension, it) }.sortedBy { it.name }.toList()

    fun addNote(title: String, body: String): Note {
        val f = File(root, slug(title) + ".md"); f.writeText("# ${title.trim()}\n\n$body\n"); log("note", "${title.trim()} | ${body.trim()}"); return Note(title, f)
    }

    private fun q(s: String) = "\"" + s.trim().replace("\"", "\\\"") + "\""
}
