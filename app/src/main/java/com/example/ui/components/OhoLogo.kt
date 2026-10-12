package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OhoCharcoal
import com.example.ui.theme.OhoOrange

/**
 * Official OHO Logo matching the brand sheet:
 * Bold 'O', 'H', and an 'O' containing the distinctive clothes hanger symbol!
 */
@Composable
fun OhoLogo(
    modifier: Modifier = Modifier,
    textColor: Color = OhoOrange,
    showTagline: Boolean = true,
    fontSize: TextUnit = 28.sp,
    iconSize: Dp = 26.dp,
    taglineColor: Color = OhoCharcoal
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // First 'O'
            Text(
                text = "O",
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.5).sp
            )

            // Middle 'H'
            Text(
                text = "H",
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.width(2.dp))

            // Second 'O' containing the distinctive clothes hanger
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(textColor),
                contentAlignment = Alignment.Center
            ) {
                // White Hanger icon drawn on canvas
                Canvas(
                    modifier = Modifier.size(iconSize * 0.65f)
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw the top hook
                    val hookPath = Path().apply {
                        // Hook starting curve
                        moveTo(w * 0.52f, h * 0.32f)
                        cubicTo(
                            w * 0.52f, h * 0.12f,
                            w * 0.72f, h * 0.08f,
                            w * 0.62f, h * 0.02f
                        )
                    }
                    drawPath(
                        path = hookPath,
                        color = Color.White,
                        style = Stroke(
                            width = w * 0.14f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw the hanger triangle body
                    val hangerPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.32f) // apex
                        lineTo(w * 0.1f, h * 0.75f) // left shoulder
                        lineTo(w * 0.9f, h * 0.75f) // right shoulder
                        close()
                    }
                    drawPath(
                        path = hangerPath,
                        color = Color.White,
                        style = Stroke(
                            width = w * 0.14f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        if (showTagline) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "PRE-LOVED. RE-STYLED. RE-LOVED.",
                color = taglineColor,
                fontSize = (fontSize.value * 0.28f).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

/**
 * Compact Header Variant of Oho Logo
 */
@Composable
fun OhoHeaderLogo(
    modifier: Modifier = Modifier,
    textColor: Color = OhoOrange
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OhoLogo(
            textColor = textColor,
            showTagline = false,
            fontSize = 22.sp,
            iconSize = 20.dp
        )
    }
}
