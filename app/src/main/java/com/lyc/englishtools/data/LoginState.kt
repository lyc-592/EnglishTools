package com.lyc.englishtools.data

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import com.lyc.englishtools.data.dao.LoginStateDao

@Entity(tableName = "login_state")
data class LoginState(
    @PrimaryKey val userId: Int = 1, // 固定ID，只保存一条状态记录
    val isLoggedIn: Boolean,
    val username: String?
)

@Database(entities = [LoginState::class], version = 1, exportSchema = false)
abstract class LoginStateDatabase : RoomDatabase() {
    abstract fun loginStateDao(): LoginStateDao
}