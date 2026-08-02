package com.unasrolitas.app.ui.screens

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasrolitas.app.ui.theme.*

@Composable
fun AudioToolsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    val nativeSampleRate = remember {
        audioManager?.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) ?: "44100"
    }
    val nativeFramesPerBuffer = remember {
        audioManager?.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER) ?: "256"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(16.dp)
    ) {
        // Top Header
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
                    text = "Diagnóstico y Herramientas de Audio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Información técnica del subsistema Android",
                    fontSize = 12.sp,
                    color = AmberAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Motor de Audio (Media3 / ExoPlayer)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        AudioInfoRow("Frecuencia Muestra Nativa", "$nativeSampleRate Hz")
                        AudioInfoRow("Tamaño de Buffer Frame", "$nativeFramesPerBuffer frames")
                        AudioInfoRow("Renderizador de Audio", "ExoPlayer AudioTrack")
                        AudioInfoRow("Efectos DSP Hardware", "Habilitados (Equalizer / BassBoost)")
                        AudioInfoRow("Modo Sincronización LRC", "Precisión milisegundos")
                    }
                }
            }

            item {
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Códecs y Formatos Soportados",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        AudioInfoRow("MP3 (MPEG Audio Layer 3)", "Soportado nativamente")
                        AudioInfoRow("AAC (Advanced Audio Coding)", "Soportado (LC / HE)")
                        AudioInfoRow("FLAC (Free Lossless)", "Soportado (Hasta 24-bit/192kHz)")
                        AudioInfoRow("WAV / PCM", "Soportado (Uncompressed)")
                        AudioInfoRow("OGG / Vorbis / Opus", "Soportado nativamente")
                    }
                }
            }
        }
    }
}

@Composable
fun AudioInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}
