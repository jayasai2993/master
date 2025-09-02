package com.example.ofmen.screens

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ProfileCropScreen(
    imageUri: Uri,
    onImageCropped: (Uri) -> Unit,
    onCancel: () -> Unit,
    cropDiameterDp: Dp = 250.dp,            // size of circular preview/crop area
    minScale: Float = 0.5f,
    maxScale: Float = 5f
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cropDiameterPx = with(density) { cropDiameterDp.toPx() }

    // load the original bitmap once (off main thread)
    var originalBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(imageUri).use { stream ->
                    originalBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                originalBitmap = null
            }
        }
    }

    // user-controlled transforms
    var userScale by remember { mutableStateOf(1f) }
    var userOffset by remember { mutableStateOf(Offset.Zero) }

    // clamp helper
    fun clampScale(s: Float) = min(maxScale, max(minScale, s))

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (originalBitmap == null) {
            // loading indicator while bitmap loads
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        } else {
            // remember baseScale once per image
            val bmp = originalBitmap!!
            val baseScale = remember(bmp) {
                min(cropDiameterPx / bmp.width.toFloat(), cropDiameterPx / bmp.height.toFloat())
            }
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .size(cropDiameterDp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            userScale = clampScale(userScale * zoom)
                            userOffset += pan
                        }
                    }
            ) {
                val totalScale = baseScale * userScale

                val dstW = bmp.width * totalScale
                val dstH = bmp.height * totalScale

                val tx = (size.width - dstW) / 2f + userOffset.x
                val ty = (size.height - dstH) / 2f + userOffset.y

                val dstRect = android.graphics.RectF(tx, ty, tx + dstW, ty + dstH)

                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

                    // Clip circle
                    val path = android.graphics.Path().apply {
                        addCircle(size.width / 2f, size.height / 2f, size.minDimension / 2f, android.graphics.Path.Direction.CW)
                    }
                    canvas.nativeCanvas.clipPath(path)

                    // Draw bitmap with scale+pan
                    canvas.nativeCanvas.drawBitmap(bmp, null, dstRect, paint)
                }

                // Border
                drawIntoCanvas { canvas ->
                    val strokePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 4f * density.density
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawCircle(size.width / 2f, size.height / 2f, size.minDimension / 2f, strokePaint)
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(onClick = {
                    // perform crop on IO thread and return uri
                    coroutineScope.launch {
                        val out = performCrop(
                            context = context,
                            bitmap = originalBitmap!!,
                            cropPx = cropDiameterPx.roundToInt(),
                            baseScale = min(cropDiameterPx / originalBitmap!!.width.toFloat(), cropDiameterPx / originalBitmap!!.height.toFloat()),
                            userScale = userScale,
                            userOffset = userOffset
                        )
                        if (out != null) {
                            onImageCropped(out)
                        }
                    }
                }) {
                    Text("Save")
                }
            }
        }
    }
}

/**
 * Performs the precise crop:
 * - maps the circular area in view coords into the source bitmap coordinates using baseScale & transforms,
 * - extracts the correct source rectangle from the original bitmap,
 * - scales it to the final size (cropPx x cropPx),
 * - applies a circular mask (transparent outside),
 * - writes PNG to cache and returns Uri.
 */
suspend fun performCrop(
    context: Context,
    bitmap: android.graphics.Bitmap,
    cropPx: Int,
    baseScale: Float,
    userScale: Float,
    userOffset: Offset
): Uri? = withContext(Dispatchers.IO) {
    try {
        val totalScale = baseScale * userScale

        // destination size of scaled bitmap in view coords
        val dstW = bitmap.width * totalScale
        val dstH = bitmap.height * totalScale

        // top-left where the bitmap is drawn in view coordinates
        val tx = (cropPx - dstW) / 2.0f + userOffset.x
        val ty = (cropPx - dstH) / 2.0f + userOffset.y

        // center of the crop circle in view coords (crop view center)
        val cxView = cropPx / 2.0f
        val cyView = cropPx / 2.0f

        // map view coords to bitmap coords: bmpX = (viewX - tx) / totalScale
        val cxBmp = (cxView - tx) / totalScale
        val cyBmp = (cyView - ty) / totalScale

        // size in bitmap pixels that corresponds to cropPx in view coords
        val cropSizeBmpF = cropPx / totalScale
        val half = cropSizeBmpF / 2.0f

        // source rectangle in bitmap coordinates
        var leftF = cxBmp - half
        var topF = cyBmp - half
        var rightF = cxBmp + half
        var bottomF = cyBmp + half

        // clamp to bitmap bounds
        if (leftF < 0f) {
            rightF -= leftF
            leftF = 0f
        }
        if (topF < 0f) {
            bottomF -= topF
            topF = 0f
        }
        if (rightF > bitmap.width) {
            val diff = rightF - bitmap.width
            leftF -= diff
            rightF = bitmap.width.toFloat()
            if (leftF < 0f) leftF = 0f
        }
        if (bottomF > bitmap.height) {
            val diff = bottomF - bitmap.height
            topF -= diff
            bottomF = bitmap.height.toFloat()
            if (topF < 0f) topF = 0f
        }

        val srcLeft = leftF.roundToInt().coerceIn(0, bitmap.width - 1)
        val srcTop = topF.roundToInt().coerceIn(0, bitmap.height - 1)
        val srcW = (rightF - leftF).roundToInt().coerceAtLeast(1).coerceAtMost(bitmap.width - srcLeft)
        val srcH = (bottomF - topF).roundToInt().coerceAtLeast(1).coerceAtMost(bitmap.height - srcTop)

        // extract the source rectangle
        val srcBitmap = android.graphics.Bitmap.createBitmap(bitmap, srcLeft, srcTop, srcW, srcH)

        // scale the extracted bitmap to desired pixel size (cropPx x cropPx)
        val scaled = android.graphics.Bitmap.createScaledBitmap(srcBitmap, cropPx, cropPx, true)

        // create output bitmap with transparency and circular mask
        val output = android.graphics.Bitmap.createBitmap(cropPx, cropPx, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawARGB(0, 0, 0, 0) // clear

        val path = Path().apply {
            addCircle(cropPx / 2.0f, cropPx / 2.0f, cropPx / 2.0f, Path.Direction.CW)
        }

        val paint = Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        canvas.restore()

        // write to cache as PNG (keeps transparency in corners)
        val file = File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { fos ->
            output.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        }

        return@withContext Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}
