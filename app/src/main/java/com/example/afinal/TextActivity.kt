package com.example.afinal

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.data.PostcardDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class TextActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val dbHelper = PostcardDatabaseHelper(this)
        super.onCreate(savedInstanceState)

        // 接收從 AddActivity 傳遞過來的圖片路徑
        val imagePath = intent.getStringExtra("imagePath") ?: ""

        setContent {
            TextScreen(dbHelper, imagePath)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TextScreen(dbHelper: PostcardDatabaseHelper, imagePath: String) {
    var titleText by remember { mutableStateOf(TextFieldValue("")) }
    var contentText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDate by remember { mutableStateOf("") }

    val currentDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Gray border box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 50.dp)
                .border(width = 2.dp, color = Color.Gray)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = currentDate,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 15.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 70.dp, start = 35.dp, end = 35.dp)
        ) {
            BasicTextField(
                value = titleText,
                onValueChange = {
                    if (it.text.length <= 16 && !it.text.contains("\n")) {
                        titleText = it
                    } else if (it.text.length > 16) {
                        Toast.makeText(context, "標題字數已達限制，標題不是內文啦", Toast.LENGTH_SHORT).show()
                    }
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 32.sp,
                    color = Color.Black
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                decorationBox = { innerTextField ->
                    if (titleText.text.isEmpty()) {
                        Text(
                            text = "請輸入標題",
                            fontSize = 32.sp,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
            )

            BasicTextField(
                value = contentText,
                onValueChange = {
                    if (it.text.length <= 250 && !it.text.contains("\n")) {
                        contentText = it
                    } else if (it.text.length > 250) {
                        Toast.makeText(context, "內容字數已達限制，回憶太過沉重也會寄不出去啦", Toast.LENGTH_SHORT).show()
                    }
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 26.sp,
                    color = Color.Black
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                decorationBox = { innerTextField ->
                    if (contentText.text.isEmpty()) {
                        Text(
                            text = "請輸入內容",
                            fontSize = 26.sp,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 70.dp, bottom = 80.dp)
                .clickable {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            if (isFutureDate(selectedYear, selectedMonth, selectedDay)) {
                                selectedDate = String.format("%04d/%02d/%02d", selectedYear, selectedMonth + 1, selectedDay)
                            } else {
                                Toast.makeText(context, "日期必須是未來日期，這是通往未來的時光機，沒辦法寄到過去😥", Toast.LENGTH_SHORT).show()
                            }
                        },
                        year,
                        month,
                        day
                    ).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selectedDate.isEmpty()) "選擇送達日期" else selectedDate,
                fontSize = 20.sp,
                color = Color(0xFF5078BE)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.image_3),
            contentDescription = "Selected Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset((-50).dp, (-70).dp)
                .clickable {
                    val db = dbHelper.writableDatabase
                    if (titleText.text.isEmpty() || contentText.text.isEmpty() || selectedDate.isEmpty()) {
                        Toast.makeText(context, "請填寫所有欄位，空白明信片就是普通的照片", Toast.LENGTH_SHORT).show()
                    } else {
                        if (imagePath.isEmpty()) {
                            Toast.makeText(context, "圖片 URI 不可為空", Toast.LENGTH_SHORT).show()
                        } else {
                            val values = ContentValues().apply {
                                put(PostcardDatabaseHelper.COLUMN_TITLE, titleText.text)
                                put(PostcardDatabaseHelper.COLUMN_TEXT, contentText.text)
                                put(PostcardDatabaseHelper.COLUMN_SEND_DATE, currentDate)
                                put(PostcardDatabaseHelper.COLUMN_TARGET_DATE, selectedDate)
                                put(PostcardDatabaseHelper.COLUMN_IMAGE_URI, imagePath)
                            }
                            val newRowId = try {
                                db.insertOrThrow(PostcardDatabaseHelper.TABLE_NAME, null, values)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "資料庫錯誤：${e.message}", Toast.LENGTH_LONG).show()
                                -1L
                            }

                            if (newRowId != -1L) {
                                AlertDialog.Builder(context)
                                    .setMessage("投資有賺有賠，寄信有丟有得，回憶不一定想得起來，但心意一定準時抵達")
                                    .setPositiveButton("知道了o7") { dialog, _ ->
                                        dialog.dismiss()
                                        try {
                                            context.startActivity(Intent(context, AllActivity::class.java))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(context, "無法跳轉到下一個頁面：${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    .show()
                            } else {
                                Toast.makeText(context, "寄送失敗，我很抱歉", Toast.LENGTH_LONG).show()
                            }

                        }
                    }
                }
        )
    }
}


fun isFutureDate(year: Int, month: Int, day: Int): Boolean {
    val today = Calendar.getInstance()
    val selectedDate = Calendar.getInstance().apply {
        set(year, month, day)
    }
    return selectedDate.after(today)
}



