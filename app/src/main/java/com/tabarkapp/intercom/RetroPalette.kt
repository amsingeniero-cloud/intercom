package com.tabarkapp.intercom

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores del "chasis" (neutros + texto/iconos) que cambian entre tema Claro y Oscuro.
 * Los colores de marca/semanticos (LEDs, botones HABLAR/MANOS LIBRES, LCD) viven en
 * RetroColors y no cambian con el tema: un LCD siempre es verde, un LED siempre es rojo/verde.
 */
data class RetroPalette(
    val background: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val surfaceVariant: Color,
    val speakerGrille: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val primaryLight: Color,
)

val DarkRetroPalette = RetroPalette(
    background = Color(0xFF131313),
    surfaceContainerLowest = Color(0xFF0E0E0E),
    surfaceContainerLow = Color(0xFF1C1B1B),
    surfaceContainer = Color(0xFF20201F),
    surfaceContainerHigh = Color(0xFF2A2A2A),
    surfaceContainerHighest = Color(0xFF353535),
    surfaceVariant = Color(0xFF353535),
    speakerGrille = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE5E2E1),
    onSurfaceVariant = Color(0xFFE4BEB1),
    outline = Color(0xFFAB897D),
    outlineVariant = Color(0xFF5B4137),
    primaryLight = Color(0xFFFFB59A),
)

val LightRetroPalette = RetroPalette(
    background = Color(0xFFEDE8E0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F2EB),
    surfaceContainer = Color(0xFFEFEAE1),
    surfaceContainerHigh = Color(0xFFE1DACC),
    surfaceContainerHighest = Color(0xFFD1C8B6),
    surfaceVariant = Color(0xFFD1C8B6),
    speakerGrille = Color(0xFFDAD3C4),
    onSurface = Color(0xFF2B2620),
    onSurfaceVariant = Color(0xFF6E4B37),
    outline = Color(0xFF8A7060),
    outlineVariant = Color(0xFFC9BBAA),
    primaryLight = Color(0xFFB8460A),
)

val LocalRetroPalette = staticCompositionLocalOf { DarkRetroPalette }
