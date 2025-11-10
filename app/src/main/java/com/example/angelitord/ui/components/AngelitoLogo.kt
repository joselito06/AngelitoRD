package com.example.angelitord.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Logo animado de Angelito RD para el TopAppBar
 */
@Composable
fun AngelitoLogo(
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    // Animación sutil de rotación para el emoji
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Emoji de angelito con animación
        Text(
            text = "👼",
            fontSize = 28.sp,
            modifier = Modifier.rotate(if (animated) rotation else 0f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Texto con gradiente
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6750A4), // Morado
                                Color(0xFFD0BCFF), // Morado claro
                                Color(0xFF7D5260)  // Rosa
                            )
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                ) {
                    append("Angelito")
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                ) {
                    append(" RD")
                }
            },
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Logo simple sin animación para pantallas secundarias
 */
@Composable
fun AngelitoLogoSimple(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "👼",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Angelito RD",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Logo minimalista con solo el emoji
 */
@Composable
fun AngelitoLogoIcon(
    size: Int = 32,
    modifier: Modifier = Modifier
) {
    Text(
        text = "👼",
        fontSize = size.sp,
        modifier = modifier
    )
}

/**
 * Título estilizado para pantallas secundarias
 */
@Composable
fun StyledTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}