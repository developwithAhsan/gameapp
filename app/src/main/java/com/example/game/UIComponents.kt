package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GameBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Gradient backdrop representing space-themed puzzle classic games
                // From #0F172A through #1E1B4B to #020617
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate-900 / Deep Slate Navy
                        Color(0xFF1E1B4B), // Indigo-950 / Midnight Indigo
                        Color(0xFF020617)  // Black / Rich Darkness
                    )
                )
                drawRect(brush = brush)

                // Faint cosmic geometrical accents to avoid plain static backgrounds
                val path1 = Path().apply {
                    moveTo(size.width * 0.1f, -50f)
                    lineTo(size.width * 0.8f, size.height * 0.4f)
                    lineTo(size.width * 0.4f, size.height * 0.7f)
                    close()
                }
                drawPath(
                    path = path1,
                    color = Color(0x0422D3EE), // subtle cyan neon outline
                    style = Stroke(width = 2.dp.toPx())
                )

                val path2 = Path().apply {
                    moveTo(size.width * -0.2f, size.height * 0.5f)
                    lineTo(size.width * 0.5f, size.height * 0.95f)
                    lineTo(size.width * 0.9f, size.height * 0.6f)
                }
                drawPath(
                    path = path2,
                    color = Color(0x03818CF8), // subtle indigo outline
                    style = Stroke(width = 3.dp.toPx())
                )

                // Gentle floating circle frames representing orbits/planets
                drawCircle(
                    color = Color(0x02FFFFFF),
                    radius = size.width * 0.25f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                drawCircle(
                    color = Color(0x0222D3EE),
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.1f, size.height * 0.8f)
                )
            }
    )
}

@Composable
fun BlockTile(
    colorIndex: Int,
    modifier: Modifier = Modifier,
    isGhost: Boolean = false,
    clearedFlash: Boolean = false
) {
    if (colorIndex == 0) {
        // Grid board empty cell placeholder matching `bg-slate-800/40 rounded-sm shadow-inner`
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x331E293B)) // Slate 800 with 40% (0x66) or 20% (0x33) opacity
                .border(0.5.dp, Color(0x1F38BDF8), RoundedCornerShape(4.dp)) // Glowing layout border line
        )
        return
    }

    // Modern solid base color configurations from Geometric Balance HTML theme directions
    val (baseColor, accentColor) = when (colorIndex) {
        1 -> Pair(Color(0xFFEF4444), Color(0xFFFCA5A5)) // Red-500, Red-300
        2 -> Pair(Color(0xFF06B6D4), Color(0xFF67E8F9)) // Cyan-500, Cyan-300
        3 -> Pair(Color(0xFF8B5CF6), Color(0xFFC4B5FD)) // Violet-500, Violet-300
        4 -> Pair(Color(0xFFFACC15), Color(0xFFFEF08A)) // Yellow-400, Yellow-200
        5 -> Pair(Color(0xFF10B981), Color(0xFF6EE7B7)) // Emerald-500, Emerald-300
        6 -> Pair(Color(0xFFF97316), Color(0xFFFDBA74)) // Orange-500, Orange-300
        else -> Pair(Color(0xFF64748B), Color(0xFFCBD5E1)) // Slate fallback
    }

    val alphaVal = if (isGhost) 0.5f else 1.0f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .drawBehind {
                if (clearedFlash) {
                    drawRect(Color.White)
                } else {
                    // Draw base color rectangle
                    drawRect(color = baseColor, alpha = alphaVal)

                    // Draw top-half specular inset white gloss element:
                    // Looks like Tailwind's shadow-[inset_0_2px_4px_rgba(255,255,255,0.4)]
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.4f * alphaVal), Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.4f
                        )
                    )

                    // Draw outer translucent border matching the color scheme: border border-cyan-300/30
                    drawRect(
                        color = accentColor.copy(alpha = 0.3f * alphaVal),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            .then(
                if (isGhost) {
                    Modifier.border(2.dp, Color(0xFF00FFCC), RoundedCornerShape(4.dp))
                } else {
                    Modifier
                }
            )
    )
}
