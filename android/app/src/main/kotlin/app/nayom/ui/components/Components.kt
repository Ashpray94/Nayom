package app.nayom.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import app.nayom.ui.theme.*

@Composable
fun Icon(res: Int, tint: Color, size: Int = 24) =
    Image(painterResource(res), null, Modifier.size(size.dp), colorFilter = ColorFilter.tint(tint))

/** One card per idea. */
@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Column(modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(Shape.card.dp), ambientColor = colors.teal, spotColor = colors.teal).clip(RoundedCornerShape(Shape.card.dp)).background(colors.surface)
        .border(BorderStroke(1.dp, colors.teal.copy(alpha = .16f)), RoundedCornerShape(Shape.card.dp)).padding(Space.pad.dp), content = content)

/** 44px status-icon tile: tint at ~14% behind the icon. */
@Composable
fun Tile(res: Int, tint: Color) =
    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = .14f)), Alignment.Center) { Icon(res, tint) }

@Composable
fun Button(text: String, primary: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (primary) colors.teal else Color.Transparent
    val fg = if (primary) Color.White else colors.teal
    val shape = RoundedCornerShape(Shape.button.dp)
    Box(modifier.then(if (primary) Modifier.shadow(6.dp, shape, ambientColor = colors.teal, spotColor = colors.teal) else Modifier).clip(shape).background(bg)
        .border(BorderStroke(if (primary) 0.dp else 2.dp, colors.teal), shape).clickable(onClick = onClick).padding(vertical = if (primary) 20.dp else 17.dp), Alignment.Center) {
        Text(text.uppercase(), style = NayomType.button, color = fg)
    }
}

@Composable
fun Label(text: String) = Text(text.uppercase(), style = NayomType.label, color = colors.muted)
