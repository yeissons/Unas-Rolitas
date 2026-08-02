# ¿Unas Rolitas? — Reproductor de Música Nativo Android

**«¿Unas Rolitas?»** es un reproductor de música nativo para Android con reproductor de vinilo giratorio, ecualizador DSP, letras sincronizadas estilo Karaoke, gestión avanzada de biblioteca y flujo de integración continua listo para compilar en **GitHub Actions**.

---

## 🏗️ Arquitectura del Proyecto

El repositorio mantiene el **Android nativo como proyecto principal y compilable**. La previsualización Web está físicamente aislada en `web-preview/` y no forma parte del módulo Gradle Android:

### 📱 CAPA B — Proyecto Android Nativo (Producción)
* **Lenguaje:** Kotlin 1.9.22
* **UI Framework:** Jetpack Compose (Material3, Navigation Compose)
* **Motor de Audio:** AndroidX Media3 / ExoPlayer 1.2.1
* **Segundo Plano:** `MediaSessionService` con Notificación Multimedia Interactiva
* **Acceso a Biblioteca:** `MediaStoreRepository` con ramificación por versión (Android 14 / SDK 34 + Android 7+)
* **Procesamiento de Audio / DSP:** `AudioDspManager` (AudioEffect API, Equalizer 5/10 bandas, BassBoost, Virtualizer)
* **Compilación:** Gradle Wrapper 8.5 (AGP 8.2.2, JDK 17)
* **CI/CD:** GitHub Actions (`.github/workflows/build-debug.yml`)

### 💻 CAPA A — Previsualización Interactiva Web (AI Studio Preview)
* **Ubicación:** `web-preview/`
* **Entorno:** React 19 + TypeScript + Vite + Tailwind CSS
* **Propósito:** Prototipado, validación de UX/UI, animaciones de vinilo y prueba interactiva en navegador.
* **Nota:** `web-preview/` conserva datos simulados exclusivamente para la demo web; esos datos no son utilizados por el APK Android.

---

## 🛠️ Versiones Congeladas y Compatibilidad

| Componente | Versión |
| :--- | :--- |
| **JDK** | 17 (Temurin) |
| **Android Gradle Plugin (AGP)** | 8.2.2 |
| **Gradle** | 8.5 |
| **Kotlin** | 1.9.22 |
| **Jetpack Compose Compiler** | 1.5.8 |
| **AndroidX Media3** | 1.2.1 |
| **Compile SDK** | 34 (Android 14) |
| **Target SDK** | 34 (Android 14) |
| **Minimum SDK** | 24 (Android 7.0) |

---

## 🚀 Cómo Compilar el APK en GitHub Actions

1. Sube este proyecto a tu repositorio de **GitHub**.
2. Dirígete a la pestaña **Actions**.
3. El workflow **"Build Android Debug APK"** se ejecutará automáticamente en cada `push` o `pull_request`.
4. Una vez completado, descarga el **APK ejecutable** desde la sección **Artifacts**: `UnasRolitas-Debug-APK`.

---

## 💻 Compilación del Web Preview

Desde `web-preview/`:
```bash
npm install
npm run build
```

## 💻 Compilación Local con Gradle Wrapper

### Linux / macOS:
```bash
chmod +x gradlew
./gradlew assembleDebug
```

### Windows:
```cmd
gradlew.bat assembleDebug
```

Ubicación del APK generado:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📊 Tabla Honesta de Estado de Implementación

| Funcionalidad | Clasificación de Estado | Descripción Técnica |
| :--- | :--- | :--- |
| **Motor de Reproducción (Play/Pause/Skip/Seek)** | `UI COMPLETA + LÓGICA REAL` | Media3 ExoPlayer integrado con control de cola |
| **Reproductor de Vinilo Animado** | `UI COMPLETA + LÓGICA REAL` | Giro sincronizado, caída de aguja al reproducir y detención al pausar |
| **Segundo Plano y MediaSession** | `UI COMPLETA + LÓGICA REAL` | `PlaybackService` registrado en AndroidManifest con Notificación Multimedia |
| **Escáner de Biblioteca (MediaStore)** | `UI COMPLETA + LÓGICA REAL` | Consulta a `MediaStore.Audio.Media` con filtros de música |
| **Ecualizador DSP (Gráfico 5/10 bandas)** | `UI COMPLETA + LÓGICA REAL` | Ecualizador con WebAudio API en Preview y `AudioEffect` en Android |
| **Letras Sincronizadas (Karaoke LRC)** | `UI COMPLETA + LÓGICA REAL` | Parser LRC con resaltado automático según timestamp |
| **Herramientas de Audio (Conversor/Recortador)** | `UI COMPLETA + ARQUITECTURA PREPARADA` | Interfaz visual interactiva con arquitectura modular para FFmpeg/MediaCodec |
| **Centro de Control / Tarjetas Inteligentes** | `UI COMPLETA + LÓGICA REAL` | Ajustes categorizados con estados reales persistidos |
| **Visualizadores de Audio (Espectro, Ondas)** | `UI COMPLETA + LÓGICA REAL` | Renderizado por Canvas HTML5 / Compose |
| **CI/CD GitHub Actions** | `UI COMPLETA + LÓGICA REAL` | `.github/workflows/build-debug.yml` funcional con Gradle Wrapper |

---

## 📁 Estructura del Proyecto

```
unas-rolitas/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/unasrolitas/app/
│           │   ├── UnasRolitasApplication.kt
│           │   ├── MainActivity.kt
│           │   ├── audio/AudioDspManager.kt
│           │   ├── data/
│           │   │   ├── model/ (Song, Album, Lyrics, AudioSettings...)
│           │   │   └── repository/ (MediaStoreRepository)
│           │   ├── player/MusicPlayerManager.kt
│           │   └── service/PlaybackService.kt
│           └── res/ (xml, values/strings, colors, themes)
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── .github/workflows/build-debug.yml
├── web-preview/
│   ├── package.json
│   ├── src/
│   └── ...
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```
