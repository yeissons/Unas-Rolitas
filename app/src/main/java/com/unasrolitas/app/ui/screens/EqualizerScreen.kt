package com.unasrolitas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasrolitas.app.data.model.AudioSettings
import com.unasrolitas.app.data.model.EqualizerProfile
import com.unasrolitas.app.ui.theme.*

@Composable
fun EqualizerScreen(
    audioSettings: AudioSettings,
    presets: List<EqualizerProfile>,
    onBack: () -> Unit,
    onToggleEqualizer: (Boolean) -> Unit,
    onSelectPreset: (EqualizerProfile) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Ecualizador DSP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Motor AudioEffect Nativo",
                        fontSize = 12.sp,
                        color = OrangePrimary
                    )
                }
            }

            // Master Switch
            Switch(
                checked = audioSettings.isEqualizerEnabled,
                onCheckedChange = onToggleEqualizer,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OrangePrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Chips
        Text(
            text = "Presets Optimizados",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { profile ->
                val isSelected = audioSettings.activePreset == profile.name
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) OrangePrimary else DarkCard)
                        .clickable { onSelectPreset(profile) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = profile.name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5-Band Equalizer Sliders
        val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
        Surface(
            color = DarkCard,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ganancias de Banda (dB)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                bandLabels.forEachIndexed { index, label ->
                    val currentGain = audioSettings.bandGains.getOrElse(index) { 0f }
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = String.format("%+.1f dB", currentGain),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                        }
                        Slider(
                            value = currentGain,
                            onValueChange = { onBandGainChange(index, it) },
                            valueRange = -10f..10f,
                            enabled = audioSettings.isEqualizerEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = OrangePrimary,
                                activeTrackColor = OrangePrimary,
                                inactiveTrackColor = DividerColor
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bass Boost & Virtualizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bass Boost
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Bass Boost",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(audioSettings.bassBoost / 10)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                    Slider(
                        value = audioSettings.bassBoost.toFloat(),
                        onValueChange = { onBassBoostChange(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = audioSettings.isEqualizerEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = OrangePrimary,
                            activeTrackColor = OrangePrimary
                        )
                    )
                }
            }

            // Virtualizer
            Surface(
                color = DarkCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3D Surround",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(audioSettings.virtualizer / 10)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                    Slider(
                        value = audioSettings.virtualizer.toFloat(),
                        onValueChange = { onVirtualizerChange(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = audioSettings.isEqualizerEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberAccent,
                            activeTrackColor = AmberAccent
                        )
                    )
                }
            }
        }
    }
}
