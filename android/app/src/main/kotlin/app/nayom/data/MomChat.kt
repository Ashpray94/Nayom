package app.nayom.data

import java.time.ZonedDateTime

/** Mother's replies. Rules on the phone, no network: she answers from what she knows about your day. */
object MomChat {
    data class Reply(val text: String, val draft: String? = null, val markDoneId: String? = null)

    private fun has(t: String, p: String) = Regex("(?i)\\b($p)\\b").containsMatchIn(t)
    private fun <T> List<T>.pick() = random()

    fun reply(input: String, open: List<Promise>, wastedMin: Int, now: ZonedDateTime): Reply {
        val t = input.trim()
        val hour = now.hour
        val next = open.firstOrNull()

        // a promise in the message: help write it down
        val hit = Detector.promise(t)
        if (hit != null && hit.kind == "promise") {
            val p = Nlp.parse(t, now)
            val when_ = p.due?.let { " by ${it.format(java.time.format.DateTimeFormatter.ofPattern("EEE h:mm a"))}" } ?: " (tell me when)"
            val who = p.who?.let { " for ${it}" } ?: ""
            return Reply("Good that you said it to me. I wrote it down: ${p.what}$who$when_. Save it and I will ask you when the time comes.", draft = t)
        }
        if (has(t, "done|finished|completed|did it|ho gaya|ho gya|kar diya|kar liya|sent it|submitted")) {
            return if (next != null) Reply("Really? Then mark \"${next.what}\" as done. If I find out you lied, I will know.", markDoneId = next.id)
            else Reply("Good. What did you finish? Tell me, so I can be proud properly.")
        }
        if (has(t, "ate|eaten|had lunch|had dinner|had breakfast|khana kha liya|kha liya|khaa liya"))
            return Reply(listOf("Good boy. Fruit also, not only maggi.", "Good. Drink water now.").pick())
        if (has(t, "hungry|bhook|bhukh|food|khana|lunch|dinner|breakfast"))
            return Reply("Then go and eat first. Something proper, from home if possible. Then come back and tell me what you ate.")
        if (has(t, "tired|sleepy|thak|neend|sleep|so jaon|going to bed|good night|gn"))
            return Reply(if (hour >= 22 || hour < 5) "Yes, sleep now. Phone away, lights off. We talk tomorrow." else "Rest for twenty minutes, not two hours. Then do the one thing that is pending${next?.let { ": ${it.what}" } ?: ""}.")
        if (has(t, "sad|stressed|stress|anxious|worried|upset|low|alone|lonely|cry|crying|depressed|tension|pareshan"))
            return Reply("Come, tell me what happened. I am listening. Write it all, I will not interrupt. If it is too heavy, please also talk to someone who is with you, a friend or family.")
        if (has(t, "later|tomorrow i|kal karunga|kal kar|no mood|lazy|bored|can't|cant|don't feel|dont feel|nahi hoga|baad mein"))
            return Reply(if (next != null) "No. Not later. Start \"${next.what}\" for ten minutes only. After ten minutes, if you still want to stop, tell me." else "Nothing is pending, so why this excuse? Go and do something useful for ten minutes.")
        if (has(t, "instagram|insta|reels|youtube|netflix|phone|scroll|scrolling|twitter|facebook"))
            return Reply(if (wastedMin >= 20) "$wastedMin minutes gone today on that. Put the phone down and do one real thing. Now." else "Just a little? You always say that. Put it down when I say, okay?")
        if (has(t, "what.*(pending|due|to do|todo)|pending|due today|my promises|what should i do|kya karun"))
            return Reply(if (open.isEmpty()) "Nothing is pending. Use the free time well. Read, walk, call someone at home." else "You owe: " + open.take(4).joinToString("; ") { "${it.what} (${it.person})" } + ". Start with the first one.")
        if (has(t, "how am i|how.s my day|am i doing|my day"))
            return Reply(if (wastedMin > 30) "Not great. $wastedMin minutes on time-wasting apps. But the day is not over. Fix it." else "Okay so far. Keep going and finish what you owe.")
        if (has(t, "thanks|thank you|thx|shukriya"))
            return Reply("Thanks kya. Go and do your work.")
        if (has(t, "hate you|shut up|stop nagging|leave me|annoying|irritating"))
            return Reply("I nag because I care. You asked me to. Go take a walk, drink water, and come back.")
        if (has(t, "hi|hello|hey|namaste|good morning|good evening|good afternoon"))
            return Reply((if (hour < 12) "Good morning. Did you eat breakfast? " else if (hour < 17) "Hello. Did you have lunch? " else "Come, sit. Did you eat? ") + (next?.let { "Remember, \"${it.what}\" is due." } ?: "Nothing is pending. Good."))
        return Reply(listOf("Okay. And what is pending today? Tell me.", "Hmm. Tell me properly, what is going on?", "Okay, but first tell me, did you eat and did you finish your work?").pick())
    }
}
