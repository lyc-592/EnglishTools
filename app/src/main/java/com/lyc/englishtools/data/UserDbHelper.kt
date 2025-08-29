package com.lyc.englishtools.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UsersDatabase"
        private const val DATABASE_VERSION = 2  // 版本升级
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_SALT = "salt"  // 新增字段
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_SALT TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_SALT TEXT")
        }
    }

    // 注册用户（存储哈希密码和盐值）
    fun registerUser(username: String, hashedPassword: String, salt: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, hashedPassword)
            put(COLUMN_SALT, salt)
        }
        return db.insert(TABLE_USERS, null, values) != -1L
    }

    // 获取用户凭证（哈希密码和盐值）
    fun getUserCredentials(username: String): Pair<String, String>? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_PASSWORD, COLUMN_SALT),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                val password = it.getString(0)
                val salt = it.getString(1)
                password to salt
            } else {
                null
            }
        }
    }
}