package com.example.trailquest.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

@Composable
fun CameraPreview(
    outputFile: File,
    onPhotoSaved: (File) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    var previewView by remember {
        mutableStateOf<PreviewView?>(null)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { ctx ->

                PreviewView(ctx).also { view ->

                    previewView = view

                    val cameraProviderFuture =
                        ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({

                        val cameraProvider =
                            cameraProviderFuture.get()

                        val preview =
                            Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(
                                        view.surfaceProvider
                                    )
                                }

                        val cameraSelector =
                            CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()

                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {

                val view = previewView ?: return@IconButton

                imageCapture.targetRotation =
                    view.display?.rotation
                        ?: Surface.ROTATION_0

                val outputOptions =
                    ImageCapture.OutputFileOptions.Builder(
                        outputFile
                    ).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {

                        override fun onImageSaved(
                            outputResults: ImageCapture.OutputFileResults
                        ) {

                            try {
                                fixImageRotation(outputFile)

                                onPhotoSaved(outputFile)

                            } catch (e: Exception) {
                                e.printStackTrace()

                                onPhotoSaved(outputFile)
                            }
                        }

                        override fun onError(
                            exception: ImageCaptureException
                        ) {
                            exception.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scatta e Salva",
                tint = Color.White
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Chiudi",
                tint = Color.White
            )
        }
    }
}

private fun fixImageRotation(file: File) {

    if (!file.exists()) return

    val exif = ExifInterface(file.absolutePath)

    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    if (orientation == ExifInterface.ORIENTATION_NORMAL) {
        return
    }

    val bitmap =
        BitmapFactory.decodeFile(file.absolutePath)
            ?: return

    val matrix = Matrix()

    when (orientation) {

        ExifInterface.ORIENTATION_ROTATE_90 -> {
            matrix.postRotate(90f)
        }

        ExifInterface.ORIENTATION_ROTATE_180 -> {
            matrix.postRotate(180f)
        }

        ExifInterface.ORIENTATION_ROTATE_270 -> {
            matrix.postRotate(270f)
        }

        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
            matrix.setScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            matrix.setScale(1f, -1f)
        }

        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(270f)
            matrix.postScale(-1f, 1f)
        }

        else -> {
            bitmap.recycle()
            return
        }
    }

    val rotatedBitmap = Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )

    bitmap.recycle()

    FileOutputStream(file).use { outputStream ->
        rotatedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            95,
            outputStream
        )
    }

    rotatedBitmap.recycle()

    val fixedExif = ExifInterface(file.absolutePath)

    fixedExif.setAttribute(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL.toString()
    )

    fixedExif.saveAttributes()
}