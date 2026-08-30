# ¿UNAS ROLITAS? — MEMORIA PERSISTENTE DEL PROYECTO

## Propósito

Esta carpeta contiene la memoria técnica persistente utilizada para mantener
el contexto y el hilo de desarrollo del proyecto ¿UNAS ROLITAS?.

## Reglas permanentes de trabajo

- La raíz local del proyecto es `~/unasrolitas-github`.
- Termux se utiliza exclusivamente para leer, editar y modificar código fuente
  y para sincronizar cambios mediante Git.
- Termux NO se utiliza para compilar, generar APKs ni ejecutar procesos de
  compilación del proyecto.
- Las compilaciones se realizan exclusivamente mediante GitHub Actions.
- Los comandos deben limitar la salida visible a un máximo de 25.000 caracteres.
- Si una salida puede superar ese límite, debe guardarse completa en un archivo
  y mostrarse solamente de forma resumida.
- Antes de realizar cambios importantes se debe consultar esta memoria y el
  estado real del código/Git.
- No asumir que un cambio está implementado solamente porque aparezca en el
  contexto de una conversación anterior: comprobarlo en el código y/o Git.

## Estado actual

### Playlists

La aplicación ya dispone de arquitectura para playlists mediante:

- `data/model/Playlist.kt`
- `data/repository/PreferencesRepository.kt`
- `data/repository/PlaylistFileRepository.kt`
- `viewmodel/MusicViewModel.kt`
- `ui/screens/LibraryScreen.kt`
- `MainActivity.kt`

Commits relacionados:

- `87ea589` — Fix playlist architecture and import flow
- `1f6d364` — Fix playlist persistence compilation

### Problemas actuales reportados

1. Las playlists se crean y muestran nombre/cantidad de canciones, pero al
   abrir una playlist aparentemente no aparecen sus canciones.
2. El menú de tres puntos de cada playlist tiene muy pocas opciones y debe
   incluir exportación.
3. La portada de cada playlist debe estar formada por un máximo de cuatro
   carátulas de canciones que pertenecen a esa playlist.
4. En la pantalla/listado de playlists no se quiere mostrar el encabezado
   "Listas de reproducción".
5. El botón Crear debe permanecer en la zona superior donde está actualmente.
6. El botón inferior "Importar listas" debe desaparecer y su función debe
   convertirse en "Importar y exportar listas" en la zona superior indicada.

### Estado actualizado — 2026-08-29

- Commit `ad82d0f` — `Fix playlist artwork mosaic`.
- El commit fue subido correctamente a `origin/main`.
- `origin/main` y `HEAD` apuntan a `ad82d0f`.
- `LibraryScreen.kt` ahora obtiene hasta cuatro canciones de la playlist que
  tengan `artworkUri`.
- Si la playlist tiene `coverUri`, se conserva esa portada personalizada.
- Si no tiene `coverUri`, se muestra un mosaico 2x2 con hasta cuatro carátulas.
- Se añadió el helper `PlaylistCoverTile`.
- Se ejecutó `git diff --cached --check` sin errores antes del commit.
- Los archivos `.backup/`, `.unasrolitas/` y `playlist_diagnostico.txt` no
  forman parte del commit `ad82d0f` y permanecen sin seguimiento.
- Pendiente: comprobar mediante GitHub Actions que el cambio compile
  correctamente. No compilar localmente en Termux.

## Procedimiento para cada tarea

1. Consultar esta memoria.
2. Inspeccionar el código real antes de modificarlo.
3. Identificar la causa concreta del problema.
4. Modificar únicamente lo necesario.
5. Revisar el diff.
6. Comprobar que no se introduzcan errores evidentes.
7. Registrar en esta memoria el cambio realizado y el commit.
8. Sincronizar mediante Git cuando corresponda.
9. Compilar exclusivamente mediante GitHub Actions.
10. Registrar el resultado de la compilación/prueba.

## Registro de cambios

### 2026-08-29 — Corrección de compilación: alcance de playlistForExport

La primera compilación de GitHub Actions después del commit `bd466a0` falló en `MainActivity.kt` porque `playlistForExport` era utilizada por `exportPlaylistLauncher` antes de que la variable estuviera declarada.

Corrección aplicada:
- Movida la declaración de `playlistForExport` para colocarla antes de `exportPlaylistLauncher`.
- Eliminada la declaración duplicada de su ubicación anterior.
- No se modificó la lógica de exportación M3U8.
- `git diff --check` pasó sin errores.
- La corrección queda pendiente de commit y push.
- La compilación de validación deberá ejecutarse exclusivamente mediante GitHub Actions.


### 2026-08-29 — Commit y push completados

- Commit creado: `bd466a0`
- Mensaje: `Add playlist export and multi-song management`
- Push realizado correctamente a `origin/main`.
- `HEAD`, `main` y `origin/main` apuntan a `bd466a0`.
- Los cambios de playlists quedaron publicados en GitHub.
- Los archivos `.backup/`, `.unasrolitas/`, `playlist_cambios.diff` y `playlist_diagnostico.txt` permanecen sin seguimiento y no fueron incluidos en el commit.
- No se realizó compilación local.
- Siguiente paso obligatorio: validar la compilación exclusivamente mediante GitHub Actions.


