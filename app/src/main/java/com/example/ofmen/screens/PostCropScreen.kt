package com.example.ofmen.screens

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

enum class CropAspect(val widthRatio: Int, val heightRatio: Int) {
    Square(1, 1),
    Portrait(4, 5),
    Landscape(16, 9)
}

@Composable
fun PostCropScreen(
    imageUri: Uri,
    onImageCropped: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                originalBitmap = BitmapFactory.decodeStream(stream)
            }
        }
    }

    var userScale by remember { mutableStateOf(1f) }
    var userOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedAspect by remember { mutableStateOf(CropAspect.Square) }

    // store latest rects used in Canvas
    var lastCropRect by remember { mutableStateOf<RectF?>(null) }
    var lastDstRect by remember { mutableStateOf<RectF?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (originalBitmap == null) {
            CircularProgressIndicator()
        } else {
            val bmp = originalBitmap!!

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            userScale = (userScale * zoom).coerceIn(0.5f, 5f)
                            userOffset += pan
                        }
                    }
            ) {
                // Fit image to screen base scale
                val baseScale = min(size.width / bmp.width, size.height / bmp.height)
                val totalScale = baseScale * userScale
                val dstW = bmp.width * totalScale
                val dstH = bmp.height * totalScale
                val tx = (size.width - dstW) / 2f + userOffset.x
                val ty = (size.height - dstH) / 2f + userOffset.y
                val dstRect = RectF(tx, ty, tx + dstW, ty + dstH)

                // --- central crop rect with selected aspect ---
                val cropWidth = size.width * 0.9f
                val cropHeight = when (selectedAspect) {
                    CropAspect.Square -> cropWidth
                    CropAspect.Portrait -> cropWidth * 1.25f
                    CropAspect.Landscape -> cropWidth * 0.5625f
                }
                val cropLeft = (size.width - cropWidth) / 2f
                val cropTop = (size.height - cropHeight) / 2f
                val cropRect = RectF(cropLeft, cropTop, cropLeft + cropWidth, cropTop + cropHeight)

                // Save rects for cropping
                lastCropRect = cropRect
                lastDstRect = dstRect

                drawIntoCanvas { canvas ->
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                    // dark overlay
                    val overlayPaint = Paint().apply {
                        color = Color.parseColor("#AA000000")
                        style = Paint.Style.FILL
                    }
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, overlayPaint)

                    // clear crop rect
                    val clearPaint = Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    }
                    canvas.nativeCanvas.drawRect(cropRect, clearPaint)

                    // draw image behind (shows through crop area)
                    canvas.nativeCanvas.drawBitmap(bmp, null, dstRect, paint)
                }

                // white border
                drawIntoCanvas { canvas ->
                    val strokePaint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 4f * density.density
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawRect(cropRect, strokePaint)
                }
            }

            // --- Bottom Controls ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Aspect ratio buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { selectedAspect = CropAspect.Square }) { Text("1:1") }
                    Button(onClick = { selectedAspect = CropAspect.Portrait }) { Text("4:5") }
                    Button(onClick = { selectedAspect = CropAspect.Landscape }) { Text("16:9") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save + Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onCancel) { Text("Cancel") }
                    Button(onClick = {
                        coroutineScope.launch {
                            if (lastCropRect != null && lastDstRect != null) {
                                val cropped = performAspectCrop(
                                    context, bmp, lastCropRect!!, lastDstRect!!
                                )
                                cropped?.let { onImageCropped(it) }
                            }
                        }
                    }) { Text("Save") }
                }
            }
        }
    }
}

suspend fun performAspectCrop(
    context: Context,
    bitmap: Bitmap,
    cropRect: RectF,
    dstRect: RectF
): Uri? = withContext(Dispatchers.IO) {
    try {
        // Map cropRect (screen coords) into bitmap coords
        val scaleX = bitmap.width / dstRect.width()
        val scaleY = bitmap.height / dstRect.height()

        val cropLeft = ((cropRect.left - dstRect.left) * scaleX).roundToInt().coerceAtLeast(0)
        val cropTop = ((cropRect.top - dstRect.top) * scaleY).roundToInt().coerceAtLeast(0)
        val cropRight = ((cropRect.right - dstRect.left) * scaleX).roundToInt().coerceAtMost(bitmap.width)
        val cropBottom = ((cropRect.bottom - dstRect.top) * scaleY).roundToInt().coerceAtMost(bitmap.height)

        val src = Bitmap.createBitmap(
            bitmap,
            cropLeft,
            cropTop,
            (cropRight - cropLeft).coerceAtLeast(1),
            (cropBottom - cropTop).coerceAtLeast(1)
        )

        val outputFile = File(context.cacheDir, "post_crop_${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile).use { fos ->
            src.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        Uri.fromFile(outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
