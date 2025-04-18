package com.example.afinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Button
import androidx.compose.material.Text
import coil.compose.rememberAsyncImagePainter

class AddActivity : ComponentActivity() {
    private val selectedImageUri = mutableStateOf<Uri?>(null)

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri.value = uri
                Log.i("AddActivity", "Selected image URI: $uri")
                Toast.makeText(this, "超讚的回憶碎片我收好了!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AddScreen(
                onUploadClick = { imagePicker.launch("image/*") },
                onNextClick = {
                    val imagePath = selectedImageUri.value?.toString()
                    if (imagePath != null) {
                        navigateToTextActivity(imagePath)
                    } else {
                        Toast.makeText(this, "唉喲！放一下照片嘛！", Toast.LENGTH_SHORT).show()
                    }
                },
                imageUri = selectedImageUri.value
            )
        }
    }

    // 跳轉到下一個活動，傳遞圖片路徑
    private fun navigateToTextActivity(imagePath: String) {
        val intent = Intent(this, TextActivity::class.java).apply {
            putExtra("imagePath", imagePath)
        }
        startActivity(intent)
    }
}

// AddScreen 顯示介面與按鈕
@Composable
fun AddScreen(
    onUploadClick: () -> Unit,
    onNextClick: () -> Unit,
    imageUri: Uri?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp, 200.dp)
                .background(Color.Gray)
                .padding(16.dp)
        ) {
            if (imageUri == null) {
                Text(
                    text = "選張超讚的照片作為開始吧!",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(150.dp, 100.dp)
                .background(Color.Gray)
                .padding(16.dp)
                .clickable { onUploadClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "↑", fontSize = 24.sp)
                Text(text = "點擊上傳", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNextClick) {
            Text(text = "下一步GOGO", fontSize = 18.sp)
        }
    }
}
