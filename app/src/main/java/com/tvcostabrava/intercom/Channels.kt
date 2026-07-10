package com.tvcostabrava.intercom

/**
 * Los 5 canales/departamentos, compartidos entre la pantalla principal y Ajustes.
 * [label] es el nombre completo (Ajustes > Rol, hay sitio de sobra). [shortLabel] es
 * la version abreviada que cabe en una sola linea bajo cada interruptor de la pantalla
 * principal (muy poco ancho: 6 interruptores en una fila).
 */
data class ChannelDef(val id: String, val label: String, val shortLabel: String = label)

val DEPARTMENT_CHANNELS = listOf(
    ChannelDef("realizacion", "REALIZACIÓN", "REALIZA"),
    ChannelDef("camaras", "CÁMARAS"),
    ChannelDef("produccion", "PRODUCCIÓN", "PRODUC."),
    ChannelDef("tecnica", "TÉCNICA"),
    ChannelDef("periodistas", "PERIODISTAS", "PRENSA"),
)

const val TODOS_ID = "__todos__"
