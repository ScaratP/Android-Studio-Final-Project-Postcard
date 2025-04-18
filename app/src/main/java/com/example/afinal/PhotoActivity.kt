package com.example.afinal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

class PhotoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 接收從 AddActivity 傳遞的圖片路徑
        val imagePath = intent.getStringExtra("imagePath")

        // 模擬圖片存入資料庫的邏輯
        val imageStoredInDatabase = imagePath?.let { storeImageToDatabase(it) } ?: false

        setContent {
            PhotoScreen(
                imagePath = imagePath,
                imageStored = imageStoredInDatabase,
                onClick = {
                    startActivity(Intent(this, TextActivity::class.java))
                }
            )
        }
    }

    // 模擬存入資料庫的邏輯
    private fun storeImageToDatabase(imagePath: String): Boolean {
        // 模擬成功存入
        return true
    }
}

@Composable
fun PhotoScreen(imagePath: String?, imageStored: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
            .clickable { onClick() }, // 點擊事件返回 TextActivity
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (imagePath != null) {
                // 動態加載圖片
                Image(
                    painter = rememberImagePainter(data = imagePath),
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            } else {
                // 顯示占位符或錯誤信息
                Text(
                    text = "未選擇圖片",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 顯示確認消息
            Text(
                text = if (imageStored) "圖片已成功存入資料庫！" else "存入資料庫失敗！",
                fontSize = 16.sp
            )
        }
    }
}
