# ¿Unas Rolitas?

Aplicación nativa de música para Android.

## Proyecto

¿Unas Rolitas? es un reproductor de música nativo para Android que trabaja con la música disponible en el dispositivo.

La aplicación utiliza:

- Kotlin
- Jetpack Compose
- Android MediaStore
- Android Media3 / ExoPlayer
- Coroutines
- DataStore Preferences

## Biblioteca

La biblioteca utiliza el contenido real disponible en el dispositivo.

Las categorías de la biblioteca representan la información real de las canciones y no generan listas de reproducción ficticias.

## Listas de reproducción

Las listas de reproducción son administradas por la aplicación.

Se pueden:

- Crear listas de reproducción.
- Agregar canciones.
- Quitar canciones.
- Renombrar listas.
- Eliminar listas.
- Importar playlists externas compatibles.

Las playlists no se generan automáticamente a partir de álbumes, artistas, géneros, carpetas u otras agrupaciones de la biblioteca.

## Reproducción

La reproducción utiliza Android Media3 / ExoPlayer y un servicio de reproducción en primer plano para mantener el control de reproducción cuando la aplicación pasa a segundo plano.

## Compilación

Para generar el APK debug:

    ./gradlew assembleDebug

El APK generado se encuentra en:

    app/build/outputs/apk/debug/app-debug.apk

## GitHub Actions

El proyecto utiliza GitHub Actions para compilar automáticamente el APK debug cuando se realiza un push sobre la rama principal `main`.

También puede ejecutarse manualmente mediante `workflow_dispatch`.

El workflow genera un APK debug estándar y lo publica como artefacto `unas-rolitas-debug`.

## Estado del proyecto

La rama principal del proyecto es `main`.

El proyecto está enfocado exclusivamente en la aplicación Android nativa.

No se utiliza ningún entorno de web preview como parte de la aplicación Android.
