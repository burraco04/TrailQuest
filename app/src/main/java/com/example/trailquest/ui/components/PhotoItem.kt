package com.example.trailquest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import com.example.trailquest.ui.utils.decodeSampledBitmapFromFile
import java.io.File

@Composable
fun PhotoItem(photoPath: String) {
    val bitmap = remember(photoPath) {
        if (photoPath.isNotBlank()) {
            val file = File(photoPath)
            if (file.exists()) {
                decodeSampledBitmapFromFile(file.absolutePath, 300, 300)
            } else null
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto Utente",
            modifier = Modifier
                .size(120.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
        }
    }
}







