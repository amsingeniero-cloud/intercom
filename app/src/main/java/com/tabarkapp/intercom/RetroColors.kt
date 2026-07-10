package com.tabarkapp.intercom

import androidx.compose.ui.graphics.Color

/**
 * Colores de marca / semanticos: NO cambian entre tema Claro y Oscuro
 * (un LCD siempre es verde, un LED siempre es rojo/verde, etc).
 * Los colores de "chasis" (neutros, texto) que si cambian con el tema
 * viven en RetroPalette / LocalRetroPalette.
 */
object RetroColors {
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

    // Interruptor de canal en "solo" (mantener pulsado): unico canal activo mientras dura
    val SoloBlue = Color(0xFF2563EB)
}
