package com.example.ofmen.screens



import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun PostCropScreen(
    imageUri: Uri,
    onImageCropped: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cropSizePx = with(density) { 300.dp.toPx() }  // square crop for posts

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

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (originalBitmap == null) {
            CircularProgressIndicator()
        } else {
            val bmp = originalBitmap!!
            val baseScale = remember(bmp) {
                min(cropSizePx / bmp.width.toFloat(), cropSizePx / bmp.height.toFloat())
            }

            Canvas(
                modifier = Modifier
                    .size(300.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            userScale = (userScale * zoom).coerceIn(0.5f, 5f)
                            userOffset += pan
                        }
                    }
            ) {
                val totalScale = baseScale * userScale
                val dstW = bmp.width * totalScale
                val dstH = bmp.height * totalScale
                val tx = (size.width - dstW) / 2f + userOffset.x
                val ty = (size.height - dstH) / 2f + userOffset.y
                val dstRect = RectF(tx, ty, tx + dstW, ty + dstH)

                drawIntoCanvas { canvas ->
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                    // --- Step 1: dark overlay ---
                    val overlayPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#AA000000") // semi-transparent black
                        style = Paint.Style.FILL
                    }
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, overlayPaint)

                    // --- Step 2: clear the crop square (destination rect area) ---
                    val clearPaint = Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    }
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, clearPaint)

                    // --- Step 3: draw the image ---
                    canvas.nativeCanvas.drawBitmap(bmp, null, dstRect, paint)
                }

                // --- Step 4: white border for crop frame ---
                drawIntoCanvas { canvas ->
                    val strokePaint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 4f * density.density
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, strokePaint)
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    coroutineScope.launch {
                        val cropped = performPostCrop(
                            context, originalBitmap!!, cropSizePx.roundToInt(),
                            baseScale, userScale, userOffset
                        )
                        cropped?.let { onImageCropped(it) }
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}

suspend fun performPostCrop(
    context: Context,
    bitmap: Bitmap,
    cropPx: Int,
    baseScale: Float,
    userScale: Float,
    userOffset: Offset
): Uri? = withContext(Dispatchers.IO) {
    try {
        val totalScale = baseScale * userScale
        val dstW = bitmap.width * totalScale
        val dstH = bitmap.height * totalScale
        val tx = (cropPx - dstW) / 2f + userOffset.x
        val ty = (cropPx - dstH) / 2f + userOffset.y

        // map center
        val left = ((0 - tx) / totalScale).roundToInt().coerceAtLeast(0)
        val top = ((0 - ty) / totalScale).roundToInt().coerceAtLeast(0)
        val right = ((cropPx - tx) / totalScale).roundToInt().coerceAtMost(bitmap.width)
        val bottom = ((cropPx - ty) / totalScale).roundToInt().coerceAtMost(bitmap.height)

        val src = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
        val scaled = Bitmap.createScaledBitmap(src, cropPx, cropPx, true)

        val outputFile = File(context.cacheDir, "post_crop_${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile).use { fos ->
            scaled.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        }
        Uri.fromFile(outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
