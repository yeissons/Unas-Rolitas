package com.unasrolitas.app.navigation

sealed class NavRoutes(val route: String, val title: String) {
    object Library : NavRoutes("library", "Biblioteca")
    object Player : NavRoutes("player", "Reproductor")
    object Queue : NavRoutes("queue", "Cola de Reproducción")
    object Lyrics : NavRoutes("lyrics", "Letras Sincronizadas")
    object Equalizer : NavRoutes("equalizer", "Ecualizador DSP")
    object AudioTools : NavRoutes("audio_tools", "Herramientas de Audio")
    object ControlCenter : NavRoutes("control_center", "Centro de Control")
}
