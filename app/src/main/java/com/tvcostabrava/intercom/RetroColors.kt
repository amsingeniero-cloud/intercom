package com.tvcostabrava.intercom

import androidx.compose.ui.graphics.Color

/**
 * Paleta "Signal-84" calcada del HTML/CSS real del mockup de Stitch
 * (Radio Unit-82 / Walkie-Talkie Radio Alicante Libre).
 */
object RetroColors {
    // Fondo de pagina detras del chasis (body { bg-background })
    val Background = Color(0xFF131313)

    // Fondos del chasis
    val SurfaceContainerLowest = Color(0xFF0E0E0E)
    val SurfaceContainerLow = Color(0xFF1C1B1B)
    val SurfaceContainer = Color(0xFF20201F)
    val SurfaceContainerHigh = Color(0xFF2A2A2A)
    val SurfaceContainerHighest = Color(0xFF353535)
    val SurfaceVariant = Color(0xFF353535)
    val SpeakerGrille = Color(0xFF1A1A1A)

    // Texto / iconos
    val OnSurface = Color(0xFFE5E2E1)
    val OnSurfaceVariant = Color(0xFFE4BEB1)
    val Outline = Color(0xFFAB897D)
    val OutlineVariant = Color(0xFF5B4137)

    // "primary" (salmon claro): titulo cabecera, iconos antena/bateria, texto LCD
    val PrimaryLight = Color(0xFFFFB59A)

    // "primary-container" (naranja vivo): solo la pestaña activa de la barra inferior
    val PrimaryContainer = Color(0xFFFF5C00)
    val OnPrimaryContainer = Color(0xFF521800)

    // Boton HABLAR: gradiente rojo (reposo) <-> verde (transmitiendo)
    val TalkIdleStart = Color(0xFFFF6B6B)
    val TalkIdleEnd = Color(0xFFB91C1C)
    val TalkIdleShadow = Color(0xFF7F1D1D)
    val TalkActiveStart = Color(0xFF4ADE80)
    val TalkActiveEnd = Color(0xFF166534)
    val TalkActiveShadow = Color(0xFF052E16)

    // Boton MANOS LIBRES: gradiente amarillo/dorado
    val HandsFreeStart = Color(0xFFFACC15)
    val HandsFreeEnd = Color(0xFFCA8A04)
    val HandsFreeShadow = Color(0xFF854D0E)
    val OnHandsFree = Color(0xFF161E12)

    // LEDs TX / RX
    val TxRed = Color(0xFFDC2626)
    val RxGreen = Color(0xFF22C55E)

    // Pantalla LCD
    val LcdBackground = Color(0xFF2D3528)
}
