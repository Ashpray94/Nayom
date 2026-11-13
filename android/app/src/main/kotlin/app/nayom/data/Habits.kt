package app.nayom.data

import android.content.Context
import java.time.LocalDate
import java.time.LocalTime

/** What you open, when, and for how long. Learned from every app switch. */
class Habits(private val ctx: Context) {
    private val p = ctx.getSharedPreferences("habits", Context.MODE_PRIVATE)
    companion object {
        val wasters = listOf("instagram", "facebook", "snapchat", "reddit", "twitter", "com.x.", "tiktok", "youtube", "netflix", "hotstar", "primevideo", "9gag", "pinterest", "moj", "josh", "sharechat")
        fun waster(pkg: String) = wasters.any { pkg.lowercase().contains(it) }
        @Volatile var cur = ""; @Volatile var since = 0L
    }

    fun label(pkg: String): String = runCatching {
        val pm = ctx.packageManager; pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg.substringAfterLast('.'))

    fun currentMs() = if (cur.isEmpty()) 0L else System.currentTimeMillis() - since

    /** Call when a new app comes to the front. */
    @Synchronized fun switchTo(pkg: String) {
        if (pkg == cur) return
        val now = System.currentTimeMillis()
        if (cur.isNotEmpty() && now - since > 3000) close(cur, now - since)
        cur = pkg; since = now
        val h = LocalTime.now().hour
        p.edit().putInt("hr:$pkg:$h", p.getInt("hr:$pkg:$h", 0) + 1).apply()
    }

    private fun close(pkg: String, ms: Long) {
        val d = LocalDate.now().toString()
        p.edit().putInt("n:$pkg", p.getInt("n:$pkg", 0) + 1).putLong("ms:$pkg", p.getLong("ms:$pkg", 0) + ms)
            .putLong("day:$pkg:$d", p.getLong("day:$pkg:$d", 0) + ms).apply()
        runCatching { Vault.open(ctx).log("move", "${label(pkg)} ${ms / 60000}m ${ms / 1000 % 60}s"); Brain(ctx).session(pkg, label(pkg), ms, this) }
    }

    fun hourCount(pkg: String, h: Int) = p.getInt("hr:$pkg:$h", 0)
    fun opens(pkg: String) = p.getInt("n:$pkg", 0)
    fun usualMin(pkg: String): Int { val n = opens(pkg); return if (n == 0) 0 else (p.getLong("ms:$pkg", 0) / n / 60000).toInt() }
    fun todayMin(pkg: String): Int = ((p.getLong("day:$pkg:${LocalDate.now()}", 0) + (if (cur == pkg) currentMs() else 0)) / 60000).toInt()
    fun wasterToday(): Int = p.all.keys.filter { it.startsWith("day:") && it.endsWith(LocalDate.now().toString()) && waster(it.substringAfter("day:").substringBefore(":")) }
        .sumOf { p.getLong(it, 0) / 60000 }.toInt() + (if (waster(cur)) (currentMs() / 60000).toInt() else 0)

    /** You open this app around this hour often, and it is a time-waster or you stay long. */
    fun isPattern(pkg: String): Boolean { val c = hourCount(pkg, LocalTime.now().hour); return c >= 3 && (waster(pkg) || usualMin(pkg) >= 10) }

    /** The app you most often open around this hour, if there is a clear habit. */
    fun expectedNow(hour: Int = LocalTime.now().hour): String? {
        val best = p.all.keys.filter { it.startsWith("hr:") && it.endsWith(":$hour") }
            .map { it.removePrefix("hr:").removeSuffix(":$hour") to p.getInt(it, 0) }.maxByOrNull { it.second } ?: return null
        return if (best.second >= 3) best.first else null
    }
}
