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

/** Empty / celebration state: one icon tile, one short serif line, one plain sentence. Action sits in the screen's bottom actions. */
@Composable
fun Empty(icon: Int, tint: androidx.compose.ui.graphics.Color, line: String, sentence: String, modifier: Modifier = Modifier) =
    Column(modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(24.dp)).background(tint.copy(alpha = .14f)), androidx.compose.ui.Alignment.Center) {
            Icon(icon, tint, 36)
        }
        Text(line, style = NayomType.display.copy(fontSize = 40.sp, lineHeight = 44.sp), color = colors.ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(sentence, style = NayomType.body, color = colors.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp))
    }
