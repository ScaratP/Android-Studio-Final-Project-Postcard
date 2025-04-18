package com.example.afinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.example.afinal.data.Postcard
import com.example.afinal.data.PostcardDatabaseHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val defaultImageUrls = listOf(
    "https://pbs.twimg.com/media/Gfa1MM0a4AEYS7Y?format=jpg&name=large",
    "https://pbs.twimg.com/media/GfXRF0-aIAAbAac?format=jpg&name=large",
    "https://pbs.twimg.com/media/GcKydBhasAEEhMe?format=jpg&name=large",
    "https://pbs.twimg.com/media/GfaFXytbcAADQrp?format=jpg&name=small",
    "https://pbs.twimg.com/media/GfWeBfJWUAAf_LP?format=jpg&name=small",
    "https://pbs.twimg.com/media/GfU3CxUXcAAhuRe?format=jpg&name=small",
    "https://pbs.twimg.com/media/GfGzoS_WsAANVbj?format=jpg&name=large"
)

fun getDefaultImageUrl(id: Int): String {
    return defaultImageUrls[id % defaultImageUrls.size]
}


class DeliveredActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dbHelper = PostcardDatabaseHelper(this)
            val currentDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
            val postcards = dbHelper.getFilteredPostcards(currentDate)

            DeliveredScreen(
                postcards = postcards,
                onReturnToMain = {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun DeliveredScreen(postcards: List<Postcard>, onReturnToMain: () -> Unit) {
    Scaffold(
        content = { paddingValues ->
            if (postcards.isEmpty()) {
                EmptyMessage(Modifier.padding(paddingValues))
            } else {
                PostcardApp(
                    postcards = postcards,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.image_home),
                    contentDescription = "Return to Main",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(40.dp)
                        .size(80.dp)
                        .clickable { onReturnToMain() }
                )
            }
        }
    )
}

@Composable
fun EmptyMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "哎呀!你好像還沒收到信ㄟ!",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PostcardApp(postcards: List<Postcard>, modifier: Modifier = Modifier) {
    var selectedPostcard by remember { mutableStateOf<Postcard?>(null) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    Box(modifier = modifier.fillMaxSize()) {
        when (viewMode) {
            ViewMode.LIST -> PostcardList(
                postcardList = postcards,
                onClickPostcard = { postcard ->
                    selectedPostcard = postcard
                    viewMode = ViewMode.PHOTO
                }
            )
            ViewMode.PHOTO -> selectedPostcard?.let { postcard ->
                PhotoViewer(postcard = postcard) {
                    viewMode = ViewMode.GREETING
                }
            } ?: run {
                viewMode = ViewMode.LIST
            }
            ViewMode.GREETING -> selectedPostcard?.let { postcard ->
                Greeting(postcard = postcard) {
                    viewMode = ViewMode.LIST
                    selectedPostcard = null
                }
            }
        }
    }
}

enum class ViewMode { LIST, GREETING, PHOTO }

@Composable
fun PostcardList(
    postcardList: List<Postcard>,
    onClickPostcard: (Postcard) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(postcardList) { postcard ->
            PostcardCard(
                postcard = postcard,
                onClick = { onClickPostcard(postcard) },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun PostcardCard(
    postcard: Postcard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val imageUri = if (File(postcard.image).exists()) {
                Uri.fromFile(File(postcard.image))
            } else {
                Uri.parse(getDefaultImageUrl(postcard.id))
            }

            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = postcard.comment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = postcard.title,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun PhotoViewer(postcard: Postcard, onDismiss: () -> Unit) {
    val defaultUri = getDefaultImageUrl(postcard.id)
    val imageUri = if (File(postcard.image).exists()) {
        Uri.fromFile(File(postcard.image))
    } else {
        Uri.parse(defaultUri)
    }
    val isDefaultImage = imageUri.toString() == defaultUri

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            if (isDefaultImage) {
                Text(
                    text = "抱歉把你的照片寄丟了，但你的心意我們好好送到了",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun Greeting(postcard: Postcard, onDismiss: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
    ) {
        Text(
            text = postcard.comment,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}


fun PostcardDatabaseHelper.getFilteredPostcards(currentDate: String): List<Postcard> {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val postcards = this.getAllPostcards()

    return postcards.filter { postcard ->
        try {
            val targetDate = dateFormat.parse(postcard.targetDate)
            val current = dateFormat.parse(currentDate)
            targetDate != null && current != null && !targetDate.after(current)
        } catch (e: Exception) {
            false
        }
    }
}
