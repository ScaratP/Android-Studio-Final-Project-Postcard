// AllActivity.kt

package com.example.afinal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.afinal.data.Postcard
import com.example.afinal.data.PostcardDatabaseHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
fun calculateStatus(arrivedDate: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val currentDate = LocalDate.now()
        val arrivalDate = LocalDate.parse(arrivedDate, formatter)

        if (currentDate.isBefore(arrivalDate)) "寄送中" else "已送達"
    } catch (e: Exception) {
        "未知"
    }
}

class AllActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = PostcardDatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val postcardList = mutableListOf<Postcard>()

        val cursor = db.query(PostcardDatabaseHelper.TABLE_NAME,null,null,null,null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(PostcardDatabaseHelper.COLUMN_ID))
                val title = getString(getColumnIndexOrThrow(PostcardDatabaseHelper.COLUMN_TITLE))
                val sendDate = getString(getColumnIndexOrThrow(PostcardDatabaseHelper.COLUMN_SEND_DATE))
                val targetDate = getString(getColumnIndexOrThrow(PostcardDatabaseHelper.COLUMN_TARGET_DATE))
                val comment = getString(getColumnIndexOrThrow(PostcardDatabaseHelper.COLUMN_TEXT))
                val image = getString(getColumnIndexOrThrow(PostcardDatabaseHelper.COLUMN_IMAGE_URI))

                postcardList.add(
                    Postcard(
                        id = id,
                        title = title,
                        sendDate = sendDate,
                        targetDate = targetDate,
                        comment = comment,
                        image = image
                    )
                )
            }
        }
        cursor.close()

        setContent {
            val context = LocalContext.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SendList(
                    postcardList = postcardList,
                    onUpdatePostcard = { postcard ->
                        if (calculateStatus(postcard.targetDate) == "寄送中") {
                            showDatePicker(postcard)
                        } else {
                            Toast.makeText(this@AllActivity, "這已經送到了喔!要不要去看一下!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeletePostcard = { postcard ->
                        if (calculateStatus(postcard.targetDate) == "已送達") {
                            deletePostcard(postcard)
                            Toast.makeText(this@AllActivity, "明信片已刪除，跟回憶說掰掰", Toast.LENGTH_SHORT).show()
                            recreate()
                        } else {
                            Toast.makeText(this@AllActivity, "我快送到了不要刪掉我!!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                ///按下image_home返回主畫面
                Image(
                    painter = painterResource(id = R.drawable.image_home),
                    contentDescription = "回到主畫面",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(40.dp)
                        .size(80.dp)
                        .clickable {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }
                )
            }
        }

    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker(postcard: Postcard) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val newDate = String.format("%04d/%02d/%02d", selectedYear, selectedMonth + 1, selectedDay)

            if (LocalDate.parse(newDate, DateTimeFormatter.ofPattern("yyyy/MM/dd")) > LocalDate.now()) {
                updatePostcardDate(postcard, newDate)
                Toast.makeText(this, "送達日期已更新，我們努力配送中!", Toast.LENGTH_SHORT).show()
                recreate()
            } else {
                Toast.makeText(this, "這不是時光機啦!只能選擇未來的日期!", Toast.LENGTH_SHORT).show()
            }
        }, year, month, day)

        datePickerDialog.datePicker.minDate = calendar.timeInMillis // 限制只能選擇未來日期
        datePickerDialog.show()
    }

    private fun deletePostcard(postcard: Postcard) {
        val dbHelper = PostcardDatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val selection = "${PostcardDatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(postcard.id.toString())

        db.delete(PostcardDatabaseHelper.TABLE_NAME, selection, selectionArgs)
        db.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePostcardDate(postcard: Postcard, newDate: String) {
        val dbHelper = PostcardDatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val values = android.content.ContentValues().apply {
            put(PostcardDatabaseHelper.COLUMN_TARGET_DATE, newDate)
        }

        val selection = "${PostcardDatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(postcard.id.toString())

        db.update(PostcardDatabaseHelper.TABLE_NAME, values, selection, selectionArgs)
        db.close()

        postcard.targetDate = newDate // 更新本地資料
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SendList(
    postcardList: List<Postcard>,
    onUpdatePostcard: (Postcard) -> Unit,
    onDeletePostcard: (Postcard) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortedPostcards by remember { mutableStateOf(postcardList) }
    var filterText by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("全部") }
    var filterSendMonth by remember { mutableStateOf("") }
    var filterTargetMonth by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("寄出日期") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column {
        // 搜尋欄
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(2.dp, Color(0xFF292929), shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            BasicTextField(
                value = filterText,
                onValueChange = { filterText = it },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    if (filterText.isEmpty()) {
                        Text("搜尋標題", color = Color.Gray)
                    }
                    innerTextField()
                },
                cursorBrush = SolidColor(Color.Black)
            )
        }
        // 篩選與排序功能
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Button(
                    onClick = { filterStatus = "全部" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F7F7F))
                ) {
                    Text("全部", color = Color.White)
                }
                Button(
                    onClick = { filterStatus = "寄送中" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F7F7F))
                ) {
                    Text("寄送中", color = Color.White)
                }
                Button(
                    onClick = { filterStatus = "已送達" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F7F7F))
                ) {
                    Text("已送達", color = Color.White)
                }
            }
            Box {
                TextButton(onClick = { dropdownExpanded = true }) {
                    Text("排序: ${sortOption}")
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                ) {
                    DropdownMenuItem(onClick = {
                        sortOption = "寄出日期"
                        sortedPostcards = postcardList.sortedBy { it.sendDate }
                        dropdownExpanded = false
                    }) {
                        Text("寄出日期")
                    }
                    DropdownMenuItem(onClick = {
                        sortOption = "送達日期"
                        sortedPostcards = postcardList.sortedBy { it.targetDate }
                        dropdownExpanded = false
                    }) {
                        Text("送達日期")
                    }
                    DropdownMenuItem(onClick = {
                        sortOption = "標題"
                        sortedPostcards = postcardList.sortedBy { it.title }
                        dropdownExpanded = false
                    }) {
                        Text("標題")
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, Color(0xFF292929), shape = RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = filterSendMonth,
                    onValueChange = { filterSendMonth = it },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (filterSendMonth.isEmpty()) {
                            Text("寄送月份 (yyyy/MM)", color = Color.Gray)
                        }
                        innerTextField()
                    },
                    cursorBrush = SolidColor(Color.Black)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, Color(0xFF292929), shape = RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = filterTargetMonth,
                    onValueChange = { filterTargetMonth = it },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (filterTargetMonth.isEmpty()) {
                            Text("送達月份 (yyyy/MM)", color = Color.Gray)
                        }
                        innerTextField()
                    },
                    cursorBrush = SolidColor(Color.Black)
                )
            }
        }


        // 篩選資料
        val filteredPostcards = sortedPostcards.filter {
            (filterText.isEmpty() || it.title.contains(filterText, ignoreCase = true)) &&
                    (filterStatus == "全部" || calculateStatus(it.targetDate) == filterStatus) &&
                    (filterSendMonth.isEmpty() || it.sendDate.startsWith(filterSendMonth)) &&
                    (filterTargetMonth.isEmpty() || it.targetDate.startsWith(filterTargetMonth))
        }

        if (filteredPostcards.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "沒有符合條件的明信片!你要不要寄一張明信片",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        } else {
            LazyColumn(modifier = modifier) {
                items(filteredPostcards) { postcard ->
                    val status = calculateStatus(postcard.targetDate)
                    val backgroundColor = when (status) {
                        "寄送中" -> Color(0xFFE0F7FA) // 淺藍色
                        "已送達" -> Color(0xFFE8F5E9) // 淺綠色
                        else -> Color(0xFFF5F5F5)    // 默認淺灰色
                    }
                    val textColor = when (status) {
                        "寄送中" -> Color(0xFF01579B) // 深藍色
                        "已送達" -> Color(0xFF2E7D32) // 深綠色
                        else -> Color(0xFF757575)    // 默認灰色
                    }

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onUpdatePostcard(postcard) },
                                onLongClick = { onDeletePostcard(postcard) }
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "寄出時間",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "送達時間",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = postcard.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = postcard.sendDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = postcard.targetDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

