package com.example.afinal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                onImage1Click = {
                    val intent = Intent(this, AllActivity::class.java)
                    startActivity(intent)
                },
                onImage2Click = {
                    val intent = Intent(this, DeliveredActivity::class.java)
                    startActivity(intent)
                },
                onPostboxClick = {
                    val intent = Intent(this, AddActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    onImage1Click: () -> Unit,
    onImage2Click: () -> Unit,
    onPostboxClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFAD8463)) // Updated background color
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Main Images
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(70.dp)) // Add spacing
            Image(
                painter = painterResource(id = R.drawable.image_cat),
                contentDescription = "Main Image",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(id = R.drawable.image_postbox),
                contentDescription = "Postbox Image",
                modifier = Modifier
                    .size(420.dp)
                    .clickable { onPostboxClick() }, // Click event
                contentScale = ContentScale.Crop
            )
        }

        // Bottom-left Image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.image_1),
                contentDescription = "Image 1",
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .clickable { onImage1Click() },
                contentScale = ContentScale.Crop
            )
        }

        // Bottom-right Image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.image_2),
                contentDescription = "Image 2",
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .clickable { onImage2Click() },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        onImage1Click = {},
        onImage2Click = {},
        onPostboxClick = {}
    )
}
