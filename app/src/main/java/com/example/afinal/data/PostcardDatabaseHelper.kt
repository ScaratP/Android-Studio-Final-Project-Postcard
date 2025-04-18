package com.example.afinal.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PostcardDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_TITLE TEXT NOT NULL," +  // 移除 NOT NULL 限制，允許空值
                    "$COLUMN_TEXT TEXT," +
                    "$COLUMN_SEND_DATE TEXT," +
                    "$COLUMN_TARGET_DATE TEXT," +
                    "$COLUMN_IMAGE_URI TEXT NOT NULL)"
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME RENAME TO temp_$TABLE_NAME;")
            onCreate(db)  // 重新創建新表
            db.execSQL(
                "INSERT INTO $TABLE_NAME ($COLUMN_ID, $COLUMN_TITLE, $COLUMN_TEXT, $COLUMN_SEND_DATE, $COLUMN_TARGET_DATE, $COLUMN_IMAGE_URI) " +
                        "SELECT $COLUMN_ID, $COLUMN_TITLE, $COLUMN_TEXT, $COLUMN_SEND_DATE, $COLUMN_TARGET_DATE, $COLUMN_IMAGE_URI FROM temp_$TABLE_NAME;"
            )
            db.execSQL("DROP TABLE temp_$TABLE_NAME;")
        }
    }

    // 插入明信片資料
    fun insertPostcard(postcard: Postcard): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, postcard.title.ifEmpty { "Untitled Postcard" })  // 預設標題
            put(COLUMN_TEXT, postcard.comment)
            put(COLUMN_SEND_DATE, postcard.sendDate)
            put(COLUMN_TARGET_DATE, postcard.targetDate)
            put(COLUMN_IMAGE_URI, postcard.image)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // 查詢所有資料
    fun getAllPostcards(): List<Postcard> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val postcards = mutableListOf<Postcard>()
        cursor.use {
            while (it.moveToNext()) {
                postcards.add(cursorToPostcard(it))
            }
        }
        return postcards
    }

    // 更新明信片資料
    fun updatePostcard(postcard: Postcard): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, postcard.title.ifEmpty { "Untitled Postcard" })  // 預設標題
            put(COLUMN_TEXT, postcard.comment)
            put(COLUMN_SEND_DATE, postcard.sendDate)
            put(COLUMN_TARGET_DATE, postcard.targetDate)
            put(COLUMN_IMAGE_URI, postcard.image)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(postcard.id.toString()))
    }

    // 刪除明信片
    fun deletePostcard(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // 將 Cursor 轉換為 Postcard 類別
    private fun cursorToPostcard(cursor: Cursor): Postcard {
        return Postcard(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)),
            sendDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEND_DATE)),
            targetDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TARGET_DATE)),
            image = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
        )
    }

    companion object {
        const val DATABASE_VERSION = 2  // 升級資料庫版本以觸發升級
        const val DATABASE_NAME = "Postcard.db"
        const val TABLE_NAME = "postcards"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_TEXT = "text"
        const val COLUMN_SEND_DATE = "send_date"
        const val COLUMN_TARGET_DATE = "target_date"
        const val COLUMN_IMAGE_URI = "image_uri"
    }
}
