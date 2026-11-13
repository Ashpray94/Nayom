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
