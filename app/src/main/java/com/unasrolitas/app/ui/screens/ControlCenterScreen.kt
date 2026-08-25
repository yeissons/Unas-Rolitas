package com.unasrolitas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasrolitas.app.ui.theme.*

@Composable
fun ControlCenterScreen(
    onBack: () -> Unit,
    onExportLog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DarkCard)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Centro de Control",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Ajustes del Sistema Android Nativo",
                    fontSize = 12.sp,
                    color = OrangePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingCard(
                    icon = Icons.Default.BugReport,
                    title = "Registro de Diagnóstico / Crash Log",
                    subtitle = "Exporta el registro interno de la aplicación para diagnosticar cierres inesperados, errores del reproductor y problemas del DSP.",
                    badge = "Exportar"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onExportLog,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar registro de diagnóstico")
                }
            }

            item {
                SettingCard(
                    icon = Icons.Default.Folder,
                    title = "1. Escáner de Almacenamiento & MediaStore",
                    subtitle = "Configura el filtro de audios cortos (<30s), carpetas ignoradas y re-escaneo automático en segundo plano.",
                    badge = "Android 14 Ready"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.PlayCircle,
                    title = "2. Motor de Reproducción Media3 / ExoPlayer",
                    subtitle = "Ajustes de reproducción sin pausas (Gapless), fundido cruzado (Crossfade), y tamaño de búfer de streaming.",
                    badge = "ExoPlayer 1.2.1"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.GraphicEq,
                    title = "3. Ecualizador & AudioEffect DSP",
                    subtitle = "Gestión de procesador de audio nativo, ganancia por banda, Bass Boost y sonorización 3D Virtualizer.",
                    badge = "Efectos DSP"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Palette,
                    title = "4. Apariencia & Tema Vinilo Oscuro",
                    subtitle = "Personaliza colores de acento (Naranja Cumbia, Ámbar, Rojo Fuego) y animaciones del disco giratorio.",
                    badge = "Jetpack Compose"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Equalizer,
                    title = "5. Visualizador de Espectro de Audio",
                    subtitle = "Ajusta la sensibilidad del análisis FFT de frecuencia y los modos de ondas o barras de neón.",
                    badge = "Animado"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Subtitles,
                    title = "6. Letras & Karaoke LRC Sincronizado",
                    subtitle = "Fuentes de letras en línea, auto-sincronización con desfase de tiempo LRC y traducción instantánea.",
                    badge = "LRC Sync"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Edit,
                    title = "7. Editor de Etiquetas ID3 y Carátulas",
                    subtitle = "Edita título, artista, álbum, año y asigna carátulas directamente en el archivo de audio.",
                    badge = "ID3v2"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Build,
                    title = "8. Herramientas de Conversión & Normalización",
                    subtitle = "Convierte audios a MP3/FLAC, recorta tonos de llamada y aplica normalización de volumen LUFS.",
                    badge = "Herramientas"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Backup,
                    title = "9. Copia de Seguridad de Listas",
                    subtitle = "Exporta e importa tus listas de reproducción, canciones favoritas y ajustes en formato JSON.",
                    badge = "Backup JSON"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Bluetooth,
                    title = "10. Integración MediaSession & Controles Externos",
                    subtitle = "Soporte para audífonos Bluetooth, Android Auto, notificaciones en barra de estado y pantalla de bloqueo.",
                    badge = "MediaSession"
                )
            }

            item {
                SettingCard(
                    icon = Icons.Default.Info,
                    title = "11. Acerca de «¿Unas Rolitas?»",
                    subtitle = "Versión 1.0.0 Nativa Android. Desarrollada con Kotlin, Jetpack Compose y arquitectura limpia MVVM.",
                    badge = "v1.0.0 Native"
                )
            }
        }
    }
}

@Composable
fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String
) {
    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
