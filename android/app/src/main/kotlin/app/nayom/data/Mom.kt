package app.nayom.data

import java.time.LocalTime

/** How she talks: nagging that is love (food, sleep, phone, "I know you"), never shame about body or worth. */
object Mom {
    private fun <T> List<T>.pick() = random()
    fun pattern(app: String, usual: Int, opens: Int) = listOf(
        "Arre, $app again? This is exactly your time for it. Not now, beta.",
        "$app kholne ka time ho gaya, na? I know you. Not now.",
        "Every day at this time, $app. $opens times already. Aaj mat kholo.",
        "Hand off $app. $usual minutes becomes an hour, and then you say 'bas ek minute'.").pick()
    fun wasted(app: String, min: Int, total: Int) = when {
        min < 20 -> listOf("$min minutes on $app. Phone mein hi ghus jaoge kya?", "$min minutes. Enough now, close $app.").pick()
        min < 30 -> listOf("$min minutes, $app. Is this what I raised you for?", "Aankhein kharab hongi. Close $app. Abhi.").pick()
        else -> "$min minutes on $app, $total minutes wasted today. Main kuch bol rahi hoon, sun rahe ho?"
    }
    fun meal(): String? { val h = LocalTime.now().hour
        return when (h) { in 8..9 -> "Breakfast kiya? Khali pet phone mat chalao."; in 13..14 -> "Khana khaya? Thanda ho raha hoga, jao pehle."
            in 16..17 -> "Chai ke saath kuch khaya? Sirf chai nahi."; in 20..21 -> "Dinner time. Phone rakho, kuch ghar ka khao."; else -> null } }
    fun late(): String = listOf("Itni raat ho gayi. Phone rakho aur so jao.", "Neend nahi aati kyunki phone haath mein hai. So jao, beta.", "Kal subah kaun uthayega? Main to nahi.").pick()
    fun kept() = listOf("Wah. Dekha, ho gaya na? Proud of you.", "Good boy. Ab pani piyo.").pick()
    fun missed() = listOf("Tumne bola tha. Mujhe yaad hai.", "Promise toota. Mujhe bura laga, aur tumhe bhi lagna chahiye.").pick()
    fun protect() = "Koi kuch bole to mujhe batana. Tumhe main hi daantungi."
}