### 2026-08-29 — Cambios de playlists pendientes de commit

Se aplicaron y revisaron los cambios de la segunda fase de playlists:

- `PlaylistFileRepository.kt`
  - Añadida exportación de playlists a formato M3U8 mediante `writeM3u8()`.
  - La exportación genera `#EXTM3U`, entradas `#EXTINF` y las rutas de archivo de las canciones.
  - Corregido un error inicial para que los saltos de línea sean reales (`\n`) y no texto literal.

- `MusicViewModel.kt`
  - Añadido `exportPlaylistToUri()` para conectar la exportación con el repositorio.

- `LibraryScreen.kt`
  - El menú de playlist ahora incluye:
    - Añadir canciones
    - Exportar playlist
    - Renombrar
    - Eliminar
  - Añadido selector múltiple de canciones desde `MainActivity`.
  - La portada conserva el mosaico de hasta cuatro carátulas implementado anteriormente.
  - La apertura de una playlist reconstruye sus canciones desde `playlist.songIds` y la lista actual de canciones, evitando depender del filtro de búsqueda.

- `MainActivity.kt`
  - Añadido launcher de `CreateDocument` para exportar `.m3u8`.
  - Añadido diálogo para seleccionar múltiples canciones y agregarlas a una playlist.
  - Añadidos imports necesarios para `LazyColumn`, `items`, `clickable` y `TextAlign`.

Validaciones realizadas:
- `git diff --check` sin errores.
- El diff actual afecta únicamente a los cuatro archivos de código esperados.
- El diff tiene aproximadamente 20 KB y fue guardado temporalmente en `playlist_cambios.diff`.
- Todavía NO se ha realizado commit.
- Todavía NO se ha compilado localmente.
- La compilación deberá realizarse exclusivamente mediante GitHub Actions.

Archivos auxiliares sin seguimiento:
- `.backup/`
- `.unasrolitas/`
- `playlist_cambios.diff`
- `playlist_diagnostico.txt`

Estado: cambios de playlists listos para revisión final, staging y commit.


### 2026-08-29
- `ad82d0f` — `Fix playlist artwork mosaic`
- Estado remoto: `origin/main` sincronizado con `ad82d0f`.
- Validación previa al commit: `git diff --cached --check` sin errores.
- Próximo paso: compilación mediante GitHub Actions.


## CIERRE — Importación y exportación de playlists (2026-08-30)

### Decisiones definitivas
- La exportación individual desde la tarjeta de una playlist se conserva y genera un archivo `.m3u8`.
- El botón `Importar / Exportar` tiene su propio flujo de exportación mediante ZIP.
- En `Importar / Exportar → Exportar listas`, seleccionar UNA playlist también genera un ZIP con esa única playlist.
- En `Importar / Exportar → Exportar listas`, seleccionar DOS O MÁS playlists genera un ZIP con todas las seleccionadas.
- Por tanto, el flujo de `Importar / Exportar` SIEMPRE exporta a ZIP, independientemente de que se seleccione una o varias playlists.
- El botón `Importar` acepta playlists individuales `.m3u`, `.m3u8`, `.pls` y `.wpl`.
- El botón `Importar` también acepta archivos `.zip`.
- Un ZIP puede contener una o muchas playlists y el sistema intenta importar todas las playlists reconocibles que encuentre dentro.
- La detección de formato de playlist se reforzó usando extensión del URI, nombre visible del archivo y MIME type.

### Implementación actual
- `PlaylistFileRepository.kt`
  - `writeM3u8()` mantiene la exportación individual.
  - `writePlaylistsZip()` genera el ZIP para el flujo Importar/Exportar.
  - `readPlaylists()` gestiona importación individual o ZIP.
  - `readZipPlaylists()` busca `.m3u`, `.m3u8`, `.pls` y `.wpl` dentro del ZIP.
  - `detectFormat()` reconoce formatos por extensión, nombre y MIME.
- `MusicViewModel.kt`
  - `importPlaylists()` incorpora una o varias playlists importadas.
  - `exportPlaylistToUri()` conserva la exportación individual.
  - `exportPlaylistsToUri()` exporta las seleccionadas mediante ZIP.
- `MainActivity.kt`
  - Importación acepta ZIP además de los formatos de playlist existentes.
  - El launcher de exportación individual usa `.m3u8`.
  - El launcher de `Importar / Exportar → Exportar listas` usa `application/zip`.
- `LibraryScreen.kt`
  - El selector de exportación permite seleccionar una o varias playlists.
  - El resultado de ese selector siempre pasa al exportador ZIP.

### Estado de cierre
- `git diff --check`: limpio.
- Los cuatro archivos de código relacionados con esta corrección están preparados/staged:
  - `MainActivity.kt`
  - `PlaylistFileRepository.kt`
  - `LibraryScreen.kt`
  - `MusicViewModel.kt`
- No compilar en Termux.
- La compilación debe hacerse exclusivamente mediante GitHub Actions.
- Próximo paso: commit y push de estos cambios, seguido de compilación mediante GitHub Actions y pruebas del APK.

### Regla de trabajo
Esta decisión queda CERRADA y no debe volver a plantearse ni investigarse salvo que el usuario solicite explícitamente cambiarla.
