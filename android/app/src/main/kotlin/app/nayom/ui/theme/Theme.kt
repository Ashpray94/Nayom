package app.nayom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import app.nayom.R

class NayomColors(
    val bg: Color, val surface: Color, val ink: Color, val muted: Color,
    val teal: Color, val amber: Color, val berry: Color, val leaf: Color, val sky: Color,
)

val LightColors = NayomColors(
    Color(0xFFF2F8F7), Color(0xFFFFFFFF), Color(0xFF12302F), Color(0xFF4B6663),
    Color(0xFF23706B), Color(0xFFF5A524), Color(0xFFB3265B), Color(0xFF3F7A1F), Color(0xFF2563B8))
val DarkColors = NayomColors(
    Color(0xFF0B1B1D), Color(0xFF122A2C), Color(0xFFE8F5F2), Color(0xFFA9C4C0),
    Color(0xFF5FD0C0), Color(0xFFFFB938), Color(0xFFFF7AA8), Color(0xFF9FD66B), Color(0xFF7FB0FF))

val Sora = FontFamily(
    Font(R.font.sora_extralight, FontWeight.ExtraLight), Font(R.font.sora_light, FontWeight.Light),
    Font(R.font.sora_regular, FontWeight.Normal), Font(R.font.sora_semibold, FontWeight.SemiBold),
    Font(R.font.sora_bold, FontWeight.Bold))
val Cormorant = FontFamily(
    Font(R.font.cormorant_regular_normal, FontWeight.Normal), Font(R.font.cormorant_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.cormorant_medium_normal, FontWeight.Medium), Font(R.font.cormorant_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.cormorant_semibold_normal, FontWeight.SemiBold), Font(R.font.cormorant_semibold_italic, FontWeight.SemiBold, FontStyle.Italic))

/** Type scale from spec/tokens.json (D-25). Roman for firm moments, italic for warm. */
object NayomType {
    val hero = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontSize = 72.sp, lineHeight = 70.sp)
    val heading = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontSize = 40.sp, lineHeight = 44.sp)
    val headingWarm = heading.copy(fontStyle = FontStyle.Italic)
    val display = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 46.sp, lineHeight = 46.sp)
    val numeral = TextStyle(fontFamily = Sora, fontWeight = FontWeight.ExtraLight, fontSize = 56.sp)
    val title = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 30.sp)
    val body = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp)
    val status = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.8.sp)
    val label = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 2.sp)
    val button = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.6.sp)
}

object Shape { const val card = 22; const val button = 6; const val tile = 44 }
/** 8dp grid. Screen edge 24, gap between ideas 24, inside a card 24. */
object Space { const val edge = 24; const val gap = 24; const val pad = 24; const val tight = 8 }

val LocalNayomColors = staticCompositionLocalOf { LightColors }

@Composable
fun NayomTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNayomColors provides (if (dark) DarkColors else LightColors), content = content)
}
val colors: NayomColors @Composable get() = LocalNayomColors.current
