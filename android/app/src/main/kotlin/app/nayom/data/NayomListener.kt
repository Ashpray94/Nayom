package app.nayom.data

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Logs messages that reach the phone into the vault inbox. Never reads banking, OTP, authenticator or password apps. */
class NayomListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName || !sbn.isClearable) return
        if (Blocked.app(sbn.packageName)) return
        val e = sbn.notification.extras
        val title = e.getCharSequence("android.title")?.toString().orEmpty()
        val text = (e.getCharSequence("android.bigText") ?: e.getCharSequence("android.text"))?.toString().orEmpty()
        if (text.isBlank() || Blocked.text(text)) return
        runCatching {
            VaultStore((getExternalFilesDir(null) ?: filesDir).resolve("vault")).inbox(sbn.packageName.substringAfterLast('.'), title, text)
        }
    }
}

object Blocked {
    private val words = listOf("bank", "pay", "upi", "wallet", "authenticator", "otp", "password", "pass", "vault", "keepass", "lastpass", "bitwarden", "1password", "sbi", "hdfc", "icici", "axis", "kotak", "paytm", "phonepe", "gpay", "cred")
    fun app(pkg: String) = words.any { pkg.lowercase().contains(it) }
    private val otp = Regex("""(?i)\b(otp|one[- ]time|verification code|passcode|cvv|pin)\b|\b\d{4,8}\b.*\b(code|otp)\b""")
    fun text(t: String) = otp.containsMatchIn(t)
}
