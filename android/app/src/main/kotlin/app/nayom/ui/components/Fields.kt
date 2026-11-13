package app.nayom.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nayom.ui.theme.*

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, singleLine: Boolean = true, minHeight: Int = 0) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Label(label)
        BasicTextField(value, onChange, singleLine = singleLine, textStyle = NayomType.body.copy(color = colors.ink),
            cursorBrush = SolidColor(colors.teal),
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight.dp).clip(RoundedCornerShape(14.dp)).background(colors.surface).padding(18.dp))
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) =
    Box(Modifier.clip(RoundedCornerShape(Shape.button.dp)).background(if (selected) colors.teal else colors.surface)
        .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text, style = NayomType.body, color = if (selected) androidx.compose.ui.graphics.Color.White else colors.ink)
    }

@Composable
fun Row2(title: String, sub: String, trailing: String? = null, onClick: (() -> Unit)? = null) =
    Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) { Text(title, style = NayomType.body, color = colors.ink); Text(sub, style = NayomType.body.copy(fontSize = 13.sp), color = colors.muted) }
        if (trailing != null) Label(trailing)
    }

/** Screen heading: short serif word. Roman = firm, italic = warm. */
@Composable
fun Title(text: String, warm: Boolean = false, color: androidx.compose.ui.graphics.Color = colors.ink) =
    Text(text, style = if (warm) NayomType.headingWarm else NayomType.heading, color = color)

/** Single-line illustration: robot mum in ink, one concept fill in an accent. Layers so dark mode just works. */
@Composable
fun Illustration(name: String, accent: androidx.compose.ui.graphics.Color, size: Int = 260) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    fun id(suffix: String) = ctx.resources.getIdentifier("ill_${name}_$suffix", "drawable", ctx.packageName)
    val art = ctx.resources.getIdentifier("art_$name", "drawable", ctx.packageName)
    if (art != 0) {
        androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(art), null, Modifier.size(size.dp))
        return
    }
    Box(Modifier.size(size.dp)) {
        Icon(id("brand"), colors.teal.copy(alpha = .45f), size)
        Icon(id("fill"), accent.copy(alpha = .38f), size)
        Icon(id("line"), colors.ink, size)
    }
}

/** Empty or celebration state: illustration, one serif line, one plain sentence. The action sits in the screen's bottom actions. */
@Composable
fun Empty(art: String, accent: androidx.compose.ui.graphics.Color, line: String, sentence: String, modifier: Modifier = Modifier) =
    Column(modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Illustration(art, accent)
        Text(line, style = NayomType.display.copy(fontSize = 38.sp, lineHeight = 44.sp), color = colors.ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(sentence, style = NayomType.body, color = colors.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp))
    }

/**
 * Hero dial: the one thing on Today. A hairline ring fills as the deadline nears (24 h window).
 * Calm teal until the last hour, then berry. Everything inside is centred and quiet.
 */
@Composable
fun HeroDial(label: String, time: String, title: String, sub: String, progress: Float, urgent: Boolean) {
    val arc = if (urgent) colors.berry else colors.teal
    val track = colors.ink.copy(alpha = .08f)
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(colors.surface).padding(vertical = 40.dp, horizontal = 24.dp),
        androidx.compose.ui.Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(280.dp)) {
            val w = 3.dp.toPx(); val inset = w / 2
            val sz = androidx.compose.ui.geometry.Size(size.width - w, size.height - w)
            val tl = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(track, 0f, 360f, false, tl, sz, style = androidx.compose.ui.graphics.drawscope.Stroke(w))
            drawArc(arc, -90f, 360f * progress.coerceIn(0.004f, 1f), false, tl, sz,
                style = androidx.compose.ui.graphics.drawscope.Stroke(w, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.width(210.dp)) {
            Text(label.uppercase(), style = NayomType.label, color = colors.muted)
            Spacer(Modifier.height(14.dp))
            Text(time, style = NayomType.numeral.copy(fontSize = 44.sp), color = if (urgent) colors.berry else colors.ink)
            Spacer(Modifier.height(14.dp))
            Text(title, style = NayomType.title.copy(fontSize = 18.sp, lineHeight = 24.sp), color = colors.ink,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(sub, style = NayomType.body.copy(fontSize = 13.sp), color = colors.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
