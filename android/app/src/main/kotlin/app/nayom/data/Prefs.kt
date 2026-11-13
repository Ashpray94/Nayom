package app.nayom.data

import android.content.Context

/** Small on-device settings. Which apps keep the avatar hidden, and what Nayom watches. */
class Prefs(ctx: Context) {
    private val p = ctx.getSharedPreferences("nayom", Context.MODE_PRIVATE)
    var hideIn: Set<String>
        get() = p.getStringSet("hideIn", setOf("com.android.camera", "com.sec.android.app.camera", "com.google.android.apps.maps")) ?: emptySet()
        set(v) = p.edit().putStringSet("hideIn", v).apply()
    var avatarOn: Boolean
        get() = p.getBoolean("avatarOn", true); set(v) = p.edit().putBoolean("avatarOn", v).apply()
    var headHidden: Boolean
        get() = p.getBoolean("headHidden", false); set(v) = p.edit().putBoolean("headHidden", v).apply()
    var watchTyping: Boolean
        get() = p.getBoolean("watchTyping", true); set(v) = p.edit().putBoolean("watchTyping", v).apply()
    var avatarY: Int
        get() = p.getInt("avatarY", 400); set(v) = p.edit().putInt("avatarY", v).apply()
    var avatarX: Int
        get() = p.getInt("avatarX", 0); set(v) = p.edit().putInt("avatarX", v).apply()
}

object Vault {
    fun open(ctx: Context) = VaultStore((ctx.getExternalFilesDir(null) ?: ctx.filesDir).resolve("vault"))
}
