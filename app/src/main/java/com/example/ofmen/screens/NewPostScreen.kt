package com.example.ofmen.screens


import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ofmen.data.PostRepository
import com.example.ofmen.viewmodel.PostViewModel

@Composable
fun NewPostScreen(
    cloudName: String,
    uploadPreset: String
) {
    // create repo & vm using a simple factory
    val repo = remember { PostRepository(cloudName = cloudName, unsignedUploadPreset = uploadPreset) }
    val factory = remember { object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(repo) as T
        }
    } }
    val viewModel: PostViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val loading by viewModel.loading.collectAsState()
    val status by viewModel.status.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mediaUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        mediaUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = { launcher.launch("image/* video/*") }) {
            Text("Choose image / video")
        }

        mediaUri?.let {
            AsyncImage(model = it, contentDescription = "preview", modifier = Modifier.fillMaxWidth().height(200.dp))
        }

        Button(
            onClick = {
                mediaUri?.let { uri ->
                    if (validateFile(context, uri)) {
                        viewModel.uploadPost(context, title, description, uri)
                    }
                }
            },
            enabled = !loading && mediaUri != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Posting..." else "Post")
        }

        status?.let {
            if (it == "success") {
                Text("✅ Posted successfully", color = MaterialTheme.colorScheme.primary)
            } else {
                Text("❌ $it", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
fun validateFile(context: Context, uri: Uri): Boolean {
    val contentResolver = context.contentResolver

    // Allowed formats
    val allowedFormats = listOf("image/jpeg", "image/png", "video/mp4")
    val mimeType = contentResolver.getType(uri)
    if (mimeType !in allowedFormats) {
        Toast.makeText(context, "Unsupported file format!", Toast.LENGTH_SHORT).show()
        return false
    }

    // File size check (max 10 MB)
    val fileSize = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
    val maxSize = 10 * 1024 * 1024 // 10 MB
    if (fileSize > maxSize) {
        Toast.makeText(context, "File too large! Max size is 10MB", Toast.LENGTH_SHORT).show()
        return false
    }

    return true
}

