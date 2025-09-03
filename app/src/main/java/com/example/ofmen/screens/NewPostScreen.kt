package com.example.ofmen.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ofmen.data.PostRepository
import com.example.ofmen.viewmodel.PostViewModel
import java.io.File

@Composable
fun NewPostScreen(
    cloudName: String,
    uploadPreset: String,
    navController: NavHostController
) {
    val repo = remember { PostRepository(cloudName, uploadPreset) }
    val factory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PostViewModel(repo) as T
            }
        }
    }
    val viewModel: PostViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val loading by viewModel.loading.collectAsState()
    val status by viewModel.status.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showCrop by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val mime = context.contentResolver.getType(it)
            if (mime?.startsWith("image/") == true) {
                showCrop = it
            } else {
                selectedUri = it
            }
        }
    }

    if (showCrop != null) {
        PostCropScreen(
            imageUri = showCrop!!,
            onImageCropped = { cropped ->
                selectedUri = cropped
                showCrop = null
            },
            onCancel = { showCrop = null }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        Text(
                            "Create New Post",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Button(
                        onClick = { launcher.launch("image/* video/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("📂 Choose Image / Video")
                    }

                    selectedUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(15.dp))
                        )
                    }

                    Button(
                        onClick = {
                            selectedUri?.let { uri ->
                                if (validateFile(context, uri)) {
                                    viewModel.uploadPost(context, title, description, uri)
                                }
                            }
                        },
                        enabled = !loading && selectedUri != null && status != "success",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                loading -> Color.Gray
                                status == "success" -> Color.Green
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    ) {
                        Text(
                            when {
                                loading -> "⏳ Posting..."
                                status == "success" -> "✅ Posted"
                                else -> "🚀 Post"
                            }
                        )
                    }

                    status?.let {
                        val color =
                            if (it == "success") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        Text(
                            text = if (it == "success") "✅ Posted Successfully!" else "❌ $it",
                            color = color,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("YourPosts") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text("Your Posts",color = MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
}



fun validateFile(context: Context, uri: Uri): Boolean {
    val contentResolver = context.contentResolver
    val allowedFormats = listOf("image/jpeg", "image/png", "video/mp4")

    val mimeType = contentResolver.getType(uri)
        ?: run {
            // fallback: check extension
            val path = uri.toString()
            when {
                path.endsWith(".png", true) -> "image/png"
                path.endsWith(".jpg", true) || path.endsWith(".jpeg", true) -> "image/jpeg"
                path.endsWith(".mp4", true) -> "video/mp4"
                else -> null
            }
        }

    if (mimeType !in allowedFormats) {
        Toast.makeText(context, "Unsupported file format!", Toast.LENGTH_SHORT).show()
        return false
    }

    val fileSize = try {
        context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
    } catch (e: Exception) {
        File(uri.path ?: "").length()
    }
    val maxSize = 100 * 1024 * 1024
    if (fileSize > maxSize) {
        Toast.makeText(context, "File too large! Max size is 10MB", Toast.LENGTH_SHORT).show()
        return false
    }

    return true
}
