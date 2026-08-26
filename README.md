# ¿Unas Rolitas?

Reproductor de música Android nativo.

## Plataforma

- Android nativo
- Kotlin
- Jetpack Compose
- Media3 / ExoPlayer
- MediaSession
- PlaybackService
- Gradle

## Arquitectura

La aplicación está organizada en capas:

- `data/` — modelos y repositorios.
- `player/` — motor de reproducción.
- `service/` — reproducción en segundo plano y MediaSession.
- `audio/` — procesamiento y funciones de audio.
- `permissions/` — permisos Android.
- `navigation/` — navegación.
- `ui/` — componentes y pantallas Compose.
- `viewmodel/` — estado y lógica de presentación.
- `util/` — utilidades y logging.

## Reproducción

La reproducción utiliza Media3 / ExoPlayer y una MediaSession Android real.

Incluye:

- Reproducción en segundo plano.
- Notificación multimedia.
- Controles multimedia del sistema.
- Controles Bluetooth compatibles con MediaSession.
- Cola de reproducción.
- Reproducción anterior/siguiente.
- Pausa/reanudación.
- Barra de progreso.
- Repetición.
- Aleatorio.
- Ecualizador/DSP.

## Compilación

El proyecto se compila mediante Gradle.

La compilación de validación se realiza mediante GitHub Actions.

## Estado

La rama de desarrollo actual es:

`desarrollo-estabilizacion`

El objetivo de esta rama es estabilizar primero la arquitectura Android y posteriormente implementar la experiencia visual y las funciones definidas para ¿Unas Rolitas?.
