package com.unasrolitas.app.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.unasrolitas.app.ui.theme.*

@Composable
fun PermissionHandler(
    onPermissionGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var isGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
        if (granted) {
            onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (isGranted) {
            onPermissionGranted()
        }
    }

    if (isGranted) {
        content()
    } else {
        Surface(
            color = DarkCanvas,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(OrangePrimary.copy(alpha = 0.2f), RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Permiso de Audio Requerido",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "«¿Unas Rolitas?» necesita acceso a tus archivos de música para escanear y reproducir tus canciones almacenadas localmente.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { launcher.launch(requiredPermission) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Conceder Permiso de Almacenamiento", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
