package com.tvcostabrava.intercom

/** Los 5 canales/departamentos, compartidos entre la pantalla principal y Ajustes. */
data class ChannelDef(val id: String, val label: String)

val DEPARTMENT_CHANNELS = listOf(
    ChannelDef("realizacion", "REALIZACIÓN"),
    ChannelDef("camaras", "CÁMARAS"),
    ChannelDef("produccion", "PRODUCCIÓN"),
    ChannelDef("tecnica", "TÉCNICA"),
    ChannelDef("periodistas", "PERIODISTAS"),
)

const val TODOS_ID = "__todos__"
