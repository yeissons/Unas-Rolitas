# Auditoría técnica de ¿Unas Rolitas?

## Estado de esta revisión

- ZIP de origen: `unasrolitas.zip`
- Proyecto extraído: 80 archivos, 37 directorios.
- Android nativo: módulo `app/` con Gradle Wrapper y workflow de GitHub Actions.
## Correcciones aplicadas durante esta revisión

2. Se eliminó la metadata técnica falsa por defecto del modelo Android `Song` (bitrate, sample rate y bit depth).
3. `MediaStoreRepository` dejó de depender de `MediaStore.Audio.Media.DATA` para construir la URI principal.
4. La consulta Android conserva únicamente metadata realmente disponible y evita inventar valores técnicos.
5. `CoverArtRepository` obtiene carátulas embebidas usando la `content://` URI real de MediaStore y mantiene caché local.
6. Se mejoró el cierre de conexiones HTTP de carátulas y búsquedas de letras.
7. El parser LRC acepta timestamps con uno, dos o tres dígitos de fracción y múltiples timestamps por línea.
8. La interfaz ya no muestra `320 kbps` cuando el bitrate real no fue obtenido.
9. `AudioDspManager` libera efectos anteriores antes de adjuntarse a una nueva sesión de audio.
10. Se añadió aplicación real del nivel de Virtualizer.
11. Se añadió temporizador de suspensión real en el reproductor; al vencer pausa el ExoPlayer.
12. El `gradlew` tenía una configuración de JVM que, en este entorno, se interpretaba literalmente y provocaba `ClassNotFoundException` para `"-Xmx64m"`; se corrigió esa línea.
13. El interruptor del ecualizador ahora activa/desactiva realmente los efectos DSP y se aplica también el estado de Virtualizer.

## Verificación realizada

- ZIP leído y extraído correctamente.
- Inventario completo realizado.
- Estructura Android/Web inspeccionada.
- Búsqueda de código simulado en Android realizada.
- Revisión de configuración Gradle y workflow CI realizada.
- Se intentó ejecutar `./gradlew --version` y `./gradlew :app:assembleDebug`.
- La ejecución no pudo completarse porque el `gradle/wrapper/gradle-wrapper.jar` incluido en el ZIP está corrupto: no es un JAR ZIP válido y no contiene `org.gradle.wrapper.GradleWrapperMain` utilizable.

## Bloqueo externo restante

Para confirmar una compilación Android real se necesita reemplazar/regenerar el `gradle-wrapper.jar` válido para Gradle 8.5 y después ejecutar:

```bash
./gradlew clean assembleDebug
```

Ese paso no se puede marcar como aprobado mientras el wrapper binario incluido siga corrupto.
